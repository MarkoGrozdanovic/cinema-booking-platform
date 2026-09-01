package com.cinemabooking.platform.repositories;

import com.cinemabooking.platform.model.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByBookingId(Long bookingId);
}
