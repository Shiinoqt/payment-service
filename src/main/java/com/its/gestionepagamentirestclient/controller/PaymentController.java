package com.its.gestionepagamentirestclient.controller;

import com.its.gestionepagamentirestclient.dto.PaymentRequest;
import com.its.gestionepagamentirestclient.dto.PaymentResponse;
import com.its.gestionepagamentirestclient.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<PaymentResponse> process(@Valid @RequestBody PaymentRequest paymentRequest) {
        PaymentResponse response = paymentService.processPayment(paymentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(path = "/orders/{id}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable UUID id) {
        List<PaymentResponse> responses = paymentService.getPaymentsByOrderId(id);
        return ResponseEntity.ok(responses);
    }
}