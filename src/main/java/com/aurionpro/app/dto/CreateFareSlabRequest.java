package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateFareSlabRequest {
    private Integer minStations;
    private Integer maxStations;
    private BigDecimal fare;
}