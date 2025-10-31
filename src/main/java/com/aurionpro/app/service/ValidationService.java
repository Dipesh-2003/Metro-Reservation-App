package com.aurionpro.app.service;

import java.util.List;

import com.aurionpro.app.dto.ScanRequestDto;
import com.aurionpro.app.dto.ValidationHistoryDto;
import com.aurionpro.app.dto.ValidationResponseDto;
import com.aurionpro.app.entity.User;

public interface ValidationService {
    ValidationResponseDto validateTicket(ScanRequestDto scanRequest, User staffMember);
    List<ValidationHistoryDto> getValidationHistory(Integer ticketId);

}