package com.travelplatform.packageservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelplatform.packageservice.domain.PackageStatus;
import com.travelplatform.packageservice.web.dto.CreatePackageRequest;
import com.travelplatform.packageservice.web.dto.PackageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

/**
 * Sobe um Postgres real via Testcontainers em vez de usar H2 em memória —
 * garante que constraints, tipos e comportamento do driver batam com produção.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PackageControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("package_service_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void deveCriarPacoteERecuperarPorId() {
        CreatePackageRequest request = new CreatePackageRequest(
                "customer-123",
                new CreatePackageRequest.FlightRequest(
                        "fl-8890", "FOR", "LIS",
                        LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20), 2),
                new CreatePackageRequest.HotelRequest(
                        "ht-4521", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20), "DOUBLE", 2),
                null, // sem carro
                null, // sem passeio
                new CreatePackageRequest.PaymentRequest("CREDIT_CARD", 3)
        );

        ResponseEntity<PackageResponse> createResponse =
                restTemplate.postForEntity("/api/packages", request, PackageResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().status()).isEqualTo(PackageStatus.CREATED);
        assertThat(createResponse.getBody().flight().status().toString()).isEqualTo("PENDING");
        assertThat(createResponse.getBody().carRental().status().toString()).isEqualTo("NOT_REQUESTED");

        var id = createResponse.getBody().id();

        ResponseEntity<PackageResponse> getResponse =
                restTemplate.getForEntity("/api/packages/" + id, PackageResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().customerId()).isEqualTo("customer-123");
    }

    @Test
    void deveRetornarProblemDetail404QuandoPacoteNaoExiste() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/packages/" + java.util.UUID.randomUUID(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Pacote não encontrado");
    }
}
