package com.its.gestionepagamentirestclient.mapper;

import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import com.its.gestionepagamentirestclient.dto.PaymentResponse;
import com.its.gestionepagamentirestclient.model.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    Payment toEntity(PaymentRequest paymentRequest);
    PaymentResponse toResponse(Payment payment);
}
