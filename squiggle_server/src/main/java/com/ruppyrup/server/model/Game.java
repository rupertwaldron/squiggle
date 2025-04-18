package com.ruppyrup.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Game(
        String gameId,
        @Singular
        List<String> players
) {

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

    private static final ObjectMapper mapper = new ObjectMapper();

    public String toJson() throws JsonProcessingException {
        return mapper.writeValueAsString(this);
    }

    public static Game fromJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, Game.class);
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
