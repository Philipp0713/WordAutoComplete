package org.example;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalKeyLogger implements NativeKeyListener {

    private ArrayList<String> text;
    private boolean isUpperCase;
    private FrequencyTree tree;
    private int numberOfWordsToPredict;
    private String[] predictedWords;

    public GlobalKeyLogger(FrequencyTree tree, int numberOfWordsToPredict) {
        text = new ArrayList<>();
        isUpperCase = false;
        this.tree = tree;
        this.numberOfWordsToPredict = numberOfWordsToPredict;
        predictedWords = new String[numberOfWordsToPredict];
    }



    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
//        System.out.println("Taste gedrückt: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
//        System.out.println("Taste gedrückt: " + e.getKeyCode());

        // only actions from the user should trigger one of the following events
        if(AutoTyper.writingCount > 0) {
            return;
        }


        if(e.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK
                || e.getKeyCode() == NativeKeyEvent.VC_SHIFT
                || e.getKeyCode() == 3638) { // 3638 = the shift key on the right
            isUpperCase = !isUpperCase;
            return;
        }

        for (int i = 2; i <= numberOfWordsToPredict+1; i++) {

            if(e.getKeyCode() == i && i-2 < predictedWords.length) {
                //that means the user pressed the number key labeled i-1

                int finalI = i;
                new Thread(() -> {
                    try {
                        this.writePredictedWord(finalI-2);
                    } catch (AWTException ex) {
                        throw new RuntimeException(ex);
                    }
                    displayWordsToPredict();
                }).start();
                return;
            }
        }


        switch (e.getKeyCode()) {
            case NativeKeyEvent.VC_SPACE, NativeKeyEvent.VC_ENTER:
                text.add(" ");
                break;
            case NativeKeyEvent.VC_BACKSPACE:
                if(!text.isEmpty()) text.removeLast();
                break;
            case NativeKeyEvent.VC_UP, NativeKeyEvent.VC_DOWN, NativeKeyEvent.VC_LEFT, NativeKeyEvent.VC_RIGHT:
                //we reset the text and current word
                text = new ArrayList<>();
                break;
            case 39: //ü
                text.add(isUpperCase ? "Ü" : "ü");
                break;
            case 40: //ä
                text.add(isUpperCase ? "Ä" : "ä");
                break;
            case 41: //ö
                text.add(isUpperCase ? "Ö" : "ö");
                break;
            default:
                String stringToAppend = isUpperCase
                        ? NativeKeyEvent.getKeyText(e.getKeyCode()).toUpperCase()
                        : NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
                text.add(stringToAppend);
                break;
        }


        displayWordsToPredict();
    }


    public void displayWordsToPredict() {
        for (int i = 0; i < 20; i++) {
            System.out.println();
        }
        this.predictedWords = tree.getAutoCompletedWords(getLastWord(), numberOfWordsToPredict);
        for (int i = 0; i < predictedWords.length; i++) {
            System.out.println((i+1) + ": " + predictedWords[i]);
        }
        System.out.println(getText());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {

        switch(e.getKeyCode()) {
            case NativeKeyEvent.VC_SHIFT, 3638:
                isUpperCase = !isUpperCase;
                break;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}


    public String getText() {
        return text.stream()
                //if length of string is not equal to 1, it is not supported
                .map(s -> s.length() == 1 ? s : "_")
                .collect(Collectors.joining());
    }

    /**
     * Method, that returns the last word in text. I.e., the string of characters after the last space.
     * @return last word
     */
    public String getLastWord() {
        String text = this.getText();

        if (text.isEmpty()) {
            return "";
        }

        if (text.charAt(text.length() - 1) == ' ') {
            return "";
        }

        List<String> list = Arrays.stream(text.split(" ")).toList();
        if (list.getLast() == null || list.getLast().trim().isEmpty()) {
            return "";
        }
        return list.getLast();
    }

    private void writePredictedWord(int indexInPredictedWords) throws AWTException {
        String textToAppend = predictedWords[indexInPredictedWords];

        AutoTyper.replace(getLastWord(), textToAppend);

        for (int j = getLastWord().length(); j < textToAppend.length(); j++) {
            text.add("" + textToAppend.charAt(j));
        }
    }

    /**
     * Appends the given character ch to the end of the text
      * @param ch the character to append
     */
    public void addToText(char ch) {
        text.add("" + ch);
    }

    /**
     * Returns the predicted words for the current state
     * @return the predicted words
     */
    public String[] getPredictedWords() {
        return predictedWords;
    }
}
