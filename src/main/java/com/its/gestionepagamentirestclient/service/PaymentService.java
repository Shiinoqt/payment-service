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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RestClient orderRestClient;

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

    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }
}