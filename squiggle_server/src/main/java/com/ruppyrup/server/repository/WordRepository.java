package com.ruppyrup.server.repository;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class WordRepository {
    private String guessWord;
    private final AtomicReference<String> maskedWord = new AtomicReference<>("");
    private final AtomicInteger guessCount = new AtomicInteger(0);
    private final AtomicInteger revealCount = new AtomicInteger(0);
    private final AtomicBoolean isReady = new AtomicBoolean(false);

    public boolean isReady() {
        return isReady.get();
    }

    public void setIsReady(boolean ready) {
        log.info("Setting ready to {}", ready);
        isReady.set(ready);
    }

    public int getRevealCount() {
        return revealCount.get();
    }

    public String getMaskedWord() {
        return maskedWord.get();
    }

    public void setMaskedWord(String maskedWord) {
        log.info("Setting masked word to {}", maskedWord);
        this.maskedWord.set(maskedWord);
    }

    public void incrementRevealCount() {
        log.info("Setting reveal count to {}", revealCount.get() + 1);
        revealCount.incrementAndGet();
    }

    public void resetRevealCount() {
        log.info("Resetting reveal count to 0");
        revealCount.set(0);
    }

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

