package com.travelplatform.packageservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.travelplatform.packageservice.config.RabbitMQConfig;
import com.travelplatform.packageservice.domain.PackageStatus;
import com.travelplatform.packageservice.messaging.ItemRejectedEvent;
import com.travelplatform.packageservice.messaging.ItemReservedEvent;
import com.travelplatform.packageservice.web.dto.CreatePackageRequest;
import com.travelplatform.packageservice.web.dto.PackageResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Sobe Postgres E RabbitMQ reais via Testcontainers. Como o flight-service
 * não está rodando neste teste (é um teste do package-service isoladamente),
 * simulamos a resposta dele publicando o evento diretamente no exchange —
 * o mesmo formato de mensagem que o flight-service publicaria de verdade.
 * Isso testa o listener + o SagaOrchestrator de ponta a ponta, de forma
 * assíncrona (por isso o Awaitility no lugar de assert direto).
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("package_service_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void sagaCompletaComSucessoQuandoVooEReservado() {
        UUID packageId = createPackageWithFlight("fl-offer-1");

        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "flight.reserved",
                new ItemReservedEvent(packageId, "fl-offer-1", "flight-reservation-abc"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            PackageResponse response = getPackage(packageId);
            assertThat(response.status()).isEqualTo(PackageStatus.CONFIRMED);
            assertThat(response.items().get(0).reservationId()).isEqualTo("flight-reservation-abc");
        });
    }

    @Test
    void sagaCancelaPacoteQuandoVooERejeitado() {
        UUID packageId = createPackageWithFlight("fl-offer-2");

        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, "flight.rejected",
                new ItemRejectedEvent(packageId, "fl-offer-2", "sem assentos disponíveis"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            PackageResponse response = getPackage(packageId);
            assertThat(response.status()).isEqualTo(PackageStatus.CANCELLED);
        });
    }

    private UUID createPackageWithFlight(String offerId) {
        CreatePackageRequest request = new CreatePackageRequest(
                "customer-123",
                new CreatePackageRequest.ItemRequest(offerId, 2),
                null, null, null,
                new CreatePackageRequest.PaymentRequest("CREDIT_CARD", 3));

        ResponseEntity<PackageResponse> response = restTemplate.postForEntity("/api/packages", request, PackageResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        return response.getBody().id();
    }

    private PackageResponse getPackage(UUID id) {
        return restTemplate.getForEntity("/api/packages/" + id, PackageResponse.class).getBody();
    }
}
