package com.ruppyrup.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Game (
        String gameId,
        @Singular
        List<String> players
) implements Jsonisable {

    public static class GameBuilder {
        public Game build() {
            if (this.players == null) {
                this.players = new ArrayList<>();
            } else {
                this.players = new ArrayList<>(this.players);
            }
            return new Game(gameId, players);
        }
    }

    public void addPlayer(String playerId) {
        players.add(playerId);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    public boolean isPlayerInGame(String playerId) {
        return players.contains(playerId);
    }

    public void removeAllPlayers() {
        players.clear();
    }
}
