package com.truckbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TruckBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TruckBookingApplication.class, args);
    }
}
