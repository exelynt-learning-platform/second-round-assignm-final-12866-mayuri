package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find all payments for a specific order
     */
    List<Payment> findByOrderId(Long orderId);

    /**
     * Find all payments by status
     */
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    /**
     * Find payment by payment method
     */
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);

    /**
     * Find payments for a specific user
     */
    List<Payment> findByOrder_UserId(Long userId);
}