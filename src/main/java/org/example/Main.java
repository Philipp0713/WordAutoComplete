package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import java.awt.*;
import java.io.FileNotFoundException;

public class Main {
    public static final int NUMBER_OF_WORDS_TO_PREDICT = 9;
    public static final boolean ONLY_PREFIXES = true;


    public static void main(String[] args) {

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

        GlobalKeyLogger logger = new GlobalKeyLogger(tree, NUMBER_OF_WORDS_TO_PREDICT);
        GlobalScreen.addNativeKeyListener(logger);
    }
}//äua4$