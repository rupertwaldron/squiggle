package com.ruppyrup.server.websocket;


import com.ruppyrup.server.command.SquiggleCommandFactory;
import com.ruppyrup.server.repository.WordRepository;
import com.ruppyrup.server.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${reveal.count}") int revealCount;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(messageService(), squiggleCommandFactory()),
                "/websocket").setAllowedOrigins("*");
    }

    @Bean
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public MessageService messageService() {
        return new MessageService(virtualThreadExecutor());
    }

    @Bean
    public SquiggleCommandFactory squiggleCommandFactory() {
        return new SquiggleCommandFactory(messageService(), wordRepository(), revealCount);
    }

    @Bean
    public WordRepository wordRepository() {
        return new WordRepository();
    }
}