package com.aurionpro.app.repository;

import com.aurionpro.app.entity.FareSlab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FareSlabRepository extends JpaRepository<FareSlab, Integer> {

    @Query("SELECT fs FROM FareSlab fs WHERE :stationCount BETWEEN fs.minStations AND fs.maxStations")
    Optional<FareSlab> findFareSlabForStationCount(@Param("stationCount") int stationCount);
}