package com.ruppyrup.server.repository;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class WordRepository {
    private String guessWord;
    private final AtomicInteger guessCount = new AtomicInteger(0);

    public int getGuessCount() {
        return guessCount.get();
    }

    public void incrementGuessCount() {
        log.info("Incrementing guess count to {}", guessCount.get() + 1);
        guessCount.incrementAndGet();
    }

    public void setGuessCount(int guessCount) {
        log.info("Setting guess count to {}", guessCount);
        this.guessCount.set(guessCount);
    }

    public String getGuessWord() {
        return guessWord;
    }

    public void setGuessWord(String guessWord) {
        log.info("Setting guess word to {}", guessWord);
        this.guessWord = guessWord;
    }
}

