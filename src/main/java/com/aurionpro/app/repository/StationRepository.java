package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Integer> {
}