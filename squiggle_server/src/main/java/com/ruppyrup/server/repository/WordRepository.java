package com.ruppyrup.server.repository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;


@Slf4j
public class WordRepository {
    private String guessWord;

    public String getGuessWord() {
        return guessWord;
    }

    public void setGuessWord(String guessWord) {
        log.info("Setting guess word to {}", guessWord);
        this.guessWord = guessWord;
    }
}

