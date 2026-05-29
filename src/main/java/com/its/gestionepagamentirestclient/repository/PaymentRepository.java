package com.its.gestionepagamentirestclient.repository;

import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import com.its.gestionepagamentirestclient.model.Payment;
import com.its.gestionepagamentirestclient.model.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<PaymentRequest> findByStatus(StatusEnum status);
    List<Payment> findByOrderId(UUID orderId);
    int deleteByStatus(StatusEnum status);
}
