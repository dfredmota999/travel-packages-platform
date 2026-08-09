package com.travelplatform.hotelservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.travelplatform.hotelservice.web.dto.CreateHotelOfferRequest;
import com.travelplatform.hotelservice.web.dto.CreateReservationRequest;
import com.travelplatform.hotelservice.web.dto.HotelOfferResponse;
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
class HotelOfferControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("hotel_service_test")
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
    void deveCriarOfertaEReservarQuarto() {
        HotelOfferResponse offer = createOfferWithRooms(1);

        CreateReservationRequest reservationRequest = new CreateReservationRequest("customer-123", 2);
        ResponseEntity<CreateReservationRequest.Response> reservationResponse = restTemplate.postForEntity(
                "/api/hotels/" + offer.id() + "/reservations", reservationRequest, CreateReservationRequest.Response.class);

        assertThat(reservationResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reservationResponse.getBody().status().toString()).isEqualTo("CONFIRMED");

        ResponseEntity<HotelOfferResponse> updatedOffer = restTemplate.getForEntity(
                "/api/hotels/" + offer.id(), HotelOfferResponse.class);
        assertThat(updatedOffer.getBody().availableRooms()).isZero();
    }

    @Test
    void deveRetornar409QuandoNaoHaQuartosSuficientes() {
        HotelOfferResponse offer = createOfferWithRooms(1);
        restTemplate.postForEntity("/api/hotels/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-1", 2), CreateReservationRequest.Response.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/hotels/" + offer.id() + "/reservations",
                new CreateReservationRequest("customer-2", 2), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("quartos suficientes");
    }

    private HotelOfferResponse createOfferWithRooms(int totalRooms) {
        CreateHotelOfferRequest request = new CreateHotelOfferRequest(
                "Hotel Lisboa Centro", "Lisboa", "DOUBLE",
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 20),
                new BigDecimal("450.00"), totalRooms);

        ResponseEntity<HotelOfferResponse> response =
                restTemplate.postForEntity("/api/hotels", request, HotelOfferResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
