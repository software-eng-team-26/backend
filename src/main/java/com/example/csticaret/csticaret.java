package com.example.csticaret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import com.example.csticaret.repository.UserRepository;

@SpringBootApplication
public class csticaret {
	private static final Logger log = LoggerFactory.getLogger(csticaret.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@PostConstruct
	public void init() {
		log.info("Database connection test:");
		try {
			long userCount = userRepository.count();
			log.info("Number of users in database: {}", userCount);
		} catch (Exception e) {
			log.error("Database connection failed:", e);
		}
	}
	
	public static void main(String[] args) {
		SpringApplication.run(csticaret.class, args);
	}
}
