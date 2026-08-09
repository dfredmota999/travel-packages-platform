# Arquitetura — Travel Packages Platform

## Domínio

Plataforma de compra de pacotes de viagem, combinando até 4 itens: **voo**, **hotel**,
**aluguel de carro** e **passeio**. O cliente pode montar um pacote com qualquer
subconjunto desses itens (ex: só voo + hotel).

## Bounded Contexts

| Serviço | Responsabilidade | Aggregate root | Porta | Banco |
|---|---|---|---|---|
| `package-service` | Orquestra a compra do pacote (saga), mantém o estado geral | `TravelPackage` | 8081 | `package_service` |
| `flight-service` | Ofertas de voo, assentos, disponibilidade | `FlightOffer` | 8082 | `flight_service` |
| `hotel-service` | Quartos, disponibilidade, tarifas | `HotelOffer` | 8083 | `hotel_service` |
| `car-reservation-service` | Veículos, disponibilidade, categorias | `CarOffer` | 8084 | `car_reservation_service` |
| `tour-service` | Passeios/atividades, vagas | `TourOffer` | 8085 | `tour_service` |
| `payment-service` | Processamento de pagamento | `Payment` | — | `payment_service` |
| `notification-service` | Notificações (e-mail/SMS) reagindo a eventos | — (stateless) | — | — |

O `package-service` não conhece o modelo interno dos serviços de produto — ele só
troca comandos/eventos via contratos próprios (anti-corruption layer).

## Saga (transação distribuída)

Comprar um pacote envolve reservar em até 4 serviços diferentes + processar
pagamento. Usamos **saga orquestrada**: o `package-service` é o orquestrador,
publica comandos no RabbitMQ e reage às respostas.

```
package-service                                    flight-service
      |                                                    |
      | cmd.reserve (exchange saga.commands,               |
      |   routing key flight.reserve)                      |
      | -------------------------------------------------> |
      |                                                     | reserva assento
      |    evt.reserved / evt.rejected (exchange            | (ou rejeita se
      |    saga.events)                                     |  sem disponibilidade)
      | <------------------------------------------------- |
      |
      | (todos os itens confirmados) -> AWAITING_PAYMENT -> CONFIRMED
      |
      | se rejeitado: COMPENSATING -> cmd.cancel pros itens já confirmados
      | -------------------------------------------------> |
      |    evt.cancelled                                    | libera assento
      | <------------------------------------------------- |
      |
      | (sem itens confirmados restando) -> CANCELLED
```

Implementado hoje: `package-service` ↔ `flight-service`. `hotel-service`,
`car-reservation-service` e `tour-service` seguem exatamente o mesmo padrão —
o `SagaCommandPublisher` e o `SagaOrchestrator` já têm os pontos de extensão
marcados com `// TODO`.

## Comunicação

- **Síncrona (REST):** cliente → `package-service` (criação e consulta de pacote)
- **Assíncrona (RabbitMQ):** `package-service` ↔ serviços de produto ↔ `payment-service`
  ↔ `notification-service`

## Padrão de dados

- **Database-per-service:** cada microsserviço tem seu próprio schema/banco Postgres.
  O `docker-compose` já cria um banco por serviço (`package_service`, `flight_service`,
  `hotel_service`, `car_reservation_service`, `tour_service`, `payment_service`) via
  `infra/docker/init-multiple-dbs.sh`, mesmo que o serviço ainda não exista.
- Sem transação distribuída via 2PC — consistência eventual garantida pela saga
- **Importante:** o `package-service` guarda apenas uma *referência* de cada item
  (offerId + status da reserva) no seu próprio agregado `TravelPackage` — ele nunca
  acessa o banco do `flight-service` diretamente. Quem é dono do catálogo completo
  de voos (preço, assentos, disponibilidade) é o `flight-service`, no seu próprio
  banco (`flight_service`). Isso é o que garante o isolamento do database-per-service:
  cada serviço só lê/escreve no seu próprio schema.

### Modelagem interna do `package-service`

O `package-service` guarda o **mínimo necessário** pra orquestrar a saga — não
duplica o catálogo dos serviços de produto. Cada item do pacote é uma linha na
tabela `package_items` (entidade `PackageItem`, com um `ItemType` como
discriminador: `FLIGHT`/`HOTEL`/`CAR`/`TOUR`) com só 4 campos: `offerId`
(a oferta que o cliente escolheu, já existente no serviço dono), `quantity`,
`reservationId` (devolvido quando confirmado) e `status`. O pagamento continua
em tabela própria (`payment_info`), ligado por `@OneToOne` + `@MapsId`.

Isso troca as 4 classes de value object antigas (`FlightBooking`,
`HotelBooking`, `CarRentalBooking`, `TourBooking` — cada uma com datas,
hóspedes, tipo de quarto etc. duplicados do catálogo) por uma única entidade
`PackageItem` reutilizável. Menos repetição, e adicionar um novo tipo de item
no futuro é só um valor novo no enum `ItemType`, não uma classe nova.

### Saga via RabbitMQ (package-service ↔ flight-service)

Dois exchanges topic, um por direção:

- **`saga.commands`** — o `package-service` publica `flight.reserve` /
  `flight.cancel`; o `flight-service` consome numa fila própria por routing key
- **`saga.events`** — o `flight-service` publica `flight.reserved` /
  `flight.rejected` / `flight.cancelled`; o `package-service` consome cada um
  numa fila dedicada

O `SagaOrchestrator` (no `package-service`) é quem decide as transições:
`startSaga` dispara o comando de reserva; ao receber `flight.reserved`,
confirma o item e — se todos os itens do pacote já estiverem confirmados —
avança pra `AWAITING_PAYMENT` e (por ora, simulado) `CONFIRMED`; ao receber
`flight.rejected`, entra em `COMPENSATING` e publica `flight.cancel` pra
qualquer item já confirmado antes de marcar `CANCELLED`.

O `FlightSagaListener` (no `flight-service`) reutiliza exatamente o mesmo
`FlightOfferService` que o endpoint REST usa — a saga é só mais uma porta de
entrada pra regra de negócio que já existia. Falhas de negócio (sem
assentos) viram evento `rejected`; não deixamos a exception subir, porque
isso faria o RabbitMQ reenfileirar a mensagem pra tentar de novo, e "sem
assento" não muda numa segunda tentativa.

Teste de integração cobrindo isso: `PackageControllerIntegrationTest` sobe
Postgres **e** RabbitMQ reais via Testcontainers, cria um pacote, simula a
resposta do `flight-service` publicando o evento diretamente no exchange, e
usa Awaitility pra esperar a saga (assíncrona) terminar.

## Status atual do projeto

- [x] Modelagem enxuta do `package-service` (`PackageItem` único com discriminador
      de tipo, em vez de 4 classes quase idênticas)
- [x] Endpoint REST `POST /api/packages` e `GET /api/packages/{id}`
- [x] Tratamento de erro RFC 7807 em todos os serviços
- [x] Teste de integração com Testcontainers (Postgres real) em todos os serviços
- [x] `flight-service`: catálogo de ofertas + reserva de assentos, lock otimista (`@Version`)
- [x] `hotel-service`: catálogo de ofertas + reserva de quartos, lock otimista
- [x] `car-reservation-service`: catálogo de ofertas + reserva de veículos, lock otimista
- [x] `tour-service`: catálogo de ofertas + reserva de vagas, lock otimista
- [x] Saga via RabbitMQ entre `package-service` ↔ `flight-service` (comandos,
      eventos, compensação), testada com Testcontainers (Postgres + RabbitMQ) + Awaitility
- [ ] Ligar `hotel-service`, `car-reservation-service` e `tour-service` na mesma saga
- [ ] `payment-service` (hoje o pagamento é simulado/auto-aprovado no orquestrador)
- [ ] `notification-service`
- [ ] Docker Compose completo + deploy em K8s local (kind/minikube)
- [ ] Observabilidade (OpenTelemetry + Grafana)

### Padrão comum aos 4 serviços de produto

`flight-service`, `hotel-service`, `car-reservation-service` e `tour-service` seguem
exatamente o mesmo template, pra reforçar em entrevista que você sabe replicar um
padrão consistente entre microsserviços:

- **Offer** (aggregate root, catálogo): `@Version` para lock otimista — evita
  vender o mesmo assento/quarto/carro/vaga duas vezes em reservas concorrentes
- **Reservation** (aggregate root próprio, referencia a offer só pelo ID): registro
  da reserva, com `cancel()` pronto para a compensação da saga
- `POST /{recurso}` cria a oferta (ação de catálogo/admin)
- `POST /{recurso}/{offerId}/reservations` reserva — retorna `201 Created` ou
  `409 Conflict` (RFC 7807) se não houver disponibilidade
- `GET /{recurso}/{id}` e `GET /{recurso}/reservations/{id}` para consulta
- Teste de integração com Testcontainers cobrindo o caminho feliz e o 409
