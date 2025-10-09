package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.TicketStatus;
import com.aurionpro.app.common.TicketType;
import com.aurionpro.app.common.ValidationType;
import com.aurionpro.app.dto.QrPayloadDto;
import com.aurionpro.app.dto.ScanRequestDto;
import com.aurionpro.app.dto.ValidationHistoryDto;
import com.aurionpro.app.dto.ValidationResponseDto;
import com.aurionpro.app.entity.Station;
import com.aurionpro.app.entity.Ticket;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.entity.Validation;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.exception.ResourceNotFoundException;
import com.aurionpro.app.repository.StationRepository;
import com.aurionpro.app.repository.TicketRepository;
import com.aurionpro.app.repository.ValidationRepository;
import com.aurionpro.app.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final TicketRepository ticketRepository;
    private final ValidationRepository validationRepository;
    private final StationRepository stationRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ValidationResponseDto validateTicket(ScanRequestDto scanRequest, User staffMember) {
        try {
            QrPayloadDto payload = objectMapper.readValue(scanRequest.getQrCodePayload(), QrPayloadDto.class);

            Ticket ticket = ticketRepository.findByTicketNumber(payload.getTicketNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

            Station currentStation = stationRepository.findById(scanRequest.getStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Scanning station not found with ID: " + scanRequest.getStationId()));

            if (ticket.getStatus() == TicketStatus.CANCELLED) {
                throw new InvalidOperationException("Ticket is invalid (status: CANCELLED)");
            }
            if (ticket.getExpiryTime().isBefore(Instant.now())) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
                throw new InvalidOperationException("This ticket has expired.");
            }

            if (ticket.getTicketType() == TicketType.DAY_PASS) {
                // Day pass logic remains the same
                switch (ticket.getStatus()) {
                    case CONFIRMED:
                        ticket.setStatus(TicketStatus.IN_TRANSIT);
                        ticketRepository.save(ticket);
                        recordValidation(ticket, currentStation, ValidationType.ENTRY, true);
                        return new ValidationResponseDto(true, "Day Pass Entry Successful at " + currentStation.getName(), ticket.getTicketNumber(), Instant.now());

                    case IN_TRANSIT:
                        ticket.setStatus(TicketStatus.CONFIRMED);
                        ticketRepository.save(ticket);
                        recordValidation(ticket, currentStation, ValidationType.EXIT, true);
                        return new ValidationResponseDto(true, "Day Pass Exit Successful at " + currentStation.getName(), ticket.getTicketNumber(), Instant.now());
                        
                    default:
                        throw new InvalidOperationException("Day Pass is in an invalid state for scanning: " + ticket.getStatus());
                }
            } else {
                // Logic for ONE_WAY and RETURN tickets
                int originOrder = ticket.getOriginStation().getStationOrder();
                int destOrder = ticket.getDestinationStation().getStationOrder();
                int currentOrder = currentStation.getStationOrder();
                
                switch (ticket.getStatus()) {
                    case CONFIRMED: // Entry Scan
                        boolean isValidEntry = (destOrder > originOrder)
                            ? (currentOrder >= originOrder && currentOrder < destOrder) // Ascending direction
                            : (currentOrder <= originOrder && currentOrder > destOrder); // Descending direction

                        if (!isValidEntry) {
                             throw new InvalidOperationException("Invalid entry station. Ticket is for travel between " + ticket.getOriginStation().getName() + " and " + ticket.getDestinationStation().getName());
                        }
                        ticket.setStatus(TicketStatus.IN_TRANSIT);
                        ticketRepository.save(ticket);
                        recordValidation(ticket, currentStation, ValidationType.ENTRY, true);
                        return new ValidationResponseDto(true, "Check-In Successful at " + currentStation.getName(), ticket.getTicketNumber(), Instant.now());

                    case IN_TRANSIT: // Exit Scan
                        // ================== THIS IS THE MODIFIED LOGIC ==================
                        boolean isValidExit = (destOrder > originOrder)
                            ? (currentOrder > originOrder && currentOrder <= destOrder) // Ascending direction
                            : (currentOrder < originOrder && currentOrder >= destOrder); // Descending direction
                        
                        if (!isValidExit) {
                            throw new InvalidOperationException("Invalid exit station. This ticket's destination is " + ticket.getDestinationStation().getName());
                        }
                        // ================================================================
                        ticket.setStatus(TicketStatus.USED);
                        ticketRepository.save(ticket);
                        recordValidation(ticket, currentStation, ValidationType.EXIT, true);
                        return new ValidationResponseDto(true, "Check-Out Successful at " + currentStation.getName(), ticket.getTicketNumber(), Instant.now());

                    case USED:
                        throw new InvalidOperationException("This ticket has already been used for a complete journey.");

                    default:
                        throw new InvalidOperationException("Ticket is in an invalid state for scanning: " + ticket.getStatus());
                }
            }
        } catch (Exception e) {
            return new ValidationResponseDto(false, e.getMessage(), null, Instant.now());
        }
    }

    private void recordValidation(Ticket ticket, Station station, ValidationType type, boolean isValid) {
        Validation validation = new Validation();
        validation.setTicket(ticket);
        validation.setStation(station);
        validation.setValidationType(type);
        validation.setIsValid(isValid);
        validation.setValidationTime(Instant.now());
        validationRepository.save(validation);
    }

    @Override
    public List<ValidationHistoryDto> getValidationHistory(Integer ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket with ID " + ticketId + " not found.");
        }
        List<Validation> validations = validationRepository.findByTicketTicketIdOrderByValidationTimeDesc(ticketId);
        return validations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ValidationHistoryDto mapToDto(Validation validation) {
        ValidationHistoryDto dto = new ValidationHistoryDto();
        dto.setValidationId(validation.getValidationId());
        dto.setTicketId(validation.getTicket().getTicketId());
        dto.setValidationTime(validation.getValidationTime());
        dto.setValidationType(validation.getValidationType());
        dto.setValid(validation.getIsValid());
        if (validation.getStation() != null) {
            dto.setStationName(validation.getStation().getName());
        } else {
            dto.setStationName("N/A");
        }
        return dto;
    }
}