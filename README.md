# Travel Packages Platform

Plataforma de compra de pacotes de viagem (voo + hotel + carro + passeio),
construída como estudo prático para preparação de entrevista de backend sênior
(Java 17, Spring Boot, microsserviços, DDD, mensageria, observabilidade).

Veja [`docs/architecture.md`](docs/architecture.md) para o desenho completo dos
bounded contexts e da saga.

## Estrutura

```
travel-packages-platform/
├── package-service/     # orquestrador do pacote (saga) — em construção
├── docs/                 # arquitetura, ADRs
├── infra/docker/         # docker-compose (Postgres, RabbitMQ)
└── pom.xml               # aggregator Maven do monorepo
```

## Rodando localmente

1. Suba a infra (Postgres + RabbitMQ):
   ```bash
   cd infra/docker
   docker compose up -d
   ```

2. Rode o `package-service` (ainda não tem Maven Wrapper configurado, use o `mvn` da sua máquina — Java 17+ e Maven 3.9+):
   ```bash
   cd package-service
   mvn spring-boot:run
   ```

3. Teste o endpoint:
   ```bash
   curl -X POST http://localhost:8081/api/packages \
     -H "Content-Type: application/json" \
     -d '{
       "customerId": "customer-123",
       "flight": {
         "offerId": "fl-8890", "origin": "FOR", "destination": "LIS",
         "departureDate": "2026-10-10", "returnDate": "2026-10-20", "passengers": 2
       },
       "hotel": {
         "offerId": "ht-4521", "checkIn": "2026-10-10", "checkOut": "2026-10-20",
         "roomType": "DOUBLE", "guests": 2
       },
       "paymentMethod": { "type": "CREDIT_CARD", "installments": 3 }
     }'
   ```

## Rodando os testes

```bash
cd package-service
mvn test
```

Os testes de integração usam **Testcontainers** — é necessário ter Docker
rodando localmente.

## Roadmap do projeto

Ver checklist em [`docs/architecture.md`](docs/architecture.md#status-atual-do-projeto).
