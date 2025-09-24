package com.aurionpro.app.service;

import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.User;
import java.util.List;

public interface TicketService {
    FareResponse calculateFare(Integer originId, Integer destId);
    TicketDto bookTicket(BookingRequest bookingRequest, User user);
    List<TicketDto> getTicketHistory(User user);
    
    TicketDto getTicketByIdAndUser(Integer ticketId, User user);
    TicketDto cancelTicket(Integer ticketId, User user);
}