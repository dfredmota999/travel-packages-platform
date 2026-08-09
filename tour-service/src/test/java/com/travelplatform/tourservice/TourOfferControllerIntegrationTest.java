package com.travelplatform.tourservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelplatform.tourservice.web.dto.CreateReservationRequest;
import com.travelplatform.tourservice.web.dto.CreateTourOfferRequest;
import com.travelplatform.tourservice.web.dto.TourOfferResponse;
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
class TourOfferControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("tour_service_test")
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
    void deveCriarOfertaEReservarVagas() {
        TourOfferResponse offer = createOfferWithSlots(2);

        ResponseEntity<CreateReservationRequest.Response> reservationResponse = restTemplate.postForEntity(
                "/api/tours/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-123", 2), CreateReservationRequest.Response.class);

        assertThat(reservationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservationResponse.getBody().status().toString()).isEqualTo("CONFIRMED");

        ResponseEntity<TourOfferResponse> updatedOffer = restTemplate.getForEntity(
                "/api/tours/" + offer.id(), TourOfferResponse.class);
        assertThat(updatedOffer.getBody().availableSlots()).isZero();
    }

    @Test
    void deveRetornar409QuandoNaoHaVagasSuficientes() {
        TourOfferResponse offer = createOfferWithSlots(1);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/tours/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-456", 2), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("vagas suficientes");
    }

    private TourOfferResponse createOfferWithSlots(int totalSlots) {
        CreateTourOfferRequest request = new CreateTourOfferRequest(
                "Passeio de barco em Sintra", "Sintra", LocalDate.of(2026, 10, 15),
                new BigDecimal("120.00"), totalSlots);

        ResponseEntity<TourOfferResponse> response =
                restTemplate.postForEntity("/api/tours", request, TourOfferResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
