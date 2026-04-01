package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;

import java.io.FileNotFoundException;

public class Controller {
    /**
     * Reference to the View class, which manages all the UI-Elements.
     */
    private View view;
    private FrequencyTree tree;
    private GlobalKeyLogger logger;
    private int numberOfPredictedWords;

    public Controller() {
        numberOfPredictedWords = 9;

        view = new View(numberOfPredictedWords);

        try {
            tree = new FrequencyTree();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        logger = new GlobalKeyLogger(tree, numberOfPredictedWords);
        GlobalScreen.addNativeKeyListener(logger);

        logger.addListener(this::updateTextFields);
        logger.addListener(this::updateText);
    }

    public void updateTextFields() {
        view.updateTextFields(logger.getPredictedWords());
    }

    public void updateText() {
        view.updateText(logger.getText());
    }


    public static void main(String[] args) {
        new Controller();
    }
}//