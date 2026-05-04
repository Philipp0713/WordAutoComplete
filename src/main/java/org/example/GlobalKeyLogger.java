package org.example;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private final FrequencyTree tree;

    /**
     * Stores the number of words that should be predicted and shown to the user at every timeframe.
     */
    private int numberOfPredictedWords;

    /**
     * Stores the predicted words after the last time they were updated using the updatePredictedWords() method.
     * This will be the predicted words used in the internal logic.
     */
    private String[] predictedWordsLogic;

    /**
     * Stores the predicted words after the last time they were updated using the updatePredictedWords() method.
     * This will be the actual predicted words that are shown to the user.
     */
    private String[] predictedWordsEdited;

    /**
     * Stores if the program is paused.
     */
    private boolean paused;

    /**
     * Stores if the ctrl key is pressed.
     */
    private boolean isCtrlPressed;

    /**
     * Stores if a space should be added to the end of an autocompleted word.
     */
    private boolean addSpaceAfterAutocompletion;

    /**
     * Stores all the listeners that should be notified when there is a change relevant to them.
     */
    private final List<Runnable> listeners;

    public GlobalKeyLogger(FrequencyTree tree, int numberOfPredictedWords, boolean addSpaceAfterAutocompletion) {
        this.paused = false;
        this.text = new StringBuilder();
        this.tree = tree;
        this.numberOfPredictedWords = numberOfPredictedWords;
        this.addSpaceAfterAutocompletion = addSpaceAfterAutocompletion;
        this.predictedWordsLogic = new String[numberOfPredictedWords];
        this.predictedWordsEdited = new String[numberOfPredictedWords];
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

    /**
     * Returns for a given string, if it contains whitespace.
     * @param string the given string
     * @return if it contains whitespace
     */
    private boolean hasWhitespace(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isWhitespace(string.charAt(i))) {
                return true;
            }
        }
        return false;
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

        // numbers 1 to 9
        for (int i = 2; i <= numberOfPredictedWords+1; i++) {

            if(e.getKeyCode() == i && i-2 < predictedWordsEdited.length) {
                //that means the user pressed the number key labeled i-1

                int finalI = i;
                new Thread(() -> {
                    String predictedWord = predictedWordsLogic[finalI-2];

                    try {
                        this.writePredictedWord(finalI-2);
                    } catch (AWTException ignored) {}
                    updatePredictedWords();
                    displayPredictedWords();

                    if (addSpaceAfterAutocompletion || this.hasWhitespace(predictedWord)) {
                        tree.addPraseToWords(predictedWord);
                    }
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
        this.predictedWordsLogic = tree.getAutoCompletedWords(getLastWord(), numberOfPredictedWords);
        this.predictedWordsEdited = Arrays.copyOf(predictedWordsLogic, predictedWordsLogic.length);

        if (addSpaceAfterAutocompletion) {
            for (int i = 0; i < predictedWordsEdited.length; i++) {

                if (predictedWordsEdited[i].charAt(predictedWordsEdited[i].length()-1) != ' ') {
                    predictedWordsEdited[i] += " ";
                }
            }
        }
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

        return getLastWordOfString(textAsString);
    }

    /**
     * Method that returns the last word in text without the last spaces.
     * @return last word without the last spaces
     */
    public String getLastWordWithoutSpace() {
        String textAsString = this.getText().trim();

        return getLastWordOfString(textAsString);
    }

    /**
     * Method that returns the last word in the String textAsString.
     * @param textAsString String that will be split
     * @return last word in the String textAsString
     */
    private String getLastWordOfString(String textAsString) {
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

        String textToAppend = predictedWordsEdited[indexInPredictedWords];

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
     * Returns the predicted words for the current state that are shown to the user.
     * @return the predicted words
     */
    public String[] getPredictedWordsEdited() {
        return predictedWordsEdited;
    }

    /**
     * Returns the predicted words that are used internally by the program.
     * @return the predicted words
     */
    public String[] getPredictedWordsLogic() {
        return predictedWordsLogic;
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

    /**
     * Returns if a space should be added to the end of an autocompleted word.
     * @return if a space should be added to the end of an autocompleted word
     */
    public boolean isAddSpaceAfterAutocompletion() {
        return addSpaceAfterAutocompletion;
    }

    /**
     * Sets if a space should be added to the end of an autocompleted word.
     * @param addSpaceAfterAutocompletion if a space should be added to the end of an autocompleted word
     */
    public void setAddSpaceAfterAutocompletion(boolean addSpaceAfterAutocompletion) {
        this.addSpaceAfterAutocompletion = addSpaceAfterAutocompletion;
    }
}
