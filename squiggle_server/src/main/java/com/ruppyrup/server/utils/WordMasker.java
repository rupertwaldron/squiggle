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


        int starred = starMaskCounter(maskedWord);

        List<Integer> stars = new ArrayList<>();
        List<Integer> reveals = new ArrayList<>();

        int index = 0;
        for (String s : maskedWord.split("")) {
            if (s.equals("*")) {
                stars.add(index);
            } else {
                reveals.add(index);
            }
            index++;
        }

        if (reveals.size() >= revealCount) {
            return maskedWord;
        }

        int charsToReveal = revealCount - reveals.size();
        if (charsToReveal >= stars.size()) {
            return guessWord;
        }

//        for (int i = 0; i < charsToReveal; i++) {
//            if (stars.isEmpty()) {
//                break;
//            }
//            int charToReveal = stars.get(random.nextInt(stars.size()));
//            maskedWord = maskedWord.substring(0, charToReveal) + guessWord.charAt(charToReveal) + maskedWord.substring(charToReveal + 1);
//            stars.remove((Integer) charToReveal);
//        }

        if (stars.size() == guessWord.length()) {
            int charToReveal = stars.get(random.nextInt(stars.size()));
            maskedWord = maskedWord.substring(0, charToReveal) + guessWord.charAt(charToReveal) + maskedWord.substring(charToReveal + 1);
        } else {
            int charToReveal = stars.get(random.nextInt(stars.size()));
            System.out.println("charToReveal = " + charToReveal);
            maskedWord = maskedWord.substring(0, charToReveal) + guessWord.charAt(charToReveal) + maskedWord.substring(charToReveal + 1);
        }

        return maskedWord;


//        for (int i = 0; i < revealCount; i++) {
//            for (String s : guessWord.split("")) {
//                if (s.equals("*")) {
//
//                }
//            }
//            int charToReveal = (int) (Math.random() * guessWord.length());
//            stringBuilder.setCharAt(charToReveal, guessWord.charAt(charToReveal));
//        }
//
//        return stringBuilder.toString();
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
