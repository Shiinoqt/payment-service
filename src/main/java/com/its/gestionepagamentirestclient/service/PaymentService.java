package com.its.gestionepagamentirestclient.service;

import com.its.gestionepagamentirestclient.config.RabbitMQConfig;
import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import com.its.gestionepagamentirestclient.dto.PaymentResponse;
import com.its.gestionepagamentirestclient.exception.PaymentFailedException;
import com.its.gestionepagamentirestclient.mapper.PaymentMapper;
import com.its.gestionepagamentirestclient.model.Payment;
import com.its.gestionepagamentirestclient.model.StatusEnum;
import com.its.gestionepagamentirestclient.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class handling the core business logic for payment processing.
 *
 * This service manages the execution workflow of incoming payment requests, orchestrates
 * entity persistence, updates transaction statuses randomly for simulation purposes,
 * and broadcasts transaction result payloads back to RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RabbitTemplate rabbitTemplate;

    public PaymentResponse processPayment(PaymentRequest request) {
        return processPayment(request, null);
    }

    /**
     * Processes an incoming payment request by mapping it to a database entity,
     * simulating a bank authorization check, persisting the result, and publishing
     * the update event.
     *
     * The payment outcome is randomly decided to simulate processing.
     *
     * @param request the incoming payment request
     * @param correlationId correlation identifier propagated from the upstream service
     */
    public PaymentResponse processPayment(PaymentRequest request, String correlationId) {
        try {
            Payment payment = buildPayment(request);
            StatusEnum finalStatus = resolvePaymentStatus();

            payment.setStatus(finalStatus);
            Payment savedPayment = paymentRepository.save(payment);

            String receiptFilename = createReceiptFile(
                    MDC.get("caller"),
                    savedPayment.getCreation(),
                    savedPayment.getAmount()
            );
            savedPayment.setReceipt(receiptFilename);
            paymentRepository.save(savedPayment);

            PaymentResponse response = paymentMapper.toResponse(savedPayment);
            log.info("event=payment_processed orderId={} paymentStatus={}",
                    response.getOrderId(), response.getStatus());

            publishPaymentResult(response, correlationId);
            return response;
        } catch (Exception e) {
            log.error("event=payment_processing_failed orderId={}", request.getOrderId(), e);
            throw new PaymentFailedException("Failed to process payment for order " + request.getOrderId());
        }
    }

    private Payment buildPayment(PaymentRequest request) {
        return Payment.builder()
                .orderId(request.getOrderId())
                .email(request.getEmail())
                .amount(request.getAmount())
                .build();
    }

    private StatusEnum resolvePaymentStatus() {
        return ThreadLocalRandom.current().nextBoolean()
                ? StatusEnum.ACCEPTED
                : StatusEnum.DECLINED;
    }

    private void publishPaymentResult(PaymentResponse response, String correlationId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_RESULTS_EXCHANGE,
                RabbitMQConfig.PAYMENT_RESULTS_ROUTING_KEY,
                response,
                message -> {
                    MessageProperties props = message.getMessageProperties();

                    setHeaderIfPresent(props, "X-Correlation-Id", correlationId);
                    setHeaderIfPresent(props, "caller", defaultIfBlank(MDC.get("caller")));
                    setHeaderIfPresent(props, "method", "AMQP");
                    setHeaderIfPresent(props, "uri", RabbitMQConfig.PAYMENT_RESULTS_QUEUE);

                    return message;
                }
        );

        log.info("event=payment_result_published orderId={} paymentStatus={}",
                response.getOrderId(), response.getStatus());
    }

    private void setHeaderIfPresent(MessageProperties props, String key, String value) {
        if (value != null && !value.isBlank()) {
            props.setHeader(key, value);
        }
    }

    private String defaultIfBlank(String value) {
        return (value == null || value.isBlank()) ? "payment_service" : value;
    }

    /**
     * Returns all persisted payments.
     *
     * @return list of payment response DTOs
     */
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    /**
     * Returns payments associated with a specific order.
     *
     * @param orderId the order identifier
     * @return list of matching payment response DTOs
     */
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    public String createReceiptFile(String userId, LocalDate date, BigDecimal amount) throws IOException {
        String filename = "receipt_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".txt";

        String content = userId + " at " + date + " paid " + amount;

        Path receiptsDir = Paths.get(System.getProperty("user.home")).resolve("receipts");
        if (!Files.exists(receiptsDir)) {
            Files.createDirectories(receiptsDir);
        }

        Files.write(receiptsDir.resolve(filename), content.getBytes());
        log.info("Receipt file created: {}", filename);

        return filename; // <-- now returned
    }

    public void attachReceipt(UUID orderId, String receiptFileName) {
        paymentRepository.findByOrderId(orderId)
                .stream()
                .filter(p -> p.getStatus() == StatusEnum.ACCEPTED)
                .findFirst()
                .ifPresentOrElse(
                        payment -> {
                            payment.setReceipt(receiptFileName);
                            paymentRepository.save(payment);
                            log.info("event=receipt_attached orderId={} fileName={}",
                                    orderId, receiptFileName);
                        },
                        () -> log.warn("event=receipt_attach_no_payment orderId={}", orderId)
                );
    }
}