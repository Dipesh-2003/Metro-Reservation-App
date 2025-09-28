package com.aurionpro.app.service.implementation;

import java.math.BigDecimal;
import java.time.Instant;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurionpro.app.common.PaymentMethod;
import com.aurionpro.app.common.PaymentStatus;
import com.aurionpro.app.dto.BookingRequest;
import com.aurionpro.app.dto.UpiPaymentRequest;
import com.aurionpro.app.dto.UpiPaymentResponse;
import com.aurionpro.app.entity.Payment;
import com.aurionpro.app.entity.User;
import com.aurionpro.app.exception.InvalidOperationException;
import com.aurionpro.app.repository.PaymentRepository;
import com.aurionpro.app.service.PaymentService;
import com.aurionpro.app.service.TicketService;
import com.aurionpro.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService; 
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @Override
    @Transactional
    public UpiPaymentResponse createUpiOrder(UpiPaymentRequest request, User user) throws RazorpayException {
        // 1. Calculate the fare
        BigDecimal fare = ticketService.calculateFare(request.getOriginStationId(), request.getDestinationStationId()).getFare();

        // 2. Create a Payment record in PENDING state
     // 2. Create a Payment record in PENDING state
        Payment payment = new Payment();
        payment.setAmount(fare);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUser(user);
        Payment savedPayment = paymentRepository.save(payment);

        // 3. Create an order with Razorpay
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", fare.multiply(new BigDecimal(100)).intValue()); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "receipt_metro_" + savedPayment.getPaymentId());

        // Add notes to link Razorpay order with our internal payment and booking
        JSONObject notes = new JSONObject();
        notes.put("paymentId", savedPayment.getPaymentId().toString());
        notes.put("userId", user.getUserId().toString());
        notes.put("originStationId", request.getOriginStationId().toString());
        notes.put("destinationStationId", request.getDestinationStationId().toString());
        notes.put("ticketType", request.getTicketType().toString());
        orderRequest.put("notes", notes);
        
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        // 4. Update our Payment record with the Razorpay Order ID
        savedPayment.setTransactionId(razorpayOrderId);
        paymentRepository.save(savedPayment);

        // 5. Return the response to the client
        return new UpiPaymentResponse(razorpayOrderId, savedPayment.getPaymentId(), fare, "INR", apiKey);
    }
    
    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) { // <-- Notice the change to String payload
        try {
            // 1. Verify the webhook signature against the RAW payload string
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                log.warn("Webhook signature verification failed.");
                throw new InvalidOperationException("Invalid webhook signature.");
            }
            
            // 2. If verification passes, THEN parse the string into an object
            JSONObject payloadJson = new JSONObject(payload);
            String event = payloadJson.getString("event");

            if (!"payment.captured".equals(event)) {
                 log.info("Ignoring webhook event: {}", event);
                 return;
            }

         // 3. Extract relevant data from the parsed JSON
            JSONObject payloadObject = payloadJson.getJSONObject("payload");
            JSONObject paymentEntity = payloadObject.getJSONObject("payment").getJSONObject("entity"); // <-- FIX HERE

            String razorpayOrderId = paymentEntity.getString("order_id");
            String status = paymentEntity.getString("status");
            JSONObject notesJson = paymentEntity.getJSONObject("notes");
            Integer paymentId = Integer.parseInt(notesJson.getString("paymentId"));
            
            // 4. Find the payment in our database
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new InvalidOperationException("Payment not found for ID: " + paymentId));
            
            // 5. If payment was successful, create the ticket
            if ("captured".equalsIgnoreCase(status) && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                
                BookingRequest bookingRequest = new BookingRequest();
                bookingRequest.setOriginStationId(Integer.parseInt(notesJson.getString("originStationId")));
                bookingRequest.setDestinationStationId(Integer.parseInt(notesJson.getString("destinationStationId")));
                bookingRequest.setTicketType(com.aurionpro.app.common.TicketType.valueOf(notesJson.getString("ticketType")));
                bookingRequest.setPaymentMethod(PaymentMethod.UPI);
                
                Integer userId = Integer.parseInt(notesJson.getString("userId"));
                User ticketUser = userService.findUserEntityById(userId);
                
                ticketService.createTicketForConfirmedPayment(bookingRequest, ticketUser, payment);
                
                log.info("Ticket created successfully for payment ID: {}", paymentId);
            } else {
                 log.warn("Webhook received for non-captured payment or payment not in PENDING state. Status: {}, PaymentId: {}", status, paymentId);
            }

        } catch (Exception e) {
            log.error("Error processing webhook or verifying signature", e);
            throw new RuntimeException(e);
        }
    }
}