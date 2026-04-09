package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;

import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class Controller {
    /**
     * Reference to the View class, which manages all the UI-Elements.
     */
    private final View view;
    private FrequencyTree tree;
    private GlobalKeyLogger logger;
    private int numberOfPredictedWords;

    public Controller() {
        numberOfPredictedWords = 9;
        boolean attentionToLowerUppercase = false;


        view = new View(numberOfPredictedWords);
        view.setAttentionToLowerUppercase(attentionToLowerUppercase);

        try {
            tree = new FrequencyTree(attentionToLowerUppercase);
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

        view.addResetButtonListener(e -> {
            logger.resetText();
            logger.displayPredictedWords();
        });

        view.addPauseButtonListener(e -> {
            logger.setPaused(!logger.isPaused());

            if (logger.isPaused()) {
                view.setPauseButtonToPaused();
            } else {
                view.setPauseButtonToPlay();
            }
        });

        view.addSettingsButtonListener(e -> {
            logger.setPaused(true);
            view.setPlayIcon();
            view.showSettingsMenu();
        });

        view.addAttentionToLowerUppercaseListener(e -> {
            tree.setAttentionToLowerUppercase(!tree.isAttentionToLowerUppercase());
            view.setAttentionToLowerUppercase(tree.isAttentionToLowerUppercase());
        });
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
}//testSchlechtes Autismus-Spektrum-Störung asdf as