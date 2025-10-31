package com.aurionpro.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.User;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByUser(User user);

}