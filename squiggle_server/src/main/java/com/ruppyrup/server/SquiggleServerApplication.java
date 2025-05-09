package com.ruppyrup.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class SquiggleServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SquiggleServerApplication.class, args);
    }

}
