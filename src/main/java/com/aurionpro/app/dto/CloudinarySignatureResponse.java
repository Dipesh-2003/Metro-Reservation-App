package com.aurionpro.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloudinarySignatureResponse {
    private String signature;
    private String timestamp;
    private String apiKey;
    private String cloudName;
}