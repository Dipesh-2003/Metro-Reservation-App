package com.aurionpro.app.service;

import java.util.List;

import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.User;

public interface TicketService {
    FareResponse calculateFare(Integer originId, Integer destId);
    TicketDto bookTicket(BookingRequest bookingRequest, User user);
    List<TicketDto> getTicketHistory(User user);
    
    TicketDto getTicketByIdAndUser(Integer ticketId, User user);
    TicketDto cancelTicket(Integer ticketId, User user);
    
    TicketDto createTicketForConfirmedPayment(BookingRequest bookingRequest, User user, Payment payment);
}