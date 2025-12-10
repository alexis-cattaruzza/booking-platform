package com.booking.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingApiApplication {

	public static void main(String[] args) {
		// Load .env.local file
		Dotenv dotenv = Dotenv.configure()
				.filename(".env.local")
				.ignoreIfMissing()
				.load();

		// Set system properties from .env.local
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		SpringApplication.run(BookingApiApplication.class, args);
	}

}
