package com.ruppyrup.server.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuessWordTest {

    private GuessWord guessWord;

    @BeforeEach
    void setUp() {
        guessWord = new GuessWord("Monkey");
    }

    @Test
    void testIsReadyDefaultTrue() {
        assertThat(guessWord.isReady()).isTrue();
    }

    @Test
    void testSetIsReady() {
        guessWord.setIsReady(false);
        assertThat(guessWord.isReady()).isFalse();
    }

    @Test
    void testGetRevealCountDefault() {
        assertThat(guessWord.getRevealCount()).isZero();
    }

    @Test
    void testIncrementRevealCount() {
        guessWord.incrementRevealCount();
        assertThat(guessWord.getRevealCount()).isEqualTo(1);
    }

    @Test
    void testResetRevealCount() {
        guessWord.incrementRevealCount();
        guessWord.resetRevealCount();
        assertThat(guessWord.getRevealCount()).isZero();
    }

    @Test
    void testGetMaskedWordDefault() {
        assertThat(guessWord.getMaskedWord()).isEmpty();
    }

    @Test
    void testSetMaskedWord() {
        guessWord.setMaskedWord("******");
        assertThat(guessWord.getMaskedWord()).isEqualTo("******");
    }

    @Test
    void testGetGuessCountDefault() {
        assertThat(guessWord.getGuessCount()).isZero();
    }

    @Test
    void testIncrementGuessCount() {
        guessWord.incrementGuessCount();
        assertThat(guessWord.getGuessCount()).isEqualTo(1);
    }

    @Test
    void testSetGuessCount() {
        guessWord.setGuessCount(5);
        assertThat(guessWord.getGuessCount()).isEqualTo(5);
    }

    @Test
    void testGetGuessWord() {
        assertThat(guessWord.getGuessWord()).isEqualTo("Monkey");
    }

    @Test
    void testSetGuessWord() {
        guessWord.setGuessWord("Banana");
        assertThat(guessWord.getGuessWord()).isEqualTo("Banana");
    }

    @Test
    void testReset() {
        guessWord.reset();
        assertThat(guessWord.getGuessWord()).isNull();
        assertThat(guessWord.getMaskedWord()).isEmpty();
        assertThat(guessWord.getGuessCount()).isZero();
        assertThat(guessWord.getRevealCount()).isZero();
        assertThat(guessWord.isReady()).isFalse();
    }
}