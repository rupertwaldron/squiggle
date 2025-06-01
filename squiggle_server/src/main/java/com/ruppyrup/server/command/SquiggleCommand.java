package com.ruppyrup.server.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.Player;
import com.ruppyrup.server.repository.GameRepository;
import com.ruppyrup.server.service.MessageService;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;


public interface SquiggleCommand {
    void execute(WebSocketSession session, DrawPoint drawPoint);

    default List<WebSocketSession> getGameSessions(DrawPoint drawPoint, GameRepository gameRepository) {
        return Optional.ofNullable(gameRepository.getGameById(drawPoint.gameId()))
                .map(Game::getSessions)
                .orElse(List.of());
    }

    default List<Player> getGamePlayers(DrawPoint drawPoint, GameRepository gameRepository) {
        return Optional.ofNullable(gameRepository.getGameById(drawPoint.gameId()))
                .map(Game::getPlayers)
                .orElse(List.of());
    }

    default void sendBackToSession(WebSocketSession session, DrawPoint drawPoint, GameRepository gameRepository, MessageService messageService) {
        // Handle the draw point here
        sendMesssage(drawPoint, messageService, gameRepository, List.of(session));
    }

    default void sendToOtherSessions(WebSocketSession session, DrawPoint drawPoint, GameRepository gameRepository, MessageService messageService) {
        List<WebSocketSession> sessions = getWebSocketSessions(drawPoint, gameRepository);

        sessions.remove(session);

        // Handle the draw point here
        sendMesssage(drawPoint, messageService, gameRepository, sessions);
    }

    default void sendToAllSessions(DrawPoint drawPoint, GameRepository gameRepository, MessageService messageService) {
        List<WebSocketSession> sessions = getWebSocketSessions(drawPoint, gameRepository);

        // Handle the draw point here
        sendMesssage(drawPoint, messageService, gameRepository, sessions);
    }

    private void sendMesssage(DrawPoint drawPoint, MessageService messageService, GameRepository gameRepository, List<WebSocketSession> sessions) {
        try {
//            List<String> gamePlayers = getGamePlayers(drawPoint, gameRepository).stream()
//                    .map(Player::playerId)
//                    .toList();
//
//            DrawPoint drawPointToSend = drawPoint.toBuilder()
//                    .players(gamePlayers)
//                    .build();

            messageService.sendInfoToSessions(sessions, drawPoint.toJson());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<WebSocketSession> getWebSocketSessions(DrawPoint drawPoint, GameRepository gameRepository) {
        List<WebSocketSession> sessions = getGameSessions(drawPoint, gameRepository);

        if (sessions.isEmpty()) {
            throw new IllegalStateException("No sessions found for game id " + drawPoint.gameId());
        }
        return sessions;
    }
}
