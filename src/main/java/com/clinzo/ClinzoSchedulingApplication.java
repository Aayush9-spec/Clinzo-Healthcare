package com.clinzo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClinzoSchedulingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClinzoSchedulingApplication.class, args);
    }
}
