package com.aurionpro.app.service.implementation;

import com.aurionpro.app.common.TicketStatus;
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
            // Step 1: Parse the QR Code JSON payload
            QrPayloadDto payload = objectMapper.readValue(scanRequest.getQrCodePayload(), QrPayloadDto.class);

            // Step 2: Find the ticket and the station where the scan is happening
            Ticket ticket = ticketRepository.findByTicketNumber(payload.getTicketNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

            Station currentStation = stationRepository.findById(scanRequest.getStationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Scanning station not found with ID: " + scanRequest.getStationId()));

            // Step 3: Perform initial validation checks (cancellation and expiry)
            if (ticket.getStatus() == TicketStatus.CANCELLED || ticket.getStatus() == TicketStatus.EXPIRED) {
                throw new InvalidOperationException("Ticket is invalid (status: " + ticket.getStatus() + ")");
            }
            if (ticket.getExpiryTime().isBefore(Instant.now())) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
                throw new InvalidOperationException("This ticket has expired.");
            }

            // Step 4: Implement the Check-In / Check-Out state machine
            switch (ticket.getStatus()) {
                case CONFIRMED: // --- THIS IS THE CHECK-IN LOGIC ---
                    // Verify if the entry scan is at the correct origin station
                    if (!ticket.getOriginStation().getStationId().equals(currentStation.getStationId())) {
                        throw new InvalidOperationException("Invalid entry station. Please scan at: " + ticket.getOriginStation().getName());
                    }
                    // Update ticket status to IN_TRANSIT
                    ticket.setStatus(TicketStatus.IN_TRANSIT);
                    ticketRepository.save(ticket);
                    // Record the successful ENTRY validation
                    recordValidation(ticket, currentStation, ValidationType.ENTRY, true);
                    return new ValidationResponseDto(true, "Check-In Successful at " + currentStation.getName(), ticket.getTicketNumber(), Instant.now());

                case IN_TRANSIT: // --- THIS IS THE CHECK-OUT LOGIC ---
                    // Verify if the exit scan is at the correct destination station
                    if (!ticket.getDestinationStation().getStationId().equals(currentStation.getStationId())) {
                        throw new InvalidOperationException("Invalid exit station. Please scan at: " + ticket.getDestinationStation().getName());
                    }
                    // Update ticket status to USED
                    ticket.setStatus(TicketStatus.USED);
                    ticketRepository.save(ticket);
                    // Record the successful EXIT validation
                    recordValidation(ticket, currentStation, ValidationType.EXIT, true);
                    return new ValidationResponseDto(true, "Check-Out Successful at " + currentStation.getName(), ticket.getTicketNumber(), Instant.now());

                case USED:
                    throw new InvalidOperationException("This ticket has already been used for a complete journey.");

                default:
                    throw new InvalidOperationException("Ticket is in an invalid state for scanning.");
            }

        } catch (Exception e) {
            // Log any failure and return a clear error message
            // Note: We don't record a failed validation here as the business logic exceptions are more informative.
            return new ValidationResponseDto(false, e.getMessage(), null, Instant.now());
        }
    }

    // Helper method to create and save a validation record
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
        // ... (this method remains the same and will now show both ENTRY and EXIT records)
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket with ID " + ticketId + " not found.");
        }
        List<Validation> validations = validationRepository.findByTicketTicketIdOrderByValidationTimeDesc(ticketId);
        return validations.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ValidationHistoryDto mapToDto(Validation validation) {
        // ... (this method remains the same)
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