package com.ecommerce.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.repository.OrderRepository;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private com.ecommerce.repository.PaymentRepository paymentRepository;

    public PaymentDTO processPayment(PaymentDTO paymentDTO) throws StripeException {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Stripe.apiKey = stripeApiKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalPrice().multiply(new java.math.BigDecimal(100)).longValue())
                .setCurrency("usd")
                .setDescription("Order #" + order.getId())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(Payment.PaymentMethod.STRIPE)
                .stripePaymentIntentId(intent.getId())
                .amount(order.getTotalPrice())
                .paymentStatus(Payment.PaymentStatus.PROCESSING)
                .transactionId(UUID.randomUUID().toString())
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build();

        order.setPayment(payment);
        orderRepository.save(order);

        return new PaymentDTO(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod().toString(),
                payment.getStripePaymentIntentId(),
                payment.getPaymentStatus().toString(),
                payment.getTransactionId()
        );
    }

    public void handlePaymentSuccess(String paymentIntentId) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> o.getPayment() != null &&
                        o.getPayment().getStripePaymentIntentId().equals(paymentIntentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order with payment intent not found"));

        Payment payment = order.getPayment();
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        order.setStatus(Order.OrderStatus.PROCESSING);
        orderRepository.save(order);
    }

    public void handlePaymentFailure(String paymentIntentId) {
        Order order = orderRepository.findAll().stream()
                .filter(o -> o.getPayment() != null &&
                        o.getPayment().getStripePaymentIntentId().equals(paymentIntentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order with payment intent not found"));

        Payment payment = order.getPayment();
        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}