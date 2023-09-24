package com.edutrain.busbooking.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.edutrain.busbooking.booking.repository.BookingRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = BookingRepository.class)
@EnableEurekaClient
@ComponentScan(basePackages = "com.edutrain.busbooking.booking.controller,com.edutrain.busbooking.booking.model,com.edutrain.busbooking.booking.repository")
public class BookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
	}

}
