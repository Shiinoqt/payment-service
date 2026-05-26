package com.its.gestionepagamentirestclient.service;

import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import com.its.gestionepagamentirestclient.dto.PaymentResponse;
import com.its.gestionepagamentirestclient.exception.PaymentFailedException;
import com.its.gestionepagamentirestclient.mapper.PaymentMapper;
import com.its.gestionepagamentirestclient.model.Payment;
import com.its.gestionepagamentirestclient.model.StatusEnum;
import com.its.gestionepagamentirestclient.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class responsible for managing transaction processing and payment records.
 * It simulates a payment gateway transaction using a randomized outcome and synchronizes
 * successful transactions back to the external order microservice.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    /**
     * Repository handling database persistence operations for {@link Payment} entities.
     */
    private final PaymentRepository paymentRepository;

    /**
     * Mapper component used to convert between Payment entities and DTOs.
     */
    private final PaymentMapper paymentMapper;

    /**
     * REST client utilized to communicate asynchronously or synchronously back to the Order Service.
     */
    private final RestClient orderRestClient;

    /**
     * Processes an incoming payment request.
     * <p>
     * This method simulates an external credit card gateway processing logic by randomly
     * accepting or declining the transaction via {@link ThreadLocalRandom}.
     * </p>
     * <ul>
     * <li>If declined, the record is stored as {@link StatusEnum#DECLINED} and an exception is thrown.</li>
     * <li>If accepted, the record is stored as {@link StatusEnum#ACCEPTED} and a synchronization HTTP call
     * is triggered to update the corresponding order's status to {@code PAID}.</li>
     * </ul>
     *
     * @param request the {@link PaymentRequest} payload containing transaction details and the related order ID
     * @return the {@link PaymentResponse} payload mirroring the finalized database state
     * @throws PaymentFailedException if the transaction is declined by the gateway, or if the gateway succeeds
     * but the subsequent Order microservice webhook sync fails
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);

        boolean isAccepted = ThreadLocalRandom.current().nextBoolean();

        if (!isAccepted) {
            payment.setStatus(StatusEnum.DECLINED);
            paymentRepository.save(payment);

            throw new PaymentFailedException("Transaction declined by the credit card issuer.");
        }

        payment.setStatus(StatusEnum.ACCEPTED);
        Payment savedPayment = paymentRepository.save(payment);

        try {
            orderRestClient.put()
                    .uri("/orders/{id}/status?status={status}", savedPayment.getOrderId(), "PAID")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new PaymentFailedException("Payment succeeded, but failed to sync with Order Service: " + e.getMessage());
        }

        return paymentMapper.toResponse(savedPayment);
    }

    /**
     * Retrieves all transaction records mapped to a specific order identifier.
     *
     * @param orderId the unique {@link UUID} of the order whose payment records are being audited
     * @return a {@link List} of {@link PaymentResponse} elements representing the history of payments for the order
     */
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}