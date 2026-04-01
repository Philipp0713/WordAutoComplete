package org.example;

import com.github.kwhat.jnativehook.GlobalScreen;
import java.awt.*;
import java.io.FileNotFoundException;

public class Main {

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

        GlobalKeyLogger logger = new GlobalKeyLogger(tree, 9);
        GlobalScreen.addNativeKeyListener(logger);
    }
}//somit ist das grundsätzliche Konzept abgeschlossen. Jetzt scheint es auch besser zu funktionieren.