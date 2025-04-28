package com.ruppyrup.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Game implements Jsonisable {

    @Getter
    private final String gameId;

    private final Map<Player, WebSocketSession> playerSessions = new ConcurrentHashMap<>();

    public Game(String gameId) {
        this.gameId = gameId;
    }

    public void addPlayer(Player playerId) {
        playerSessions.put(playerId, playerId.session());
    }

    public void removePlayer(Player playerId) {
        playerSessions.remove(playerId);
//        try (WebSocketSession remove = playerSessions.remove(playerId)) {
//            log.info("Removed player {} from game {} on thread {}", remove.getId(), gameId, Thread.currentThread());
//        } catch (Exception e) {
//            log.error("Error removing player {} from game {}: {}", playerId, gameId, e.getMessage());
//        }
    }

    public boolean isPlayerInGame(Player playerId) {
        return playerSessions.containsKey(playerId);
    }

    public void removeAllPlayers() {
        playerSessions.clear();
    }

    public List<WebSocketSession> getSessions() {
        return new ArrayList<>(playerSessions.values());
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(playerSessions.keySet());
    }

    public Player removePlayer(WebSocketSession session) {
        Player playerToRemove = null;
        for (Map.Entry<Player, WebSocketSession> entry : playerSessions.entrySet()) {
            if (entry.getValue().equals(session)) {
                playerToRemove = entry.getKey();
                break;
            }
        }
        removePlayer(playerToRemove);
        return playerToRemove;
    }
}
