package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.Player;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import com.ruppyrup.server.utils.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Objects;

@Slf4j
public class MouseDownCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final GameRepository gameRepository;

    public MouseDownCommand(MessageService messageService, GameRepository gameRepository) {
        this.messageService = messageService;
        this.gameRepository = gameRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {

        if (!gameRepository.gameExists(drawPoint.gameId())) {
            log.info("Game with Id does not exist");
            return;
        }

        List<WebSocketSession> sessions = SessionUtils.getOtherPlayerSessions(drawPoint, gameRepository);


        // Handle the draw point here
        try {
            messageService.sendInfoToSessions(sessions, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("Sending mouse down command {} on thread {}", drawPoint, Thread.currentThread());
    }
}
