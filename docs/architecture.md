# Arquitetura — Travel Packages Platform

## Domínio

Plataforma de compra de pacotes de viagem, combinando até 4 itens: **voo**, **hotel**,
**aluguel de carro** e **passeio**. O cliente pode montar um pacote com qualquer
subconjunto desses itens (ex: só voo + hotel).

## Bounded Contexts

| Serviço | Responsabilidade | Aggregate root |
|---|---|---|
| `package-service` | Orquestra a compra do pacote (saga), mantém o estado geral | `TravelPackage` |
| `flight-service` | Ofertas de voo, assentos, disponibilidade | `FlightOffer` |
| `hotel-service` | Quartos, disponibilidade, tarifas | `RoomOffer` |
| `car-rental-service` | Veículos, disponibilidade, categorias | `CarOffer` |
| `tour-service` | Passeios/atividades, vagas | `TourOffer` |
| `payment-service` | Processamento de pagamento | `Payment` |
| `notification-service` | Notificações (e-mail/SMS) reagindo a eventos | — (stateless) |

O `package-service` não conhece o modelo interno dos serviços de produto — ele só
troca comandos/eventos via contratos próprios (anti-corruption layer).

## Saga (transação distribuída)

Comprar um pacote envolve reservar em até 4 serviços diferentes + processar
pagamento. Usamos **saga orquestrada**: o `package-service` é o orquestrador,
publica comandos no RabbitMQ e reage às respostas.

```
package-service                 flight-service / hotel-service / car-rental-service / tour-service
      |  publish cmd.reserve-item (por item solicitado)
      | ------------------------------------------------->
      |
      |  <---------------------------------------------- evt.item-reserved / evt.item-rejected
      |
      | (quando todos confirmados) publish cmd.process-payment -> payment-service
      | <---------------------------------------------- evt.payment-approved / evt.payment-rejected
      |
      | se algo falhar: publish cmd.cancel-item para os itens já confirmados (compensação)
```

Estados do agregado `TravelPackage`: `CREATED → PROCESSING → AWAITING_PAYMENT →
CONFIRMED`, com desvios para `COMPENSATING → CANCELLED` ou `FAILED` em caso de erro.

> Próxima etapa do projeto: implementar os producers/consumers RabbitMQ e o
> `SagaOrchestrator` que reage aos eventos e chama os métodos de domínio
> (`markAwaitingPayment()`, `confirm()`, `startCompensating()`, etc.) já
> definidos em `TravelPackage`.

## Comunicação

- **Síncrona (REST):** cliente → `package-service` (criação e consulta de pacote)
- **Assíncrona (RabbitMQ):** `package-service` ↔ serviços de produto ↔ `payment-service`
  ↔ `notification-service`

## Padrão de dados

- **Database-per-service:** cada microsserviço tem seu próprio schema/banco Postgres
- Sem transação distribuída via 2PC — consistência eventual garantida pela saga

## Status atual do projeto

- [x] Modelagem de domínio do `package-service` (aggregate root + value objects)
- [x] Endpoint REST `POST /api/packages` e `GET /api/packages/{id}`
- [x] Tratamento de erro RFC 7807
- [x] Teste de integração com Testcontainers (Postgres real)
- [ ] Saga via RabbitMQ (producers/consumers no `package-service`)
- [ ] `flight-service` (primeiro serviço de produto, vira template pros demais)
- [ ] `hotel-service`, `car-rental-service`, `tour-service`
- [ ] `payment-service`
- [ ] `notification-service`
- [ ] Docker Compose completo + deploy em K8s local (kind/minikube)
- [ ] Observabilidade (OpenTelemetry + Grafana)
