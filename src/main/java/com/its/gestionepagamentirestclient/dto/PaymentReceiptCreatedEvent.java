package com.its.gestionepagamentirestclient.dto;

import java.util.UUID;

public record PaymentReceiptCreatedEvent(
        UUID orderId,
        String receiptFileName
) {}