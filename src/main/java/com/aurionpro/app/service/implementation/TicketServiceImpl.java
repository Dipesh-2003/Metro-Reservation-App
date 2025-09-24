package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.PaymentMethod;
import com.aurionpro.app.common.PaymentStatus;
import com.aurionpro.app.common.TicketStatus;
import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.*;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.TicketMapper;
import com.aurionpro.app.repository.*;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private FareRuleRepository fareRuleRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public FareResponse calculateFare(Integer originId, Integer destId) {
        Station origin = stationRepository.findById(originId)
                .orElseThrow(() -> new ResourceNotFoundException("Origin station not found"));
        Station destination = stationRepository.findById(destId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found"));

        FareRule fareRule = fareRuleRepository.findByOriginStationAndDestinationStation(origin, destination)
                .orElseThrow(() -> new ResourceNotFoundException("Fare rule not found for the selected route"));

        return new FareResponse(fareRule.getFare());
    }

    @Override
    @Transactional
    public TicketDto bookTicket(BookingRequest bookingRequest, User user) {
        BigDecimal fare = calculateFare(bookingRequest.getOriginStationId(), bookingRequest.getDestinationStationId()).getFare();

        //create and save the Payment entity
        Payment payment = new Payment();
        payment.setAmount(fare);
        payment.setPaymentMethod(bookingRequest.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING); //initial status
        payment.setCreatedAt(Instant.now());

        //handle payment logic
        if (bookingRequest.getPaymentMethod() == PaymentMethod.WALLET) {
            Wallet userWallet = walletService.getWalletByUser(user);
            walletService.debit(userWallet, fare); // this will throw InsufficientFundsException if needed
            payment.setStatus(PaymentStatus.COMPLETED);
        } else {
            // Logic for UPI payment would go here (e.g., call a payment gateway)
            // For now, we'll assume it's completed for simulation purposes.
             payment.setStatus(PaymentStatus.COMPLETED);
        }

        Payment savedPayment = paymentRepository.save(payment);


        // if payment is successful, create the ticket
        Station origin = stationRepository.findById(bookingRequest.getOriginStationId()).get();
        Station destination = stationRepository.findById(bookingRequest.getDestinationStationId()).get();

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setOriginStation(origin);
        ticket.setDestinationStation(destination);
        ticket.setFare(fare);
        ticket.setTicketType(bookingRequest.getTicketType());
        ticket.setPayment(savedPayment);
        ticket.setTicketNumber(UUID.randomUUID().toString());
        ticket.setBookingTime(Instant.now());
        ticket.setIssueDate(LocalDate.now());
        ticket.setExpiryTime(Instant.now().plus(24, ChronoUnit.HOURS)); // 24-hour validity
        ticket.setStatus(TicketStatus.CONFIRMED);
        ticket.setQrCodePayload("ticketId:" + ticket.getTicketNumber()); // Simplified QR payload
        
        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.entityToDto(savedTicket); // <-- Use mapper

    }

    @Override
    public List<TicketDto> getTicketHistory(User user) {
        List<Ticket> tickets = ticketRepository.findByUserOrderByBookingTimeDesc(user);
        return ticketMapper.entityToDto(tickets);
    }    

    @Override
    public TicketDto getTicketById(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
        return ticketMapper.entityToDto(ticket);
    }
    
    @Override
    @Transactional
    public TicketDto cancelTicket(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));

        // ... (your cancellation logic)
        
        Ticket cancelledTicket = ticketRepository.save(ticket);
        return ticketMapper.entityToDto(cancelledTicket);
    }
}