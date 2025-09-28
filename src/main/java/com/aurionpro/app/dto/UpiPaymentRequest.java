package com.aurionpro.app.dto;

import com.aurionpro.app.common.TicketType;
import lombok.Data;

@Data
public class UpiPaymentRequest {
    private Integer originStationId;
    private Integer destinationStationId;
    private TicketType ticketType;
}