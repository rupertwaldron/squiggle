package com.ruppyrup.server.command;

import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.repository.GameRepository;
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
}
