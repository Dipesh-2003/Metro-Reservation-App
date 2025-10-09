package com.aurionpro.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
public class FareSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer slabId;

    @Column(nullable = false)
    private Integer minStations;

    @Column(nullable = false)
    private Integer maxStations;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fare;
}