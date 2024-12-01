package com.example.csticaret.config;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfig {
    @Value("${resend.api.key}")
    private String resendApiKey;

    @Bean
    public Resend resend() {
        return new Resend(resendApiKey);
    }
} 