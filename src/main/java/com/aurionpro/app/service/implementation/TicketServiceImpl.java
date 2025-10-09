package com.aurionpro.app.service.implementation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.common.PaymentMethod;
import com.aurionpro.app.common.PaymentStatus;
import com.aurionpro.app.common.TicketStatus;
import com.aurionpro.app.common.TicketType;
import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.FareResponse;
import com.aurionpro.app.dto.RechargeRequest;
import com.aurionpro.app.dto.TicketDto;
import com.aurionpro.app.entity.FareSlab;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Wallet;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.mapper.TicketMapper;
import com.aurionpro.app.repository.FareSlabRepository;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.service.QRCodeService;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.WalletService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final StationRepository stationRepository;
    private final FareSlabRepository fareSlabRepository;
    private final PaymentRepository paymentRepository;
    private final WalletService walletService;
    private final TicketMapper ticketMapper;
    private final QRCodeService qrCodeService;

    @Value("${metro.day-pass.fare}")
    private BigDecimal dayPassFare;

    private TicketDto convertToDtoWithImage(Ticket ticket) {
        TicketDto dto = ticketMapper.entityToDto(ticket);
        if (dto.getQrCodePayload() != null && !dto.getQrCodePayload().isEmpty()) {
            String qrCodeImage = qrCodeService.generateQRCodeBase64(dto.getQrCodePayload());
            dto.setQrCodeImage(qrCodeImage);
        }
        return dto;
    }

    @Override
    public FareResponse calculateFare(Integer originId, Integer destId, TicketType ticketType) {
        if (ticketType == TicketType.DAY_PASS) {
            return new FareResponse(dayPassFare);
        }

        Station origin = stationRepository.findById(originId)
                .orElseThrow(() -> new ResourceNotFoundException("Origin station not found"));
        Station destination = stationRepository.findById(destId)
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found"));

        int stationCount = Math.abs(origin.getStationOrder() - destination.getStationOrder());

        FareSlab fareSlab = fareSlabRepository.findFareSlabForStationCount(stationCount)
                .orElseThrow(() -> new ResourceNotFoundException("Fare slab not found for the selected route"));

        BigDecimal oneWayFare = fareSlab.getFare();
        BigDecimal finalFare;

        if (ticketType == TicketType.RETURN) {
            finalFare = oneWayFare.multiply(new BigDecimal("2"));
        } else {
            // Default to one-way fare for ONE_WAY or if ticketType is null
            finalFare = oneWayFare;
        }

        return new FareResponse(finalFare);
    }

    @Override
    @Transactional
    public TicketDto bookTicket(BookingRequest bookingRequest, User user) {
        if (bookingRequest.getPaymentMethod() != PaymentMethod.WALLET) {
            throw new InvalidOperationException("This endpoint only supports wallet payments. For UPI, use the /upi/pay endpoint.");
        }

        BigDecimal fare = calculateFare(bookingRequest.getOriginStationId(), bookingRequest.getDestinationStationId(), bookingRequest.getTicketType()).getFare();
        
        Payment payment = new Payment();
        payment.setAmount(fare);
        payment.setPaymentMethod(PaymentMethod.WALLET);
        payment.setCreatedAt(Instant.now());
        payment.setUser(user);
        payment.setStatus(PaymentStatus.COMPLETED);
        
        Wallet userWallet = walletService.getWalletByUser(user);
        walletService.debit(userWallet, fare);
        
        Payment savedPayment = paymentRepository.save(payment);

        return createTicketForConfirmedPayment(bookingRequest, user, savedPayment);
    }

    @Override
    public TicketDto getTicketByIdAndUser(Integer ticketId, User user) {
        Ticket ticket = ticketRepository.findByTicketIdAndUser(ticketId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        return convertToDtoWithImage(ticket);
    }

    @Override
    @Transactional
    public TicketDto cancelTicket(Integer ticketId, User user) {
        Ticket ticket = ticketRepository.findByTicketIdAndUser(ticketId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));
        
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
           throw new InvalidOperationException("This ticket cannot be cancelled as it is already " + ticket.getStatus());
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket cancelledTicket = ticketRepository.save(ticket);

        if (ticket.getPayment().getPaymentMethod() == PaymentMethod.WALLET) {
            Wallet userWallet = walletService.getWalletByUser(user);
            walletService.credit(userWallet, new RechargeRequest(ticket.getFare()));
        }

        return convertToDtoWithImage(cancelledTicket);
    }

    @Override
    public List<TicketDto> getTicketHistory(User user) {
        List<Ticket> tickets = ticketRepository.findByUserOrderByBookingTimeDesc(user);
        return tickets.stream()
                      .map(this::convertToDtoWithImage)
                      .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketDto createTicketForConfirmedPayment(BookingRequest bookingRequest, User user, Payment payment) {
        Station origin = stationRepository.findById(bookingRequest.getOriginStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin station not found"));
        Station destination = stationRepository.findById(bookingRequest.getDestinationStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station not found"));
        
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setOriginStation(origin);
        ticket.setDestinationStation(destination);
        ticket.setFare(payment.getAmount());
        ticket.setTicketType(bookingRequest.getTicketType());
        ticket.setPayment(payment);
        ticket.setBookingTime(Instant.now());
        ticket.setIssueDate(LocalDate.now(ZoneId.systemDefault()));
        ticket.setStatus(TicketStatus.CONFIRMED);

        if (bookingRequest.getTicketType() == TicketType.DAY_PASS) {
            Instant endOfDay = LocalDate.now(ZoneId.systemDefault()).plusDays(1)
                                      .atStartOfDay(ZoneId.systemDefault())
                                      .toInstant().minusSeconds(1);
            ticket.setExpiryTime(endOfDay);
        } else {
            ticket.setExpiryTime(Instant.now().plus(24, ChronoUnit.HOURS));
        }

        String ticketNumber = UUID.randomUUID().toString();
        ticket.setTicketNumber(ticketNumber);
        
        String qrPayload = String.format(
            "{\"ticketNumber\":\"%s\",\"userId\":%d,\"origin\":\"%s\",\"destination\":\"%s\"}",
            ticketNumber, user.getUserId(), origin.getName(), destination.getName()
        );
        
        ticket.setQrCodePayload(qrPayload);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        return convertToDtoWithImage(savedTicket);
    }
}