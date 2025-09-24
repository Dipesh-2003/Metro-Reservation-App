package com.aurionpro.app.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateFareRuleRequest {
    private Integer originStationId;
    private Integer destinationStationId;
    private BigDecimal fare;
}
