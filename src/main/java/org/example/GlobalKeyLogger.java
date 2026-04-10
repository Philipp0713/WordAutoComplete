package org.example;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalKeyLogger implements NativeKeyListener {

    /**
     * Stores the text listened to by this class.
     */
    private StringBuilder text;

    /**
     * Stores the current position of the cursor. Since this class can only listen to the left/right arrow keys, it is
     * only able to stay consistent regarding these movements. TODO
     */
    private int currentCurserPosition;

    /**
     * Object used to generate the predicted words.
     */
    private FrequencyTree tree;

    /**
     * Stores the number of words that should be predicted and shown to the user at every timeframe.
     */
    private int numberOfPredictedWords;

    /**
     * Stores the predicted words after the last time they were updated using the updatePredictedWords() method.
     */
    private String[] predictedWords;

    /**
     * Stores if the program is paused.
     */
    private boolean paused;

    /**
     * Stores if the ctrl key is pressed.
     */
    private boolean isCtrlPressed;

    /**
     * Stores all the listeners that should be notified when there is a change relevant to them.
     */
    private List<Runnable> listeners;

    public GlobalKeyLogger(FrequencyTree tree, int numberOfPredictedWords) {
        this.paused = false;
        this.text = new StringBuilder();
        this.tree = tree;
        this.numberOfPredictedWords = numberOfPredictedWords;
        this.predictedWords = new String[numberOfPredictedWords];
        this.listeners = new ArrayList<>();
    }

    /**
     * Adds a listener to this class. The listener will be run everytime there is a data change relevant to the
     * respective listener.
     * @param listener the listener
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Runs all the listeners.
     */
    public void notifyAllListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
//        System.out.println("Taste gedrückt: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
//        System.out.println("Taste gedrückt: " + e.getKeyCode());

        // if paused, do nothing
        if(paused) {
            return;
        }


        // only actions from the user should trigger one of the following events
        if(AutoTyper.writingCount > 0) {
            return;
        }

        // it should autocomplete the word at index 0 if the space bar is pressed
//        if (e.getKeyCode() == NativeKeyEvent.VC_SPACE && predictedWords.length > 0) {
//
//            if (isCtrlPressed) {
//                try {
//                    AutoTyper.writeUsingClipboard(" ");
//                } catch (AWTException ignored) {}
//                text.append(" ");
//            } else {
//                new Thread(() -> {
//                    try {
//                        this.writePredictedWord(0);
//                    } catch (AWTException ignored) {}
//                    try {
//                        AutoTyper.writeUsingClipboard(" ");
//                    } catch (AWTException ignored) {}
//                    text.append(" ");
//                    updatePredictedWords();
//                    displayPredictedWords();
//                }).start();
//                return;
//            }
//        }

        // numbers 1 to 9
        for (int i = 2; i <= numberOfPredictedWords+1; i++) {

            if(e.getKeyCode() == i && i-2 < predictedWords.length) {
                //that means the user pressed the number key labeled i-1

                int finalI = i;
                new Thread(() -> {
                    try {
                        this.writePredictedWord(finalI-2);
                    } catch (AWTException ignored) {}
                    updatePredictedWords();
                    displayPredictedWords();
                }).start();
                return;
            }
        }

        switch (e.getKeyCode()) {
            case NativeKeyEvent.VC_BACKSPACE:
                if (!text.isEmpty()) text.deleteCharAt(text.length()-1);
                updatePredictedWords();
                displayPredictedWords();
                break;
            case NativeKeyEvent.VC_UP, NativeKeyEvent.VC_DOWN, NativeKeyEvent.VC_LEFT, NativeKeyEvent.VC_RIGHT:
                // we reset the text
                text = new StringBuilder();
                break;
            case NativeKeyEvent.VC_SPACE:
                tree.addWordToWords(getLastWord());
                break;
            case NativeKeyEvent.VC_CONTROL:
                isCtrlPressed = true;
                break;
        }
    }
    /**
     * Deletes the string word from the database of words used by the program and updates everything accordingly.
     * @param word string that will be deleted
     */
    public void deleteWord(String word) {
        tree.deleteWordFromWords(word);
        updatePredictedWords();
        displayPredictedWords();
    }

    /**
     * Method that updates the array of predicted words to now predict the words of the current (possibly new) last
     * word
     */
    public void updatePredictedWords() {
        this.predictedWords = tree.getAutoCompletedWords(getLastWord(), numberOfPredictedWords);
    }

    /**
     * Displays the current array of predicted words. They are not updated.
     */
    public void displayPredictedWords() {
        notifyAllListeners();
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // if paused, do nothing
        if(paused) {
            return;
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL) {
            isCtrlPressed = false;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {

        // if paused, do nothing
        if(paused) {
            return;
        }

        // only actions from the user should trigger one of the following events
        if(AutoTyper.writingCount > 0) {
            return;
        }

        char c = e.getKeyChar();

        if (c == NativeKeyEvent.CHAR_UNDEFINED || Character.isISOControl(c)) {
            return;
        }

        text.append(c);

        updatePredictedWords();
        displayPredictedWords();
    }


    /**
     * Method that returns the current text that is the result of concatenating all the one character strings
     * from the ArrayList text. So this is a simpler form of the string that the user typed.
     * @return the current text
     */
    public String getText() {
        return text.toString();
    }

    /**
     * Resets the text to an empty string.
     */
    public void resetText() {
        text = new StringBuilder();
    }

    /**
     * Method that returns the last word in text. I.e., the string of characters after the last space.
     * @return last word
     */
    public String getLastWord() {
        String textAsString = this.getText();

        if (textAsString.isEmpty()) {
            return "";
        }

        if (textAsString.charAt(textAsString.length() - 1) == ' ') {
            return "";
        }

        List<String> list = Arrays.stream(textAsString.split(" ")).toList();
        if (list.getLast() == null || list.getLast().trim().isEmpty()) {
            return "";
        }
        return list.getLast();
    }

    /**
     * Method that replaces the last word and the character that chose the predicted word with the predicted word at
     * index indexInPredictedWords
     * @param indexInPredictedWords index of the predicted word
     * @throws AWTException if something goes wrong with writing or copy-pasting
     */
    private void writePredictedWord(int indexInPredictedWords) throws AWTException {
        // first delete the number and update the predicted words
        text.deleteCharAt(text.length()-1);
        updatePredictedWords();

        String textToAppend = predictedWords[indexInPredictedWords];

        AutoTyper.replace(getLastWord(), textToAppend);

        String lastWord = getLastWord();

        for (int i = 0; i < lastWord.length(); i++) {
            text.deleteCharAt(text.length()-1);
        }

        for (int i = 0; i < textToAppend.length(); i++) {
            text.append(textToAppend.charAt(i));
        }
    }

    /**
     * Returns the predicted words for the current state
     * @return the predicted words
     */
    public String[] getPredictedWords() {
        return predictedWords;
    }

    /**
     * Returns if the program is paused.
     * @return if the program is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Sets the paused state of the program.
     * @param paused the new paused state
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
