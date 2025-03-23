package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class NotArtistCommand implements SquiggleCommand {

    private final MessageService messageService;

    public NotArtistCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        try {
            messageService.sendInfo(session, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending artist change {} on thread {}", drawPoint, Thread.currentThread());
    }
}
