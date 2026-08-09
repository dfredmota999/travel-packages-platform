package com.travelplatform.flightservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelplatform.flightservice.web.dto.CreateFlightOfferRequest;
import com.travelplatform.flightservice.web.dto.CreateReservationRequest;
import com.travelplatform.flightservice.web.dto.FlightOfferResponse;
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
class FlightOfferControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flight_service_test")
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
    void deveCriarOfertaEReservarAssentos() {
        FlightOfferResponse offer = createOfferWithSeats(2);

        CreateReservationRequest reservationRequest = new CreateReservationRequest("customer-123", 2);
        ResponseEntity<CreateReservationRequest.Response> reservationResponse = restTemplate.postForEntity(
                "/api/flights/" + offer.id() + "/reservations", reservationRequest, CreateReservationRequest.Response.class);

        assertThat(reservationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservationResponse.getBody().status().toString()).isEqualTo("CONFIRMED");

        ResponseEntity<FlightOfferResponse> updatedOffer = restTemplate.getForEntity(
                "/api/flights/" + offer.id(), FlightOfferResponse.class);
        assertThat(updatedOffer.getBody().availableSeats()).isZero();
    }

    @Test
    void deveRetornar409QuandoNaoHaAssentosSuficientes() {
        FlightOfferResponse offer = createOfferWithSeats(1);

        CreateReservationRequest reservationRequest = new CreateReservationRequest("customer-456", 2);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/flights/" + offer.id() + "/reservations", reservationRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("assentos suficientes");
    }

    private FlightOfferResponse createOfferWithSeats(int totalSeats) {
        CreateFlightOfferRequest request = new CreateFlightOfferRequest(
                "FOR", "LIS", LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20),
                "TAP", new BigDecimal("2500.00"), totalSeats);

        ResponseEntity<FlightOfferResponse> response =
                restTemplate.postForEntity("/api/flights", request, FlightOfferResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
