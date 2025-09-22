package com.aurionpro.app.repository;

import com.aurionpro.app.entity.FareRule;
import com.aurionpro.app.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Integer> {
    Optional<FareRule> findByOriginStationAndDestinationStation(Station origin, Station destination);
}