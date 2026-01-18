package com.lab.reliability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReliabilityLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReliabilityLabApplication.class, args);
    }
}
