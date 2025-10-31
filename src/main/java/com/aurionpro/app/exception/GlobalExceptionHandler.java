package com.aurionpro.app.exception;

import com.aurionpro.app.dto.ApiErrorResponse;
import com.aurionpro.app.dto.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails("RESOURCE_NOT_FOUND", ex.getMessage(), null);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                errorDetails
        );
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiErrorResponse> handleInvalidOperationException(InvalidOperationException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails("INVALID_OPERATION", ex.getMessage(), null);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                errorDetails
        );
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex, HttpServletRequest request) {
        ErrorDetails errorDetails = new ErrorDetails("WALLET_INSUFFICIENT_FUNDS", ex.getMessage(), null);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                errorDetails
        );
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.CONFLICT);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        //log the full exception for debugging
        ex.printStackTrace(); 
        
        ErrorDetails errorDetails = new ErrorDetails("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", null);
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                Instant.now(),
                request.getRequestURI(),
                errorDetails
        );
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}