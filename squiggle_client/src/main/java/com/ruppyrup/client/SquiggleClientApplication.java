package com.ruppyrup.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SquiggleClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SquiggleClientApplication.class, args);
    }

}
