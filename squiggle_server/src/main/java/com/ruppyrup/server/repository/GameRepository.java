package com.ruppyrup.server.repository;

import com.ruppyrup.server.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class GameRepository {
    private final List<Game> games = new ArrayList<>();

    public List<Game> getGames() {
        return new ArrayList<>(games);
    }

    public void addGame(Game game) {
        games.add(game);
    }

    public boolean gameExists(String gameId) {
        return games.stream().anyMatch(game -> game.gameId().equals(gameId));
    }

    public void removeGame(Game game) {
        games.remove(game);
    }

    public void clearGames() {
        games.clear();
    }

    public void addPlayerToGame(String gameId, String playerId) {
        Game game = getGameById(gameId);
        if (game != null) {
            game.addPlayer(playerId);
        } else {
            throw new IllegalArgumentException("Game with ID " + gameId + " does not exist.");
        }
    }

    public Game getGameById(String gameId) {
        return games.stream()
                .filter(game -> game.gameId().equals(gameId))
                .findFirst()
                .orElse(null);
    }
}
