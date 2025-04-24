package com.ruppyrup.server.repository;

import com.ruppyrup.server.model.Game;
import com.ruppyrup.server.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameRepository {
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public List<Game> getGames() {
        return new ArrayList<>(games.values());
    }

    public void addGame(Game game) {
        games.putIfAbsent(game.getGameId(), game);
    }

    public boolean gameExists(String gameId) {
        return games.containsKey(gameId);
    }

    public void removeGame(Game game) {
        games.remove(game.getGameId());
    }

    public void clearGames() {
        games.clear();
    }

    public void addPlayerToGame(String gameId, Player playerId) {
        Game game = getGameById(gameId);
        if (game != null) {
            game.addPlayer(playerId);

        } else {
            throw new IllegalArgumentException("Game with ID " + gameId + " does not exist.");
        }
    }

    public Game getGameById(String gameId) {
        return games.get(gameId);
    }
}
