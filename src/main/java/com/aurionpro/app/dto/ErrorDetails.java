package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDetails {
    private String code;
    private String message;
    private Object details; //used for more detailed validation errors later
}