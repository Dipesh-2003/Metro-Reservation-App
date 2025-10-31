package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private Instant timestamp;
    private String path;
    private ErrorDetails error;
}