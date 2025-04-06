package com.ruppyrup.server.utils;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@UtilityClass
public class WordMasker {

    private static final Random random = new Random();

    public String getMaskedWord(String guessWord, String maskedWord, int revealCount) {
        if (maskedWord.isEmpty() || revealCount == 0) {
            return maskWholeWord(guessWord);
        }

        if (revealCount >= guessWord.length()) {
            return guessWord;
        }

        List<Integer> stars = new ArrayList<>();
        List<Integer> reveals = new ArrayList<>();

        populatePositions(maskedWord, stars, reveals);

        if (reveals.size() >= revealCount) {
            return maskedWord;
        }

        int charsToReveal = revealCount - reveals.size();
        if (charsToReveal >= stars.size()) {
            return guessWord;
        }

        while (charsToReveal > 0) {
            int indexOfStarToReplace = random.nextInt(stars.size());
            int charToReveal = stars.get(indexOfStarToReplace);
            stars.remove(indexOfStarToReplace);
            System.out.println("charToReveal = " + charToReveal);
            maskedWord = maskedWord.substring(0, charToReveal) + guessWord.charAt(charToReveal) + maskedWord.substring(charToReveal + 1);
            charsToReveal--;
        }

        return maskedWord;
    }

    private static void populatePositions(String maskedWord, List<Integer> stars, List<Integer> reveals) {
        int index = 0;
        for (String s : maskedWord.split("")) {
            if (s.equals("*")) {
                stars.add(index);
            } else {
                reveals.add(index);
            }
            index++;
        }
    }

    public String maskWholeWord(String guessWord) {
        StringBuilder stringBuilder = new StringBuilder();

        IntStream.range(0, guessWord.length())
                .forEach(i -> stringBuilder.append("*"));
        return stringBuilder.toString();
    }

    public int starMaskCounter(String maskedWord) {
        int countStars = 0;
        for (String s : maskedWord.split("")) {
            if (s.equals("*")) {
                countStars++;
            }
        }
        return countStars;
    }
}
