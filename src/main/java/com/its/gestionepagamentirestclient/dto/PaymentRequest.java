package com.its.gestionepagamentirestclient.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    @PositiveOrZero(message = "Total must be zero or positive")
    private BigDecimal amount;
}
