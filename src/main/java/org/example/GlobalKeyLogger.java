package org.example;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class GlobalKeyLogger implements NativeKeyListener {

    private StringBuilder text;
    private boolean isUpperCase;
    private FrequencyTree tree;
    private int numberOfWordsToPredict;
    private String[] predictedWords;

    public GlobalKeyLogger(FrequencyTree tree, int numberOfWordsToPredict) {
        text = new StringBuilder();
        isUpperCase = false;
        this.tree = tree;
        this.numberOfWordsToPredict = numberOfWordsToPredict;
        predictedWords = new String[numberOfWordsToPredict];
    }



    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
//        System.out.println("Taste gedrückt: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
//        System.out.println("Taste gedrückt: " + e.getKeyCode());


        if(e.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK
                || e.getKeyCode() == NativeKeyEvent.VC_SHIFT
                || e.getKeyCode() == 3638) { // 3638 = the shift key on the right
            isUpperCase = !isUpperCase;
            return;
        }

        for (int i = 2; i <= numberOfWordsToPredict+1; i++) {

            if(e.getKeyCode() == i && i-2 < predictedWords.length) {
                //that means the user pressed the number key labeled i-1

                try {
                    AutoTyper.replace(getLastWord(), predictedWords[i-2]);
                } catch (AWTException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if(NativeKeyEvent.getKeyText(e.getKeyCode()).length() != 1
                && e.getKeyCode() != NativeKeyEvent.VC_SPACE
                && e.getKeyCode() != NativeKeyEvent.VC_ENTER
                && e.getKeyCode() != NativeKeyEvent.VC_BACKSPACE
                && e.getKeyCode() != NativeKeyEvent.VC_UP
                && e.getKeyCode() != NativeKeyEvent.VC_DOWN
                && e.getKeyCode() != NativeKeyEvent.VC_LEFT
                && e.getKeyCode() != NativeKeyEvent.VC_RIGHT
                && e.getKeyCode() != 39 //ü
                && e.getKeyCode() != 40 //ä
                && e.getKeyCode() != 41) { //ö
            text.append("_");
            return;
        }


        switch (e.getKeyCode()) {
            case NativeKeyEvent.VC_SPACE, NativeKeyEvent.VC_ENTER:
                text.append(" ");
                break;
            case NativeKeyEvent.VC_BACKSPACE:
                if(!text.isEmpty()) text.deleteCharAt(text.length() - 1);
                break;
            case NativeKeyEvent.VC_UP, NativeKeyEvent.VC_DOWN, NativeKeyEvent.VC_LEFT, NativeKeyEvent.VC_RIGHT:
                //we reset the text and current word
                text = new StringBuilder();
                break;
            case 39: //ü
                text.append(isUpperCase ? "Ü" : "ü");
                break;
            case 40: //ä
                text.append(isUpperCase ? "Ä" : "ä");
                break;
            case 41: //ö
                text.append(isUpperCase ? "Ö" : "ö");
                break;
            default:
                String stringToAppend = isUpperCase
                        ? NativeKeyEvent.getKeyText(e.getKeyCode()).toUpperCase()
                        : NativeKeyEvent.getKeyText(e.getKeyCode()).toLowerCase();
                text.append(stringToAppend);
                break;
        }


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
        if(e.getKeyCode() == NativeKeyEvent.VC_SHIFT
                || e.getKeyCode() == 3638) {
            isUpperCase = !isUpperCase;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}


    public String getText() {
        return text.toString();
    }

    private String getLastWord() {
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
}
