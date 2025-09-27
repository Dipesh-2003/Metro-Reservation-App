package com.aurionpro.app.dto;

import com.aurionpro.app.common.ValidationType;
import lombok.Data;

import java.time.Instant;

@Data
public class ValidationHistoryDto {
    private Integer validationId;
    private Integer ticketId;
    private String stationName; //assuming validation is linked to a station
    private Instant validationTime;
    private ValidationType validationType;
    private boolean isValid;
}