package com.bankapp.payment.repository;

import com.bankapp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReference(String reference);

    boolean existsByReference(String reference);

    List<Payment> findByOwnerIdOrderByCreatedAtDesc(String ownerId);
}
