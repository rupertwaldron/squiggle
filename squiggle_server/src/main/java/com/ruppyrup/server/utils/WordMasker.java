package com.ruppyrup.server.utils;

import lombok.experimental.UtilityClass;

import java.util.stream.IntStream;

@UtilityClass
public class WordMasker {

    public String getMaskedWord(String guessWord, int revealCount) {
        StringBuilder stringBuilder = new StringBuilder();

        IntStream.range(0, guessWord.length())
                .forEach(i -> stringBuilder.append("*"));

        for (int i = 0; i < revealCount; i++) {
            int charToReveal = (int) (Math.random() * guessWord.length());
            stringBuilder.setCharAt(charToReveal, guessWord.charAt(charToReveal));
        }

        return stringBuilder.toString();
    }
}
