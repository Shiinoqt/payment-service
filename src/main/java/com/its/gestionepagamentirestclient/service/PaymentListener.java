package com.its.gestionepagamentirestclient.service;

import com.its.gestionepagamentirestclient.config.RabbitMQConfig;
import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Message listener component responsible for consuming asynchronous payment tasks
 * from RabbitMQ.
 *
 * This class acts as the entry point for incoming messages from the message broker,
 * delegating the actual business logic execution to the {@link PaymentService}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentListener {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String CALLER_HEADER = "caller";
    private static final String METHOD_HEADER = "method";
    private static final String URI_HEADER = "uri";

    private final PaymentService paymentService;

    /**
     * Consumes messages from the designated payment queue and triggers the payment
     * processing workflow.
     *
     * The payload is automatically deserialized from JSON into a {@link PaymentRequest}
     * object before this method is invoked.
     *
     * @param request the deserialized payment request payload containing transaction details
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void receivePayment(PaymentRequest request, Message message) {
        restoreMdc(message);

        try {
            log.info("event=payment_request_received orderId={}", request.getOrderId());
            paymentService.processPayment(request, header(message.getMessageProperties(), CORRELATION_HEADER));
        } finally {
            MDC.clear();
        }
    }

    private void restoreMdc(Message message) {
        MessageProperties props = message.getMessageProperties();

        putIfPresent("correlationId", header(props, CORRELATION_HEADER));
        putIfPresent("caller", defaultIfBlank(header(props, CALLER_HEADER), "rabbitmq"));
        putIfPresent("method", defaultIfBlank(header(props, METHOD_HEADER), "AMQP"));
        putIfPresent("uri", defaultIfBlank(header(props, URI_HEADER), props.getConsumerQueue()));
    }

    private String header(MessageProperties props, String name) {
        Object value = props.getHeaders().get(name);
        return value != null ? value.toString() : null;
    }

    private void putIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            MDC.put(key, value);
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}