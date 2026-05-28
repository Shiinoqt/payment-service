package com.its.gestionepagamentirestclient.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ messaging setup.
 * <p>
 * This class defines the necessary beans for configuring queues, exchanges,
 * bindings, message serialization, and connection templates required for
 * handling payment-related asynchronous communication.
 * </p>
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Configures a durable queue for receiving payment requests.
     *
     * @return a durable {@link Queue} named "queue-payment"
     */
    @Bean
    public Queue paymentQueue() {
        return new Queue("queue-payment", true);
    }

    /**
     * Declare the orders exchange here too so the topology exists regardless
     * of which service starts first. RabbitMQ is idempotent on declarations.
     *
     * @return a {@link DirectExchange} named "exchange-orders"
     */
    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange("exchange-orders");
    }

    /**
     * Binds the payment queue to the orders exchange using a specific routing key.
     *
     * @param paymentQueue    the payment queue to bind
     * @param ordersExchange   the source orders exchange
     * @return a {@link Binding} connecting the queue to the exchange via the "order.payment.request" routing key
     */
    @Bean
    public Binding paymentQueueBinding(Queue paymentQueue, DirectExchange ordersExchange) {
        return BindingBuilder.bind(paymentQueue)
                .to(ordersExchange)
                .with("order.payment.request");
    }

    /**
     * Configures the exchange used to publish the results of payment processing.
     *
     * @return a {@link DirectExchange} named "exchange-payment-results"
     */
    @Bean
    public DirectExchange paymentResultsExchange() {
        return new DirectExchange("exchange-payment-results");
    }

    /**
     * Configures the message converter to handle JSON serialization and deserialization.
     *
     * @return a {@link MessageConverter} backed by Jackson for JSON processing
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate used for sending messages to RabbitMQ.
     *
     * @param connectionFactory    the underlying connection factory to use
     * @param jsonMessageConverter the converter to automatically serialize payloads to JSON
     * @return a fully configured {@link RabbitTemplate}
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    /**
     * Configures the container factory responsible for initializing listeners
     * consuming messages from RabbitMQ.
     *
     * @param connectionFactory    the underlying connection factory to use
     * @param jsonMessageConverter the converter to automatically deserialize JSON payloads
     * @return a configured {@link SimpleRabbitListenerContainerFactory}
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}