//package com.its.gestionepagamentirestclient.scheduler;
//
//import com.its.gestionepagamentirestclient.model.Payment;
//import com.its.gestionepagamentirestclient.model.StatusEnum;
//import com.its.gestionepagamentirestclient.repository.PaymentRepository;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class DeclinedPaymentSchedulerTest {
//
//    @Autowired
//    private DeclinedPaymentScheduler scheduler;
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Test
//    void testCleanDeclinedPayments() {
//        // 1. Arrange: Save a declined payment
//        Payment declinedPayment = new Payment();
//        declinedPayment.setStatus(StatusEnum.DECLINED);
//        paymentRepository.save(declinedPayment);
//
//        // 2. Act: Force the scheduler method to run instantly
//        scheduler.cleanDeclinedPayments();
//
//        // 3. Assert: Verify the repository is now empty of that payment
//        long count = paymentRepository.count();
//        assertThat(count).isEqualTo(0);
//    }
//}