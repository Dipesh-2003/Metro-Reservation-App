package com.aurionpro.app.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateRouteRequest {
    private String description;
    private List<Integer> stationIds; //ordered list of station ids
}