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
            // Read the PDF file and encode it as base64
            File invoiceFile = new File(invoicePath);
            byte[] fileContent = Files.readAllBytes(invoiceFile.toPath());
            String base64Invoice = Base64.getEncoder().encodeToString(fileContent);

            // Create attachment
            Map<String, String> attachment = new HashMap<>();
            attachment.put("filename", "invoice.pdf");
            attachment.put("content", base64Invoice);
            attachment.put("type", "application/pdf");

            // Create email request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", fromEmail);
            requestBody.put("to", Collections.singletonList(toEmail));
            requestBody.put("subject", "Your Order Confirmation #" + order.getOrderId());
            requestBody.put("html", buildOrderConfirmationEmail(order));
            requestBody.put("attachments", Collections.singletonList(attachment));

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

            log.info("Order confirmation email sent successfully to {}", toEmail);
        } catch (HttpClientErrorException e) {
            log.error("Resend API error: {}", e.getResponseBodyAsString());
            // Don't throw exception, just log the error
            // This way the order will still be created even if email fails
        } catch (Exception e) {
            log.error("Error sending order confirmation email: {}", e.getMessage());
            // Don't throw exception, just log the error
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
            order.getOrderId(),
            order.getOrderDate(),
            order.getTotalAmount()
        );
    }
} 