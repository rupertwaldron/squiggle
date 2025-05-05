package com.ruppyrup.server.repository;

import com.ruppyrup.server.model.GuessWord;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class WordRepository {
    private final Map<String, GuessWord> guessWords = new ConcurrentHashMap<>();

    //todo - make sure word is set when playing follow on games
    public void addWord(String gameId, String word) {
        guessWords.put(gameId, new GuessWord(word));
    }

    public GuessWord getWord(String gameId) {
        return guessWords.get(gameId);
    }

    public void reset() {
        guessWords.clear();
    }
}

