package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class View {

    private final int numberOfRows;
    private final JTextField[] textFields;
    private final JButton[] buttons;
    private final JTextField text;

    public View(int numberOfRows) {
        this.numberOfRows = numberOfRows;

        textFields = new JTextField[numberOfRows];
        buttons = new JButton[numberOfRows];

        for (int i = 0; i < numberOfRows; i++) {
            textFields[i] = new JTextField("");
            buttons[i] = new JButton("delete word " + (i+1));
        }

        text = new JTextField("");


        JFrame frame = new JFrame("Word Autocomplete");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create normal rows
        for (int i = 0; i < numberOfRows; i++) {
            gbc.gridy = i;

            // Button (column 0)
            gbc.gridx = 0;
            gbc.weightx = 0;
            panel.add(buttons[i], gbc);

            // TextField (column 1)
            gbc.gridx = 1;
            gbc.weightx = 1;
            textFields[i].setEditable(false);
            textFields[i].setBackground(Color.LIGHT_GRAY);
            panel.add(textFields[i], gbc);
        }

        // Last row (spanning both columns)
        gbc.gridy = numberOfRows;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // span across both columns
        gbc.weightx = 1;

        text.setEditable(false);
        text.setBackground(Color.LIGHT_GRAY);
        text.setHorizontalAlignment(JTextField.RIGHT);
        text.setCaretPosition(text.getText().length());

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
    }
}
