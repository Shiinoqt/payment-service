package com.its.gestionepagamentirestclient.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    private UUID orderId;

    private BigDecimal amount;

    private StatusEnum status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDate creation;
}
