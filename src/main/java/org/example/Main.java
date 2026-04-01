package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import java.awt.*;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) {

        int numberOfPredictedWords = 9;

        View view = new View(numberOfPredictedWords);

        FrequencyTree tree;
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

        GlobalKeyLogger logger = new GlobalKeyLogger(tree, view, numberOfPredictedWords);
        GlobalScreen.addNativeKeyListener(logger);
    }
}//