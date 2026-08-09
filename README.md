# Travel Packages Platform

Plataforma de compra de pacotes de viagem (voo + hotel + carro + passeio),
construída como estudo prático para preparação de entrevista de backend sênior
(Java 17, Spring Boot, microsserviços, DDD, mensageria, observabilidade).

Veja [`docs/architecture.md`](docs/architecture.md) para o desenho completo dos
bounded contexts e da saga.

## Estrutura

```
travel-packages-platform/
├── package-service/            # orquestrador do pacote (saga) — porta 8081
├── flight-service/              # catálogo de voos + reservas — porta 8082
├── hotel-service/                # catálogo de hotéis + reservas — porta 8083
├── car-reservation-service/       # catálogo de carros + reservas — porta 8084
├── tour-service/                  # catálogo de passeios + reservas — porta 8085
├── docs/                          # arquitetura, ADRs
├── infra/docker/                  # docker-compose (Postgres, RabbitMQ)
└── pom.xml                        # aggregator Maven do monorepo
```

Cada serviço tem seu próprio banco Postgres (`package_service`, `flight_service`,
`hotel_service`, `car_reservation_service`, `tour_service`), criados automaticamente
pelo `docker-compose` — ver `docs/architecture.md` pra entender por que isso é
importante (database-per-service).

## Rodando localmente

1. Suba a infra (Postgres + RabbitMQ):
   ```bash
   cd infra/docker
   docker compose up -d
   ```

2. Rode cada serviço em um terminal separado (ainda não tem Maven Wrapper
   configurado, use o `mvn` da sua máquina — Java 17+ e Maven 3.9+):
   ```bash
   cd package-service && mvn spring-boot:run          # porta 8081
   cd flight-service && mvn spring-boot:run            # porta 8082
   cd hotel-service && mvn spring-boot:run              # porta 8083
   cd car-reservation-service && mvn spring-boot:run     # porta 8084
   cd tour-service && mvn spring-boot:run                 # porta 8085
   ```

3. Teste o endpoint (o `offerId` do voo precisa existir de verdade — crie uma
   oferta no `flight-service` primeiro, ver seção abaixo, e use o `id` retornado):
   ```bash
   curl -X POST http://localhost:8081/api/packages \
     -H "Content-Type: application/json" \
     -d '{
       "customerId": "customer-123",
       "flight": { "offerId": "SUBSTITUA_PELO_ID_DA_OFERTA", "quantity": 2 },
       "paymentMethod": { "type": "CREDIT_CARD", "installments": 3 }
     }'
   ```
   A resposta vem `202 Accepted` com o item em status `PENDING` — a reserva
   de verdade acontece assíncrona, via saga. Consulte `GET /api/packages/{id}`
   depois de alguns segundos pra ver o status virar `CONFIRMED` (ou `CANCELLED`
   se o voo não tiver assento disponível).

## Testando o flight-service

```bash
# 1. cria uma oferta
curl -X POST http://localhost:8082/api/flights \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "FOR", "destination": "LIS",
    "departureDate": "2026-10-10", "returnDate": "2026-10-20",
    "airline": "TAP", "price": 2500.00, "totalSeats": 2
  }'

# 2. usa o "id" retornado pra reservar (troque {offerId} abaixo)
curl -X POST http://localhost:8082/api/flights/{offerId}/reservations \
  -H "Content-Type: application/json" \
  -d '{ "customerId": "customer-123", "passengers": 2 }'

# 3. tente reservar de novo -> deve voltar 409 Conflict (sem assentos)
```

## Testando o hotel-service

```bash
curl -X POST http://localhost:8083/api/hotels \
  -H "Content-Type: application/json" \
  -d '{
    "hotelName": "Hotel Lisboa Centro", "city": "Lisboa", "roomType": "DOUBLE",
    "checkIn": "2026-10-10", "checkOut": "2026-10-20",
    "pricePerNight": 450.00, "totalRooms": 5
  }'

curl -X POST http://localhost:8083/api/hotels/{offerId}/reservations \
  -H "Content-Type: application/json" \
  -d '{ "customerId": "customer-123", "guests": 2 }'
```

## Testando o car-reservation-service

```bash
curl -X POST http://localhost:8084/api/cars \
  -H "Content-Type: application/json" \
  -d '{
    "category": "ECONOMY", "model": "Fiat Argo",
    "pickupDate": "2026-10-10", "returnDate": "2026-10-20",
    "dailyRate": 150.00, "totalUnits": 3
  }'

curl -X POST http://localhost:8084/api/cars/{offerId}/reservations \
  -H "Content-Type: application/json" \
  -d '{ "customerId": "customer-123" }'
```

## Testando o tour-service

```bash
curl -X POST http://localhost:8085/api/tours \
  -H "Content-Type: application/json" \
  -d '{
    "tourName": "Passeio de barco em Sintra", "location": "Sintra",
    "date": "2026-10-15", "pricePerPerson": 120.00, "totalSlots": 10
  }'

curl -X POST http://localhost:8085/api/tours/{offerId}/reservations \
  -H "Content-Type: application/json" \
  -d '{ "customerId": "customer-123", "participants": 2 }'
```

## Rodando os testes

```bash
for service in package-service flight-service hotel-service car-reservation-service tour-service; do
  (cd $service && mvn test)
done
```

Os testes de integração usam **Testcontainers** — é necessário ter Docker
rodando localmente.

## Roadmap do projeto

Ver checklist em [`docs/architecture.md`](docs/architecture.md#status-atual-do-projeto).
