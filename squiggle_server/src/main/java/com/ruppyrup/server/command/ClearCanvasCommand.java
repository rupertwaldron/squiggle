package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Component
@Slf4j
public class ClearCanvasCommand implements SquiggleCommand {

    private final GameRepository gameRepository;
    private final MessageService messageService;

    @Autowired
    public ClearCanvasCommand(MessageService messageService, GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.messageService = messageService;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        try {
            sendToOtherSessions(session, drawPoint, gameRepository, messageService);
        } catch (IllegalStateException e) {
            log.warn("No sessions found for game id {} on thread {}: {}", drawPoint.gameId(), Thread.currentThread(), e.getMessage());
        }

        log.info("Sending clear canvas command {} on thread {}", drawPoint, Thread.currentThread());
    }
}
