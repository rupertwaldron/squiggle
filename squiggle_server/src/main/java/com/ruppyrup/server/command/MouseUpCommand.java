package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

@Slf4j
public class MouseUpCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final GameRepository gameRepository;

    public MouseUpCommand(MessageService messageService, GameRepository gameRepository) {
        this.messageService = messageService;
        this.gameRepository = gameRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        // Handle the draw point here
        List<WebSocketSession> sessions = getGameSessions(drawPoint, gameRepository);

        if (sessions.isEmpty()) {
            log.warn("No sessions found for game id {} on thread {}", drawPoint.gameId(), Thread.currentThread());
            return;
        }

        sessions.remove(session);

        try {
            messageService.sendInfoToSessions(sessions, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending mouse up command {} on thread {}", drawPoint, Thread.currentThread());
    }
}
