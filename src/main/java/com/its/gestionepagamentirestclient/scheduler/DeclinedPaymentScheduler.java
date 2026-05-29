package com.its.gestionepagamentirestclient.scheduler;

import com.its.gestionepagamentirestclient.model.StatusEnum;
import com.its.gestionepagamentirestclient.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeclinedPaymentScheduler {
    private final PaymentRepository paymentRepository;

    @Scheduled(initialDelay = 10000, fixedRate = 300000)
    @Transactional
    public void cleanDeclinedPayments() {
        log.info("Starting cleanup of declined payments...");
        try {
            int deletedCount = paymentRepository.deleteByStatus(StatusEnum.DECLINED);
            if (deletedCount > 0) {
                log.info("Declined payments have been cleaned. Total removed: {}", deletedCount);
            } else {
                log.info("No declined payments found to clean.");
            }
        } catch (Exception e) {
            log.error("Failed to execute declined payment cleanup", e);
        }
    }
}
