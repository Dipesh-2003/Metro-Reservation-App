package com.aurionpro.app.service;

import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;

import java.util.List;

public interface TicketService {
    FareResponse calculateFare(Integer originId, Integer destId);
    Ticket bookTicket(BookingRequest bookingRequest, User user);
    List<Ticket> getTicketHistory(User user);
    Ticket getTicketById(Integer id);
    Ticket cancelTicket(Integer id);
}