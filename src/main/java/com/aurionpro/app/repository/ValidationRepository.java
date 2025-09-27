package com.aurionpro.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.app.entity.Validation;

public interface ValidationRepository extends JpaRepository<Validation, Integer> {
    List<Validation> findByTicketTicketIdOrderByValidationTimeDesc(Integer ticketId);

}