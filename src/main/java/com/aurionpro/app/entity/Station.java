package com.aurionpro.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "station_id")
    private Integer stationId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String code;
    
    @Column(nullable = false, unique = true)
    private Integer stationOrder;

    @Column(nullable = false)
    private Boolean active = true;
}