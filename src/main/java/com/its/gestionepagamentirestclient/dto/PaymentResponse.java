package com.its.gestionepagamentirestclient.dto;

import com.its.gestionepagamentirestclient.model.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID orderId;
    private UUID transactionId;
    private String email;
    private BigDecimal amount;
    @NotNull
    private StatusEnum status;
    private String receipt;
}
