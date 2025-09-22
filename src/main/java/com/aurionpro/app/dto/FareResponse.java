package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FareResponse {
    private BigDecimal fare;
}
