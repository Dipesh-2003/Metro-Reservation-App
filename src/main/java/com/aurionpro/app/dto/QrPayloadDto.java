package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class QrPayloadDto {
    private String ticketNumber;
    private Integer userId;
    private String origin;
    private String destination;
}