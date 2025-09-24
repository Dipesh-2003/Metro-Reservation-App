package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.PaymentMethod;
import com.aurionpro.app.common.PaymentStatus;
import com.aurionpro.app.common.TicketStatus;
import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.*;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.TicketMapper;
import com.aurionpro.app.repository.*;
import com.aurionpro.app.service.QRCodeService;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final StationRepository stationRepository;
    private final FareRuleRepository fareRuleRepository;
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService; // Injected QRCodeService

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

        // 1. Handle Payment
        Payment payment = new Payment();
        payment.setAmount(fare);
        payment.setPaymentMethod(bookingRequest.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());

        if (bookingRequest.getPaymentMethod() == PaymentMethod.WALLET) {
            Wallet userWallet = walletService.getWalletByUser(user);
            walletService.debit(userWallet, fare);
            payment.setStatus(PaymentStatus.COMPLETED);
        } else {
            payment.setStatus(PaymentStatus.COMPLETED);
        }
        Payment savedPayment = paymentRepository.save(payment);

        // 2. Fetch Stations
        Station origin = stationRepository.findById(bookingRequest.getOriginStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin station not found"));
        Station destination = stationRepository.findById(bookingRequest.getDestinationStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found"));

        // 3. Create and Save Ticket with QR Code
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setOriginStation(origin);
        ticket.setDestinationStation(destination);
        ticket.setFare(fare);
        ticket.setTicketType(bookingRequest.getTicketType());
        ticket.setPayment(savedPayment);
        ticket.setBookingTime(Instant.now());
        ticket.setIssueDate(LocalDate.now());
        ticket.setExpiryTime(Instant.now().plus(24, ChronoUnit.HOURS));
        ticket.setStatus(TicketStatus.CONFIRMED);

        // --- QR Code Generation Logic ---
        // a. Generate a unique ticket number first
        String ticketNumber = UUID.randomUUID().toString();
        ticket.setTicketNumber(ticketNumber);

        // b. Create a structured JSON payload for the QR code
        String qrPayload = String.format(
            "{\"ticketNumber\":\"%s\",\"userId\":%d,\"origin\":\"%s\",\"destination\":\"%s\"}",
            ticketNumber,
            user.getUserId(),
            origin.getName(),
            destination.getName()
        );

        // c. Generate the QR code as a Base64 string and set it
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrPayload);
        ticket.setQrCodePayload(qrCodeBase64); // This now correctly sets the Base64 image data
        
        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.entityToDto(savedTicket);
    }

    @Override
    public TicketDto getTicketByIdAndUser(Integer ticketId, User user) {
        Ticket ticket = ticketRepository.findByTicketIdAndUser(ticketId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        return ticketMapper.entityToDto(ticket);
    }

    @Override
    @Transactional
    public TicketDto cancelTicket(Integer ticketId, User user) {
        Ticket ticket = ticketRepository.findByTicketIdAndUser(ticketId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new InvalidOperationException("This ticket cannot be cancelled as it is already " + ticket.getStatus());
        }
        
        if (ticket.getExpiryTime().isBefore(Instant.now())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            throw new InvalidOperationException("This ticket cannot be cancelled as it has already expired.");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket cancelledTicket = ticketRepository.save(ticket);

        if (ticket.getPayment().getPaymentMethod() == PaymentMethod.WALLET) {
            Wallet userWallet = walletService.getWalletByUser(user);
            walletService.credit(userWallet, new RechargeRequest(ticket.getFare()));
        }

        return ticketMapper.entityToDto(cancelledTicket);
    }

    @Override
    public List<TicketDto> getTicketHistory(User user) {
        List<Ticket> tickets = ticketRepository.findByUserOrderByBookingTimeDesc(user);
        return ticketMapper.entityToDto(tickets);
    }
}