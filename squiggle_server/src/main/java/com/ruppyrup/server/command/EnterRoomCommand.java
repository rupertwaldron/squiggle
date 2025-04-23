package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.Player;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
public class EnterRoomCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final GameRepository gameRepository;

    public EnterRoomCommand(MessageService messageService, GameRepository gameRepository) {
        this.messageService = messageService;
        this.gameRepository = gameRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        String gameId = drawPoint.gameId();
        String playerId = drawPoint.playerId();

        DrawPoint drawPointToSend = DrawPoint.builder()
                .action("enterRoom")
                .playerId(playerId)
                .gameId(gameId)
                .build();

        if (!gameRepository.gameExists(gameId)) {
            log.info("Game with Id does not exist");
            DrawPoint invalid = drawPointToSend.toBuilder()
                    .gameId("Invalid")
                    .build();
            sendToSender(session, invalid);
            return;
        }

        Player newPlayer = new Player(playerId, session);

        gameRepository.addPlayerToGame(gameId, newPlayer);

        sendToSender(session, drawPointToSend);

        log.info("Player with Id {} entered game with Id {} on thread {}", playerId, gameId, Thread.currentThread());
    }

    private void sendToSender(WebSocketSession session, DrawPoint drawPointToSend) {
        try {
            messageService.sendInfoToSessions(List.of(session), drawPointToSend.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
