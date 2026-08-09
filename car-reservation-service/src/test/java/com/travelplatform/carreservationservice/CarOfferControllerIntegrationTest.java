package com.travelplatform.carreservationservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelplatform.carreservationservice.web.dto.CarOfferResponse;
import com.travelplatform.carreservationservice.web.dto.CreateCarOfferRequest;
import com.travelplatform.carreservationservice.web.dto.CreateReservationRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarOfferControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("car_reservation_service_test")
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
    void deveCriarOfertaEReservarVeiculo() {
        CarOfferResponse offer = createOfferWithUnits(1);

        ResponseEntity<CreateReservationRequest.Response> reservationResponse = restTemplate.postForEntity(
                "/api/cars/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-123"), CreateReservationRequest.Response.class);

        assertThat(reservationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservationResponse.getBody().status().toString()).isEqualTo("CONFIRMED");

        ResponseEntity<CarOfferResponse> updatedOffer = restTemplate.getForEntity(
                "/api/cars/" + offer.id(), CarOfferResponse.class);
        assertThat(updatedOffer.getBody().availableUnits()).isZero();
    }

    @Test
    void deveRetornar409QuandoNaoHaVeiculosSuficientes() {
        CarOfferResponse offer = createOfferWithUnits(1);
        restTemplate.postForEntity("/api/cars/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-1"), CreateReservationRequest.Response.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/cars/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-2"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("veículos suficientes");
    }

    private CarOfferResponse createOfferWithUnits(int totalUnits) {
        CreateCarOfferRequest request = new CreateCarOfferRequest(
                "ECONOMY", "Fiat Argo", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20),
                new BigDecimal("150.00"), totalUnits);

        ResponseEntity<CarOfferResponse> response =
                restTemplate.postForEntity("/api/cars", request, CarOfferResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
