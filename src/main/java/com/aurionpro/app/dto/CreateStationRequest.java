package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class CreateStationRequest {
    private String name;
    private String code;
    private Integer stationOrder;
}