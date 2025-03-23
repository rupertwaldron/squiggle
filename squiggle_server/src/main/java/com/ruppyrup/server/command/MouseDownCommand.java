package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class MouseDownCommand implements SquiggleCommand {

    private final MessageService messageService;

    public MouseDownCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        // Handle the draw point here
        try {
            messageService.sendInfo(session, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending mouse down command {} on thread {}", drawPoint, Thread.currentThread());
    }
}
