package com.ruppyrup.server.utils;

import com.ruppyrup.server.model.DrawPoint;
import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.Player;
import com.ruppyrup.server.repository.GameRepository;
import lombok.experimental.UtilityClass;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@UtilityClass
public class SessionUtils {

    public List<WebSocketSession> getOtherPlayerSessions(DrawPoint drawPoint, GameRepository gameRepository) {
        String playerId = drawPoint.playerId();

        Game game = gameRepository.getGameById(drawPoint.gameId());

        List<WebSocketSession> sessions = game.players().stream()
                .filter(player -> !player.playerId().equals(playerId))
                .map(Player::session)
                .toList();
        return sessions;
    }

    public List<WebSocketSession> getGameSessions(DrawPoint drawPoint, GameRepository gameRepository) {
        String playerId = drawPoint.playerId();

        Game game = gameRepository.getGameById(drawPoint.gameId());

        return game.players().stream()
                .map(Player::session)
                .toList();
    }
}
