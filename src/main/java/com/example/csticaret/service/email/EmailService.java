package com.example.csticaret.service.email;

import com.example.csticaret.model.Order;
import com.resend.Resend;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public void sendOrderConfirmation(String toEmail, Order order, String invoicePath) {
        try {
            log.info("Starting to send order confirmation email to: {}", toEmail);
            log.info("Using invoice path: {}", invoicePath);
            
            // Read the PDF file and encode it as base64
            File invoiceFile = new File(invoicePath);
            if (!invoiceFile.exists()) {
                log.error("Invoice file not found at path: {}", invoicePath);
                throw new FileNotFoundException("Invoice file not found");
            }
            
            byte[] fileContent = Files.readAllBytes(invoiceFile.toPath());
            String base64Invoice = Base64.getEncoder().encodeToString(fileContent);
            log.info("Invoice file read and encoded successfully");

            // Create attachment
            Map<String, String> attachment = new HashMap<>();
            attachment.put("filename", "invoice.pdf");
            attachment.put("content", base64Invoice);
            attachment.put("type", "application/pdf");

            // Create email request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", fromEmail);
            requestBody.put("to", Collections.singletonList(toEmail));
            requestBody.put("subject", "Your Order Confirmation #" + order.getId());
            requestBody.put("html", buildOrderConfirmationEmail(order));
            requestBody.put("attachments", Collections.singletonList(attachment));

            // Log request details (excluding sensitive data)
            log.info("Preparing to send email with Resend API");
            log.info("From: {}", fromEmail);
            log.info("To: {}", toEmail);
            log.info("Subject: Order Confirmation #{}", order.getId());

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Create request entity
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), 
                headers
            );

            // Send request
            String response = restTemplate.postForObject(
                "https://api.resend.com/emails",
                request,
                String.class
            );

            log.info("Email sent successfully. Response: {}", response);
        } catch (HttpClientErrorException e) {
            log.error("Resend API error. Status: {}, Response: {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send email via Resend API", e);
        } catch (Exception e) {
            log.error("Error sending order confirmation email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildOrderConfirmationEmail(Order order) {
        return String.format("""
            <html>
            <body>
                <h1>Order Confirmation</h1>
                <p>Thank you for your order!</p>
                <p>Order ID: %s</p>
                <p>Order Date: %s</p>
                <p>Total Amount: $%.2f</p>
                <p>Please find your invoice attached.</p>
                <br>
                <p>Best regards,</p>
                <p>EduMart Team</p>
            </body>
            </html>
            """,
            order.getId(),
            order.getOrderDate(),
            order.getTotalAmount()
        );
    }

    public void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            log.info("Starting to send email to: {}", toEmail);

            // Create email request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", fromEmail);
            requestBody.put("to", Collections.singletonList(toEmail));
            requestBody.put("subject", subject);
            requestBody.put("html", htmlContent);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Create request entity
            HttpEntity<String> request = new HttpEntity<>(
                objectMapper.writeValueAsString(requestBody), 
                headers
            );

            // Send request
            String response = restTemplate.postForObject(
                "https://api.resend.com/emails",
                request,
                String.class
            );

            log.info("Email sent successfully. Response: {}", response);
        } catch (HttpClientErrorException e) {
            log.error("Resend API error. Status: {}, Response: {}", 
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send email via Resend API", e);
        } catch (Exception e) {
            log.error("Error sending email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
} 