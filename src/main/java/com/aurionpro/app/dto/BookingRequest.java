package com.aurionpro.app.dto;

import com.aurionpro.app.common.PaymentMethod;
import com.aurionpro.app.common.TicketType;

import lombok.Data;

@Data
public class BookingRequest {
    private Integer originStationId;
    private Integer destinationStationId;
    private TicketType ticketType;
    private PaymentMethod paymentMethod; 
}
