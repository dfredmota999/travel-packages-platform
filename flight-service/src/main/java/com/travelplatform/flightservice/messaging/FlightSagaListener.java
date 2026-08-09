package com.travelplatform.flightservice.messaging;

import com.travelplatform.flightservice.config.RabbitMQConfig;
import com.travelplatform.flightservice.domain.FlightReservation;
import com.travelplatform.flightservice.domain.InsufficientAvailabilityException;
import com.travelplatform.flightservice.exception.FlightOfferNotFoundException;
import com.travelplatform.flightservice.service.FlightOfferService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Consome os comandos da saga e reage reutilizando exatamente a mesma lógica
 * de negócio (FlightOfferService) que o endpoint REST já usa — a saga é só
 * outra porta de entrada, a regra de negócio não muda.
 *
 * Erros de NEGÓCIO (sem assentos, oferta não existe) viram evento "rejected"
 * — não deixamos a exception subir, porque isso faria o RabbitMQ recolocar a
 * mensagem na fila pra tentar de novo (retry), e reservar-sem-assento não vai
 * ficar diferente na segunda tentativa. Erros de INFRA (banco fora, etc.)
 * continuariam subindo normalmente e o RabbitMQ faria retry — esse é o
 * comportamento certo pra cada tipo de falha.
 */
@Component
public class FlightSagaListener {

    private static final Logger log = LoggerFactory.getLogger(FlightSagaListener.class);

    private final FlightOfferService service;
    private final RabbitTemplate rabbitTemplate;

    public FlightSagaListener(FlightOfferService service, RabbitTemplate rabbitTemplate) {
        this.service = service;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.RESERVE_QUEUE)
    public void onReserve(ReserveItemCommand command) {
        try {
            FlightReservation reservation = service.reserveSeats(
                    UUID.fromString(command.offerId()), command.customerId(), command.quantity());
            publishEvent("flight.reserved",
                    new ItemReservedEvent(command.packageId(), command.offerId(), reservation.getId().toString()));
        } catch (InsufficientAvailabilityException | FlightOfferNotFoundException e) {
            log.info("Reserva de voo rejeitada para pacote {}: {}", command.packageId(), e.getMessage());
            publishEvent("flight.rejected",
                    new ItemRejectedEvent(command.packageId(), command.offerId(), e.getMessage()));
        }
    }

    @RabbitListener(queues = RabbitMQConfig.CANCEL_QUEUE)
    public void onCancel(CancelItemCommand command) {
        service.cancelReservation(UUID.fromString(command.reservationId()));
        publishEvent("flight.cancelled", new ItemCancelledEvent(command.packageId(), command.reservationId()));
    }

    private void publishEvent(String routingKey, Object event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, routingKey, event);
    }
}
