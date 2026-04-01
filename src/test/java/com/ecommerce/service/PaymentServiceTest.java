package com.ecommerce.service;

import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ecommerce.dto.PaymentDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;
    private PaymentDTO paymentDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        order = Order.builder()
                .id(1L)
                .totalPrice(new BigDecimal("199.98"))
                .status(Order.OrderStatus.PENDING)
                .build();

        paymentDTO = new PaymentDTO();
        paymentDTO.setOrderId(1L);
        paymentDTO.setAmount(new BigDecimal("199.98"));
        paymentDTO.setPaymentMethod("STRIPE");
    }

    @Test
    public void testHandlePaymentSuccess() {
        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .stripePaymentIntentId("pi_test123")
                .paymentStatus(Payment.PaymentStatus.PROCESSING)
                .build();
        order.setPayment(payment);

        when(orderRepository.findAll()).thenReturn(java.util.List.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        paymentService.handlePaymentSuccess("pi_test123");

        assertEquals(Payment.PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertEquals(Order.OrderStatus.PROCESSING, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testHandlePaymentFailure() {
        Payment payment = Payment.builder()
                .id(1L)
                .order(order)
                .stripePaymentIntentId("pi_test123")
                .paymentStatus(Payment.PaymentStatus.PROCESSING)
                .build();
        order.setPayment(payment);

        when(orderRepository.findAll()).thenReturn(java.util.List.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        paymentService.handlePaymentFailure("pi_test123");

        assertEquals(Payment.PaymentStatus.FAILED, payment.getPaymentStatus());
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}