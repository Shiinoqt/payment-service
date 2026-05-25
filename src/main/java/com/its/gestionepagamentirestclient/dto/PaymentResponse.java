package com.its.gestionepagamentirestclient.dto;

import com.its.gestionepagamentirestclient.model.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID transactionId;

    @NotNull
    private StatusEnum status;
}
