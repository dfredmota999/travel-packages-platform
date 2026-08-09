package com.travelplatform.packageservice.messaging;

import com.travelplatform.packageservice.config.RabbitMQConfig;
import com.travelplatform.packageservice.domain.PackageItem;
import com.travelplatform.packageservice.domain.TravelPackage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SagaCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SagaCommandPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishReserveFlight(TravelPackage travelPackage, PackageItem flightItem) {
        ReserveItemCommand command = new ReserveItemCommand(
                travelPackage.getId(), flightItem.getOfferId(), flightItem.getQuantity(), travelPackage.getCustomerId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.COMMANDS_EXCHANGE, "flight.reserve", command);
    }

    public void publishCancelFlight(TravelPackage travelPackage, String reservationId) {
        CancelItemCommand command = new CancelItemCommand(travelPackage.getId(), reservationId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.COMMANDS_EXCHANGE, "flight.cancel", command);
    }

    // TODO: publishReserveHotel/Car/Tour e publishCancelHotel/Car/Tour seguem o mesmo padrão,
    // assim que ligarmos os outros 3 serviços de produto na saga.
}
