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
        if (bookingRequest.getPaymentMethod() != PaymentMethod.WALLET) {
            throw new InvalidOperationException("This booking method is only for wallet payments.");
        }
        
        BigDecimal fare = calculateFare(bookingRequest.getOriginStationId(), bookingRequest.getDestinationStationId()).getFare();

        Payment payment = new Payment();
        payment.setAmount(fare);
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setCreatedAt(Instant.now());
        payment.setUser(user);

        Wallet userWallet = walletService.getWalletByUser(user);
        walletService.debit(userWallet, fare);
        payment.setStatus(PaymentStatus.COMPLETED);
        
        Payment savedPayment = paymentRepository.save(payment);

        return createTicketForConfirmedPayment(bookingRequest, user, savedPayment);
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

    @Override
    @Transactional
    public TicketDto createTicketForConfirmedPayment(BookingRequest bookingRequest, User user, Payment payment) {
        BigDecimal fare = payment.getAmount();
        Station origin = stationRepository.findById(bookingRequest.getOriginStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin station not found"));
        Station destination = stationRepository.findById(bookingRequest.getDestinationStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found"));

        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setOriginStation(origin);
        ticket.setDestinationStation(destination);
        ticket.setFare(fare);
        ticket.setTicketType(bookingRequest.getTicketType());
        ticket.setPayment(payment);
        ticket.setBookingTime(Instant.now());
        ticket.setIssueDate(LocalDate.now());
        ticket.setExpiryTime(Instant.now().plus(24, ChronoUnit.HOURS));
        ticket.setStatus(TicketStatus.CONFIRMED);

        String ticketNumber = UUID.randomUUID().toString();
        ticket.setTicketNumber(ticketNumber);

        String qrPayload = String.format(
            "{\"ticketNumber\":\"%s\",\"userId\":%d,\"origin\":\"%s\",\"destination\":\"%s\"}",
            ticketNumber, user.getUserId(), origin.getName(), destination.getName()
        );

        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(qrPayload);
        ticket.setQrCodePayload(qrCodeBase64);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.entityToDto(savedTicket);
    }
}