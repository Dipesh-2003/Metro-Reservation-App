package com.aurionpro.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurionpro.app.entity.Station;

public interface StationRepository extends JpaRepository<Station, Integer> {
    List<Station> findAllByActiveTrue();
}