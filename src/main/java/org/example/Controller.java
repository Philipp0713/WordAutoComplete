package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;

import java.awt.event.ActionListener;
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
        logger.addListener(this::updateButtonVisibility);

        ActionListener[] deleteListeners = new ActionListener[numberOfPredictedWords];

        for (int i = 0; i < deleteListeners.length; i++) {
            int finalI = i;
            deleteListeners[i] = e -> {
                logger.deleteWord(logger.getPredictedWords()[finalI]);
            };
        }

        view.addDeleteButtonListeners(deleteListeners);

        ActionListener resetListener = e -> {
            logger.resetText();
            logger.displayPredictedWords();
        };
        view.addResetButtonListener(resetListener);

        ActionListener pauseListener = e -> {
            logger.setPaused(!logger.isPaused());

            if (logger.isPaused()) {
                view.setPauseButtonToPaused();
            } else {
                view.setPauseButtonToPlay();
            }
        };
        view.addPauseButtonListener(pauseListener);
    }

    public void updateTextFields() {
        view.updateTextFields(logger.getPredictedWords());
    }

    public void updateText() {
        view.updateText(logger.getText());
    }

    public void updateButtonVisibility() {
        boolean[] visibility = new boolean[numberOfPredictedWords];
        String[] predictedWords = logger.getPredictedWords();

        for (int i = 0; i < predictedWords.length; i++) {
            visibility[i] = tree.isUserWord(predictedWords[i]);
        }
        view.updateButtonVisibility(visibility);
    }


    public static void main(String[] args) {
        new Controller();
    }
}//jetzt versuchen wir es nochmaldamit wurde der Text repräsentiertjetasdfwafd aspfjasfzt kann man scjetsadftztasdf