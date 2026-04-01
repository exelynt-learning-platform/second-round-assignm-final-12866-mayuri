package com.ecommerce.controller;

import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.service.PaymentService;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentDTO> createPaymentIntent(@Valid @RequestBody PaymentDTO paymentDTO) throws StripeException {
        PaymentDTO payment = paymentService.processPayment(paymentDTO);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload) {
        // Handle Stripe webhook for payment confirmation
        // This would typically verify the signature and process the event
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/success/{paymentIntentId}")
    public ResponseEntity<String> paymentSuccess(@PathVariable String paymentIntentId) {
        paymentService.handlePaymentSuccess(paymentIntentId);
        return ResponseEntity.ok("Payment successful");
    }

    @PostMapping("/failure/{paymentIntentId}")
    public ResponseEntity<String> paymentFailure(@PathVariable String paymentIntentId) {
        paymentService.handlePaymentFailure(paymentIntentId);
        return ResponseEntity.ok("Payment failed");
    }
}