package com.ruppyrup.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class SquiggleServerApplication {
    //todo don't let players join a game but need to get the empty boxes
    //todo make sure game link appears in the game list
    public static void main(String[] args) {
        SpringApplication.run(SquiggleServerApplication.class, args);
    }

}
