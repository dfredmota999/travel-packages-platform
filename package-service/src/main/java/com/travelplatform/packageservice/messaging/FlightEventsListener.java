package com.travelplatform.packageservice.messaging;

import com.travelplatform.packageservice.config.RabbitMQConfig;
import com.travelplatform.packageservice.service.SagaOrchestrator;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class FlightEventsListener {

    private final SagaOrchestrator sagaOrchestrator;

    public FlightEventsListener(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @RabbitListener(queues = RabbitMQConfig.FLIGHT_RESERVED_QUEUE)
    public void onReserved(ItemReservedEvent event) {
        sagaOrchestrator.onFlightReserved(event.packageId(), event.reservationId());
    }

    @RabbitListener(queues = RabbitMQConfig.FLIGHT_REJECTED_QUEUE)
    public void onRejected(ItemRejectedEvent event) {
        sagaOrchestrator.onFlightRejected(event.packageId());
    }

    @RabbitListener(queues = RabbitMQConfig.FLIGHT_CANCELLED_QUEUE)
    public void onCancelled(ItemCancelledEvent event) {
        sagaOrchestrator.onFlightCancelled(event.packageId());
    }
}
