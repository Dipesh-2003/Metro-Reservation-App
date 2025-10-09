package com.aurionpro.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
    private String event;
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private PaymentData payment;
        
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PaymentData {
            private EntityData entity;
            
            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class EntityData {
                private String id; //razorpay Payment ID
                private String order_id; //razorpay Order ID
                private String status;
                private Map<String, String> notes;
            }
        }
    }
}