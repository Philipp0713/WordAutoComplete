package org.example;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

public class AutoTyper {

    public static int writingCount = 0;

    private AutoTyper() {}

    /**
     * Writes the String text as if it was written by the users keyboard. It does that by copy-pasting the result at
     * the respective location
     * @param text text to be written
     * @throws AWTException if something goes wrong with writing or copy-pasting
     */
    public static void writeWithClipboard(String text) throws AWTException {
        Robot robot = new Robot();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Alten Inhalt sichern
        Transferable oldContent = clipboard.getContents(null);

        // Neuen Text setzen
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);

        while (true) {
            try {
                String current = (String) clipboard.getData(DataFlavor.stringFlavor);
                if (text.equals(current)) break;
            } catch (Exception ignored) {}
        }

        writingCount++;

        // Einfügen (STRG + V)
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        // kleine Pause (wichtig!)
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}
        writingCount--;

        // Alten Inhalt wiederherstellen
        if (oldContent != null) {
            clipboard.setContents(oldContent, null);
        }
    }

    /**
     * Method used to replace a String word with a String replacement. It does that by repeatedly pressing backspace
     * to delete word and then uses writeWithClipboard() to paste the new word at the position.
     * @param word old word
     * @param replacement replacement word
     * @throws AWTException if something goes wrong with writing or copy-pasting
     */
    public static void replace(String word, String replacement) throws AWTException {
        Robot robot = new Robot();
        writingCount++;

        for (int i = 0; i < word.length() + 1; i++) { //+1 because it also has to delete the number 1, 2, 3, 4, ...
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}
        writingCount--;

        AutoTyper.writeWithClipboard(replacement);
    }
}
