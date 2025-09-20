package com.aurionpro.app.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.aurionpro.app.common.TicketStatus;
import com.aurionpro.app.common.TicketType;

import lombok.Data;

@Data
public class TicketDto {
    private Integer ticketId;
    private String ticketNumber;
    private String originStationName;
    private String destinationStationName;
    private BigDecimal fare;
    private Instant bookingTime;
    private Instant expiryTime;
    private TicketStatus status;
    private TicketType ticketType;
    private String qrCodePayload;
}
