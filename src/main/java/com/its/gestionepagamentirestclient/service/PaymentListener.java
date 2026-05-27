package com.its.gestionepagamentirestclient.service;

import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Message listener component responsible for consuming asynchronous payment tasks
 * from RabbitMQ.
 * <p>
 * This class acts as the entry point for incoming messages from the message broker,
 * delegating the actual business logic execution to the {@link PaymentService}.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;

    /**
     * Consumes messages from the designated payment queue and triggers the payment
     * processing workflow.
     * <p>
     * The payload is automatically deserialized from JSON into a {@link PaymentRequest}
     * object before this method is invoked.
     * </p>
     *
     * @param request the deserialized payment request payload containing transaction details
     */
    @RabbitListener(queues = "queue-payment")
    public void receivePayment(PaymentRequest request) {
        paymentService.processPayment(request);
    }
}