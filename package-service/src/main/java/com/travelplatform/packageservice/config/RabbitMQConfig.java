package com.travelplatform.packageservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia da saga: dois exchanges topic — um pra comandos (orquestrador →
 * serviços de produto) e um pra eventos (serviços de produto → orquestrador).
 * Cada serviço declara o que precisa de forma independente (self-contained) —
 * declarar um exchange/queue que já existe com a mesma config é idempotente,
 * não dá erro.
 *
 * Routing keys usadas aqui: "flight.reserve" / "flight.cancel" (comandos) e
 * "flight.reserved" / "flight.rejected" / "flight.cancelled" (eventos).
 * Quando ligarmos hotel/car/tour, é só repetir o padrão com o prefixo certo.
 */
@Configuration
public class RabbitMQConfig {

    public static final String COMMANDS_EXCHANGE = "saga.commands";
    public static final String EVENTS_EXCHANGE = "saga.events";

    // filas que este serviço (package-service) CONSOME — eventos vindos do flight-service
    public static final String FLIGHT_RESERVED_QUEUE = "package.flight-reserved.queue";
    public static final String FLIGHT_REJECTED_QUEUE = "package.flight-rejected.queue";
    public static final String FLIGHT_CANCELLED_QUEUE = "package.flight-cancelled.queue";

    @Bean
    public TopicExchange commandsExchange() {
        return new TopicExchange(COMMANDS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue flightReservedQueue() {
        return new Queue(FLIGHT_RESERVED_QUEUE, true);
    }

    @Bean
    public Queue flightRejectedQueue() {
        return new Queue(FLIGHT_REJECTED_QUEUE, true);
    }

    @Bean
    public Queue flightCancelledQueue() {
        return new Queue(FLIGHT_CANCELLED_QUEUE, true);
    }

    @Bean
    public Binding flightReservedBinding(Queue flightReservedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(flightReservedQueue).to(eventsExchange).with("flight.reserved");
    }

    @Bean
    public Binding flightRejectedBinding(Queue flightRejectedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(flightRejectedQueue).to(eventsExchange).with("flight.rejected");
    }

    @Bean
    public Binding flightCancelledBinding(Queue flightCancelledQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(flightCancelledQueue).to(eventsExchange).with("flight.cancelled");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
