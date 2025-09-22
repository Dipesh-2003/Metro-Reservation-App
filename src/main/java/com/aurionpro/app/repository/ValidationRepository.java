package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationRepository extends JpaRepository<Validation, Integer> {
}