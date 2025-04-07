package com.ruppyrup.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordMaskerTest {

    @Test
    void testMaskWordWithNoReveals() {
        String guessWord = "hello";
        String maskedWord = "";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord,0);

        assertEquals("*****", result);
    }

    @Test
    void testMaskWordWithReveals() {
        String guessWord = "hello";
        String maskedWord = "*****";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 5);

        assertEquals("hello", result);
    }

    @Test
    void maskedWordReturnsSameIfRevealCountAlreadyRevealed() {
        String guessWord = "hello";
        String maskedWord = "h****";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 1);

        assertEquals("h****", result);
    }

    @Test
    void maskedWordReturnsSingleChar() {
        String guessWord = "hello";
        String maskedWord = "*****";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 1);

        int starCount = WordMasker.starMaskCounter(result);

        assertEquals(4, starCount);
    }

    @Test
    void maskedWordReplacesOneChar() {
        String guessWord = "hello";
        String maskedWord = "***l*";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 2);

        int starCount = WordMasker.starMaskCounter(result);
        System.out.println("maskedWord = " + result);
        assertEquals(3, starCount);
    }

    @Test
    void maskedWordReturnsReplacesAllCharsWhenRevealCountIsSameAsGuessWordLength() {
        String guessWord = "hello";
        String maskedWord = "***l*";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 5);

        assertEquals(guessWord, result);
    }


    @Test
    void maskedWordReturnsReplacesMultipleChars() {
        String guessWord = "hello";
        String maskedWord = "***l*";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 3);

        int starCount = WordMasker.starMaskCounter(result);
        System.out.println("maskedWord = " + result);

        assertEquals(2, starCount);
    }

    @Test
    void maskedWordReturnsSameIfAlreadyRevealedNumber() {
        String guessWord = "hello";
        String maskedWord = "h**l*";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 2);

        assertEquals("h**l*", result);
    }

    @Test
    void maskedWordReturnsGuessWordIfRevealCountIsGreaterThanGuessWordLength() {
        String guessWord = "hello";
        String maskedWord = "h**l*";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 6);

        assertEquals("hello", result);
    }

    @Test
    void maskedWordRetursSameIfRevealCountLessThanCurrentlyRevealed() {
        String guessWord = "hello";
        String maskedWord = "he*l*";
        String result = WordMasker.getMaskedWord(guessWord, maskedWord, 2);

        assertEquals("he*l*", result);
    }

}