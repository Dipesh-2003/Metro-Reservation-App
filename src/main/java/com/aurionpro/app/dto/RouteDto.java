package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class RouteDto {
    private Integer routeId;
    private String description;
    private String stationSequence; //comma-separated string of station ids
}