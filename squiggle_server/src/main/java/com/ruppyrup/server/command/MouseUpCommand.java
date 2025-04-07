package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class MouseUpCommand implements SquiggleCommand {

    private final MessageService messageService;

    public MouseUpCommand(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        // Handle the draw point here
        try {
            messageService.sendInfoToOthers(session, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending mouse up command {} on thread {}", drawPoint, Thread.currentThread());
    }
}
