package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class View {

    /**
     * Stores the number of rows in the table. I.e. the number of autocompleted words that will be shown.
     */
    private final int numberOfRows;
    /**
     * Stores all the text fields where the autocompleted words will be displayed.
     */
    private final JTextField[] textFields;
    /**
     * Stores all the buttons used to delete the respective word from the database.
     */
    private final JButton[] buttons;
    /**
     * Stores the String of key inputs listened to by the program.
     */
    private final JTextField text;

    public View(int numberOfRows) {
        this.numberOfRows = numberOfRows;

        textFields = new JTextField[numberOfRows];
        buttons = new JButton[numberOfRows];

        for (int i = 0; i < numberOfRows; i++) {
            textFields[i] = new JTextField("");
            buttons[i] = new JButton("delete");
        }

        text = new JTextField("");


        JFrame frame = new JFrame("Word Autocomplete");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);
        frame.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new GridBagLayout());
        ((GridBagLayout) panel.getLayout()).columnWidths = new int[] {90, 50, 0};
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create normal rows
        for (int i = 0; i < numberOfRows; i++) {
            gbc.gridy = i;

            // Button (column 0)
            gbc.gridx = 0;
            gbc.weightx = 0;
            buttons[i].setPreferredSize(new Dimension(80, 20));
            panel.add(buttons[i], gbc);

            // Information (column 1)
            gbc.gridx = 1;
            gbc.weightx = 0;
            JTextField informationField = new JTextField("word " + (i+1));
            informationField.setOpaque(false);              // No background
            informationField.setBorder(null);               // Remove border
            informationField.setEditable(false);
            informationField.setHorizontalAlignment(JTextField.CENTER);
            panel.add(informationField, gbc);

            // TextField (column 2)
            gbc.gridx = 2;
            gbc.weightx = 1;
            textFields[i].setEditable(false);
            textFields[i].setBackground(Color.LIGHT_GRAY);
            panel.add(textFields[i], gbc);
        }

        // Last row (spanning both columns)
        gbc.gridy = numberOfRows;
        gbc.gridx = 0;
        gbc.gridwidth = 3; // span across all columns
        gbc.weightx = 1;

        text.setEditable(false);
        text.setBackground(Color.LIGHT_GRAY);
        text.setHorizontalAlignment(JTextField.RIGHT);

        panel.add(text, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    public void updateTextFields(String[] newContent) {
        if (newContent == null) {
            return;
        }

        for (int i = 0; i < numberOfRows; i++) {
            if (i < newContent.length) {
                textFields[i].setText(newContent[i]);
            } else {
                textFields[i].setText("");
            }
        }
    }

    public void updateText(String newText) {
        text.setText(newText);
        text.setCaretPosition(newText.length());
    }

    public void addButtonListeners(ActionListener[] listeners) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].addActionListener(listeners[i]);
        }
    }

    public void updateButtonVisibility(boolean[] visibility) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setVisible(visibility[i]);
        }
    }
}
