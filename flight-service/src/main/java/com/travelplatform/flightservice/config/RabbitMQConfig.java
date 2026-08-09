package com.travelplatform.flightservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lado do flight-service da mesma topologia declarada no package-service:
 * consome comandos ("flight.reserve" / "flight.cancel"), publica eventos de
 * resposta ("flight.reserved" / "flight.rejected" / "flight.cancelled").
 * Cada serviço declara sua parte de forma independente — declaração de
 * exchange já existente com a mesma config é idempotente.
 */
@Configuration
public class RabbitMQConfig {

    public static final String COMMANDS_EXCHANGE = "saga.commands";
    public static final String EVENTS_EXCHANGE = "saga.events";

    public static final String RESERVE_QUEUE = "flight.reserve.queue";
    public static final String CANCEL_QUEUE = "flight.cancel.queue";

    @Bean
    public TopicExchange commandsExchange() {
        return new TopicExchange(COMMANDS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue reserveQueue() {
        return new Queue(RESERVE_QUEUE, true);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(CANCEL_QUEUE, true);
    }

    @Bean
    public Binding reserveBinding(Queue reserveQueue, TopicExchange commandsExchange) {
        return BindingBuilder.bind(reserveQueue).to(commandsExchange).with("flight.reserve");
    }

    @Bean
    public Binding cancelBinding(Queue cancelQueue, TopicExchange commandsExchange) {
        return BindingBuilder.bind(cancelQueue).to(commandsExchange).with("flight.cancel");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
