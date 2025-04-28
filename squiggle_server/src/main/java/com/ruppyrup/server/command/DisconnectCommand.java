package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Slf4j
public class DisconnectCommand implements SquiggleCommand {

    private final MessageService messageService;
    private final GameRepository gameRepository;

    public DisconnectCommand(MessageService messageService, GameRepository gameRepository) {
        this.messageService = messageService;
        this.gameRepository = gameRepository;
    }

    @Override
    public void execute(WebSocketSession session, DrawPoint drawPoint) {
        if (drawPoint == null) {
            log.info("DrawPoint is null, find player to remove {}", Thread.currentThread());
            gameRepository.getGames().forEach(game -> {
                List<WebSocketSession> sessions = game.getSessions();
                if (sessions.contains(session)) {
                    String gameId = game.getGameId();
                    String playerId = game.removePlayer(session).playerId();
                    log.info("Removed player {} from game {} on thread {}", session.getId(), game.getGameId(), Thread.currentThread());
                    DrawPoint exitDrawPoint = DrawPoint.builder()
                            .action("exitRoom")
                            .playerId(playerId)
                            .gameId(gameId)
                            .build();

                    sessions.remove(session);

                    try {
                        messageService.sendInfoToSessions(sessions, exitDrawPoint.toJson());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            log.info("DrawPoint is not null, find player to remove {}", Thread.currentThread());
            List<WebSocketSession> sessions = getGameSessions(drawPoint, gameRepository);

            if (sessions.isEmpty()) {
                log.warn("No sessions found for game id {} on thread {}", drawPoint.gameId(), Thread.currentThread());
                return;
            }

            String gameId = drawPoint.gameId();
            String playerId = drawPoint.playerId();
            Game game = gameRepository.getGameById(gameId);
            game.removePlayer(session);

            DrawPoint exitDrawPoint = DrawPoint.builder()
                    .action("exitRoom")
                    .playerId(playerId)
                    .gameId(gameId)
                    .build();

            sessions.remove(session);

            try {
                messageService.sendInfoToSessions(sessions, exitDrawPoint.toJson());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
