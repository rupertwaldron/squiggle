package com.ruppyrup.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Game implements Jsonisable {

    @Getter
    private final String gameId;

    private final List<Player> players = new CopyOnWriteArrayList<>();

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public Game(String gameId) {
        this.gameId = gameId;
    }

    public void addPlayer(Player playerId) {
        players.add(playerId);
        sessions.add(playerId.session());
    }

    public void removePlayer(Player playerId) {
        players.remove(playerId);
        sessions.remove(playerId.session());
    }

    public boolean isPlayerInGame(Player playerId) {
        return players.contains(playerId);
    }

    public void removeAllPlayers() {
        players.clear();
        sessions.clear();
    }

    public List<WebSocketSession> getSessions() {
        return new ArrayList<>(sessions);
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
}
