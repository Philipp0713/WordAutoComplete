package org.example;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

public class AutoTyper {


    //TODO: Das andere so umbauen, dass das funktioniert
    public static void writeWithClipboard(String text) throws AWTException {
        Robot robot = new Robot();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Alten Inhalt sichern
        Transferable oldContent = clipboard.getContents(null);

        // Neuen Text setzen
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);

        // Einfügen (STRG + V)
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        // kleine Pause (wichtig!)
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        // Alten Inhalt wiederherstellen
        if (oldContent != null) {
            clipboard.setContents(oldContent, null);
        }
    }


    public static void write(String text) throws AWTException {
        Robot robot = new Robot();


        for (char c : text.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

            if(Character.isUpperCase(c)) {
                robot.keyPress(KeyEvent.VK_SHIFT);
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
                robot.keyRelease(KeyEvent.VK_SHIFT);
            } else {
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            }
        }
    }

    public static void replace(String word, String replacement) throws AWTException {
        Robot robot = new Robot();

        for (int i = 0; i < word.length() + 1; i++) { //+1 because it also has to delete the number 1, 2, 3, 4, ...
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        }

        AutoTyper.write(replacement);
    }
}
