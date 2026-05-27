package com.its.gestionepagamentirestclient.service;

import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import com.its.gestionepagamentirestclient.dto.PaymentResponse;
import com.its.gestionepagamentirestclient.exception.PaymentFailedException;
import com.its.gestionepagamentirestclient.mapper.PaymentMapper;
import com.its.gestionepagamentirestclient.model.Payment;
import com.its.gestionepagamentirestclient.model.StatusEnum;
import com.its.gestionepagamentirestclient.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class handling the core business logic for payment processing.
 * <p>
 * This service manages the execution workflow of incoming payment requests, orchestrates
 * entity persistence, updates transaction statuses randomly for simulation purposes,
 * and broadcasts transaction result payloads back to RabbitMQ.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RabbitTemplate rabbitTemplate;

    /**
     * Processes an incoming payment request by mapping it to a database entity,
     * simulating a bank authorization check, persisting the result, and publishing
     * the update event.
     * <p>
     * The payment outcome is randomly decided (50/50 chance) to simulate processing flags:
     * <ul>
     * <li><b>DECLINED:</b> Saved to the database and published without throwing errors.</li>
     * <li><b>ACCEPTED:</b> Saved to the database. If publishing the event to RabbitMQ
     * fails, a runtime exception is raised.</li>
     * </ul>
     * </p>
     *
     * @param request the {@link PaymentRequest} containing the transaction details
     * @return a {@link PaymentResponse} reflecting the final status of the transaction
     * @throws PaymentFailedException if the payment succeeded but the outcome message
     * could not be successfully broadcasted to the message broker
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        boolean isAccepted = ThreadLocalRandom.current().nextBoolean();

        if (!isAccepted) {
            payment.setStatus(StatusEnum.DECLINED);
            paymentRepository.save(payment);

            PaymentResponse declinedResponse = paymentMapper.toResponse(payment);
            sendResultToRabbitMQ(declinedResponse);

            return declinedResponse;
        }

        payment.setStatus(StatusEnum.ACCEPTED);
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponse response = paymentMapper.toResponse(savedPayment);

        try {
            sendResultToRabbitMQ(response);
        } catch (Exception e) {
            throw new PaymentFailedException("Payment succeeded, but failed to publish update: " + e.getMessage());
        }

        return response;
    }

    /**
     * Helper method to dispatch the payment response event payload to RabbitMQ.
     *
     * @param response the transaction payload to be dispatched
     */
    private void sendResultToRabbitMQ(PaymentResponse response) {
        rabbitTemplate.convertAndSend(
                "exchange-payment-results",
                "payment.status.updated",
                response
        );
    }

    /**
     * Retrieves the complete transaction history associated with a specific order.
     *
     * @param orderId the unique identifier {@link UUID} of the order
     * @return a {@link List} of {@link PaymentResponse} objects matching the requested order ID,
     * or an empty list if no transactions were found
     */
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}