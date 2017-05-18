package com.syzible.loinnir.utils;

import java.util.HashMap;

/**
 * Created by ed on 17/05/2017.
 */

public class LanguageUtils {
    private static HashMap<String, String> lenitionRules;

    static {
        // b c d f g m p s t
        lenitionRules = new HashMap<>();
        lenitionRules.put("b", "bh");
        lenitionRules.put("c", "ch");
        lenitionRules.put("d", "dh");
        lenitionRules.put("f", "fh");
        lenitionRules.put("g", "gh");
        lenitionRules.put("m", "mh");
        lenitionRules.put("p", "ph");
        lenitionRules.put("s", "sh");
        lenitionRules.put("t", "th");

        lenitionRules.put("B", "Bh");
        lenitionRules.put("C", "Ch");
        lenitionRules.put("D", "Dh");
        lenitionRules.put("F", "Fh");
        lenitionRules.put("G", "Gh");
        lenitionRules.put("M", "Mh");
        lenitionRules.put("P", "Ph");
        lenitionRules.put("S", "Sh");
        lenitionRules.put("T", "Th");
    }

    private static HashMap<String, String> eclipsisRules;

    static {
        eclipsisRules = new HashMap<>();
        eclipsisRules.put("a", "n-a");
        eclipsisRules.put("A", "nA");
        eclipsisRules.put("e", "n-e");
        eclipsisRules.put("E", "nE");
        eclipsisRules.put("i", "n-i");
        eclipsisRules.put("I", "nI");
        eclipsisRules.put("o", "n-o");
        eclipsisRules.put("O", "nO");
        eclipsisRules.put("u", "n-u");
        eclipsisRules.put("U", "nU");

        eclipsisRules.put("á", "n-á");
        eclipsisRules.put("Á", "nÁ");
        eclipsisRules.put("é", "n-é");
        eclipsisRules.put("É", "nÉ");
        eclipsisRules.put("í", "n-í");
        eclipsisRules.put("Í", "nÍ");
        eclipsisRules.put("ó", "n-ó");
        eclipsisRules.put("Ó", "nÓ");
        eclipsisRules.put("ú", "n-ú");
        eclipsisRules.put("Ú", "nÚ");

        eclipsisRules.put("b", "mb");
        eclipsisRules.put("c", "gc");
        eclipsisRules.put("d", "nd");
        eclipsisRules.put("f", "bhf");
        eclipsisRules.put("g", "ng");
        eclipsisRules.put("p", "bp");
        eclipsisRules.put("t", "dt");

        eclipsisRules.put("B", "mB");
        eclipsisRules.put("C", "gC");
        eclipsisRules.put("D", "nD");
        eclipsisRules.put("F", "bhF");
        eclipsisRules.put("G", "nG");
        eclipsisRules.put("P", "bP");
        eclipsisRules.put("T", "dT");
    }

    private static String getMutation(HashMap<String, String> map, String input) {
        String initial = String.valueOf(input.charAt(0));
        if (!map.containsKey(initial))
            return input;

        String mutation = map.get(initial);
        String strippedInput = input.substring(1);
        return mutation + strippedInput;
    }

    private static String lenite(String input) {
        return getMutation(lenitionRules, input);
    }

    private static String eclipse(String input) {
        return getMutation(eclipsisRules, input);
    }

    private static boolean isVowel(String letter) {
        return letter.matches("a|e|i|o|u|á|é|í|ó|ú");
    }

    private static String palatalise(String input) {
        // Seán -> Seáin
        int wordLength = input.length();
        return input.substring(0, wordLength - 1) + "i" + input.substring(wordLength - 1, wordLength);
    }

    public static String getVocative(String input) {
        // check exceptions
        if (input.equals("Micheál"))
            return "Mhíchíl";

        if (input.equals("Liam"))
            return input;

        // check if the word is broad
        int wordLength = input.length();
        String secondLastLetter = String.valueOf(input.charAt(wordLength - 2));
        System.out.println(secondLastLetter);
        boolean hasBroadFinal = secondLastLetter.toLowerCase().matches("a|á");

        // check it's not starting with a vowel
        String firstLetter = String.valueOf(input.charAt(0));
        boolean hasVowel = isVowel(firstLetter.toLowerCase());

        if (!hasBroadFinal)
            return input;

        if (!hasVowel)
            return lenite(palatalise(input));

        return palatalise(input);
    }
}
