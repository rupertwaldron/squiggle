package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

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
        List<WebSocketSession> sessions = SessionUtils.getOtherPlayerSessions(drawPoint, gameRepository);

        try {
            messageService.sendInfoToSessions(sessions, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending mouse up command {} on thread {}", drawPoint, Thread.currentThread());
    }
}
