package org.example;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;

public class AutoTyper {

    /**
     * Number of currently active writers. A writer is active only when is writing some text and a few milliseconds
     * after that.
     */
    public static int writingCount = 0;

    private AutoTyper() {}

    /**
     * Writes the String text as if it was written by the users keyboard. It does that by copy-pasting the result at
     * the respective location.
     * @param text text to be written
     * @throws AWTException if something goes wrong with writing or copy-pasting
     */
    public static void writeUsingClipboard(String text) throws AWTException {
        Robot robot = new Robot();

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // Alten Inhalt sichern
        Transferable oldContent = clipboard.getContents(null);

        // Neuen Text setzen
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
                // optional: Logging oder Reaktion
            }
        });

        while (true) {
            try {
                String current = (String) clipboard.getData(DataFlavor.stringFlavor);
                if (text.equals(current)) break;
            } catch (Exception ignored) {}

            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}

        writingCount++;

        // Einfügen (STRG + V)
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        // kleine Pause (wichtig!)
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        writingCount--;

        // Alten Inhalt wiederherstellen
        if (oldContent != null) {
            clipboard.setContents(oldContent, null);
        }
    }

    /**
     * Method used to replace a String word with a String replacement. It does that by repeatedly pressing backspace
     * to delete the word and then uses writeWithClipboard() to paste the new word at the position.
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

        AutoTyper.writeUsingClipboard(replacement);
    }
}
