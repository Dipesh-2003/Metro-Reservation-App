package com.aurionpro.app.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FareRuleDto {
    private Integer fareRuleId;
    private String originStationName;
    private String destinationStationName;
    private BigDecimal fare;
}
