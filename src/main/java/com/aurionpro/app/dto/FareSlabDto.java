package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareSlabDto {
    private Integer slabId;
    private Integer minStations;
    private Integer maxStations;
    private BigDecimal fare;
}