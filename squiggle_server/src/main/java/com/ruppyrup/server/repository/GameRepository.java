package com.ruppyrup.server.repository;

import java.util.ArrayList;
import java.util.List;


public class GameRepository {
    private final List<String> games = new ArrayList<>();

    public List<String> getGames() {
        return new ArrayList<>(games);
    }

    public void addGame(String game) {
        games.add(game);
    }

    public void removeGame(String game) {
        games.remove(game);
    }
}
