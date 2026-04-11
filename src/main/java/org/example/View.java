package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;

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
     * Stores all the deleteButtons used to delete the respective word from the database.
     */
    private final JButton[] deleteButtons;
    /**
     * Stores the String of key inputs listened to by the program.
     */
    private final JTextField text;

    private final JButton settingsButton;

    private final JButton pauseButton;

    private final JButton infoButton;

    private final JButton resetButton;

    private final ImageIcon pauseIcon;

    private final ImageIcon playIcon;

    private final JFrame frame;

    private final JCheckBox attentionToLowerUppercaseCheckbox;

    private final JCheckBox addSpaceAfterAutocompletionCheckbox;

    private final JComboBox<String> methodUsedToGetWords;

    /**
     * Constructor for the View class.
     * @param numberOfRows 0 < numberOfRows <= 10
     * @param attentionToLowerUppercase true if the program should pay attention to the case of the letters.
     * @param methodIndex the method that should be used to get the suggestions
     *                    0: lastWord is a prefix of the suggestions
     *                    1: lastWord is an infix of the suggestions
     *                    2: lastWord is a subsequence of the suggestions
     */
    public View(int numberOfRows, boolean attentionToLowerUppercase, boolean addSpaceAfterAutocompletion, int methodIndex) {
        this.numberOfRows = numberOfRows;
        int numberOfColumns = 3;

        textFields = new JTextField[numberOfRows];
        deleteButtons = new JButton[numberOfRows];


        ImageIcon deleteIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/delete.png"))
        );
        deleteIcon.setImage(deleteIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        for (int i = 0; i < numberOfRows; i++) {
            textFields[i] = new JTextField("");

            deleteButtons[i] = new JButton(deleteIcon);
            formatButton(deleteButtons[i]);
            deleteButtons[i].setVisible(false);
            deleteButtons[i].setPreferredSize(new Dimension(20, 20));
            deleteButtons[i].setToolTipText("Wort aus Liste löschen.");
        }

        ImageIcon settingsIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/settings.png"))
        );
        settingsIcon.setImage(settingsIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        settingsButton = new JButton(settingsIcon);
        formatButton(settingsButton);
        settingsButton.setToolTipText("Einstellungen.");


        ImageIcon infoIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/info.png"))
        );
        infoIcon.setImage(infoIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        infoButton = new JButton(infoIcon);
        formatButton(infoButton);
        infoButton.setToolTipText("Informationen.");


        ImageIcon resetIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/reset.png"))
        );
        resetIcon.setImage(resetIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        resetButton = new JButton(resetIcon);
        formatButton(resetButton);
        resetButton.setToolTipText("Text zurücksetzen.");


        pauseIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/pause.png"))
        );
        pauseIcon.setImage(pauseIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        pauseButton = new JButton(pauseIcon);
        formatButton(pauseButton);
        pauseButton.setToolTipText("Programm pausieren.");

        playIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/play.png"))
        );
        playIcon.setImage(playIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));




        text = new JTextField("");


        frame = new JFrame("Word Autocomplete");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);
        frame.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new GridBagLayout());
        ((GridBagLayout) panel.getLayout()).columnWidths = new int[] {30, 30, 0};
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create normal rows
        for (int i = 0; i < numberOfRows; i++) {
            gbc.gridy = i;

            // Button (column 0)
            gbc.gridx = 0;
            gbc.weightx = 0;
            panel.add(deleteButtons[i], gbc);

            // Information (column 1)
            gbc.gridx = 1;
            gbc.weightx = 0;



            ImageIcon numberIcon = new ImageIcon(
                    Objects.requireNonNull(getClass().getResource("/numbers/number"+(i+1)+".png"))
            );
            numberIcon.setImage(numberIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            JLabel numberLabel = new JLabel(numberIcon);
            panel.add(numberLabel, gbc);



            // TextField (column 2)
            gbc.gridx = 2;
            gbc.weightx = 1;
            textFields[i].setEditable(false);
            textFields[i].setBackground(Color.LIGHT_GRAY);
            panel.add(textFields[i], gbc);
        }

        // next row (spanning both columns)
        gbc.gridy = numberOfRows;
        gbc.gridx = 0;
        gbc.gridwidth = numberOfColumns; // span across all columns
        gbc.weightx = 1;

        text.setEditable(false);
        text.setBackground(Color.LIGHT_GRAY);
        text.setHorizontalAlignment(JTextField.RIGHT);

        panel.add(text, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        buttonPanel.add(settingsButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(pauseButton);

        gbc.gridx = 0;
        gbc.gridy = numberOfRows + 1;
        gbc.gridwidth = numberOfColumns;

        panel.add(buttonPanel, gbc);

        frame.add(panel);
        frame.setVisible(true);


        attentionToLowerUppercaseCheckbox = new JCheckBox(
                "Textvervollständigung soll auf Groß- und Kleinschreibung achten ", attentionToLowerUppercase
        );

        addSpaceAfterAutocompletionCheckbox = new JCheckBox(
                "<html><body style='width: 300px'>" +
                        "Wenn ein Wort vervollständigt wird, soll immer ein Leerzeichen am Ende hinzugefügt werden." +
                        "</body></html>",
                addSpaceAfterAutocompletion
        );

        String[] options = {
                "Buchstaben sind Präfix des Wortes.",
                "Buchstaben sind Infix des Wortes.",
                "Buchstaben sind Teilfolge des Wortes."
        };
        methodUsedToGetWords = new JComboBox<>(options);
        setMethodUsedToGetWords(methodIndex);
    }

    public void setAttentionToLowerUppercase(boolean attentionToLowerUppercase) {
        this.attentionToLowerUppercaseCheckbox.setSelected(attentionToLowerUppercase);
    }

    public void addAttentionToLowerUppercaseListener(ActionListener listener) {
        attentionToLowerUppercaseCheckbox.addActionListener(listener);
    }

    public void setAddSpaceAfterAutocompletion(boolean addSpaceAfterAutocompletion) {
        this.addSpaceAfterAutocompletionCheckbox.setSelected(addSpaceAfterAutocompletion);
    }

    public void addAddSpaceAfterAutocompletionListener(ActionListener listener) {
        addSpaceAfterAutocompletionCheckbox.addActionListener(listener);
    }

    public void setMethodUsedToGetWords(int index) {
        if (index < 0 || index >= methodUsedToGetWords.getItemCount()) {
            return;
        }
        methodUsedToGetWords.setSelectedIndex(index);
    }

    public int getMethodUsedToGetWords() {
        return methodUsedToGetWords.getSelectedIndex();
    }

    public void addMethodUsedToGetWordsListener(ActionListener listener) {
        methodUsedToGetWords.addActionListener(listener);
    }


    public void showSettingsMenu() {
        JDialog dialog = new JDialog(frame, "Settings", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new GridLayout(5, 1, 8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));


        JTextArea explanationText = new JTextArea("Hier kann ausgewählt werden, wie die Wörter vervollständigt werden.");
        explanationText.setEditable(false);
        explanationText.setLineWrap(true);
        explanationText.setWrapStyleWord(true);
        explanationText.setOpaque(false);
        explanationText.setBorder(null);
        content.add(explanationText);


        content.add(methodUsedToGetWords);


        content.add(attentionToLowerUppercaseCheckbox);


        content.add(addSpaceAfterAutocompletionCheckbox);


        JCheckBox alwaysOnTopBox = new JCheckBox("Fenster ist immer im Vordergrund", frame.isAlwaysOnTop());
        alwaysOnTopBox.addActionListener(e -> frame.setAlwaysOnTop(alwaysOnTopBox.isSelected()));
        content.add(alwaysOnTopBox);



        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonRow.add(closeButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttonRow, BorderLayout.SOUTH);
        dialog.setPreferredSize(new Dimension(450, 300));
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private static void formatButton(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    public void updateTextFields(String[] newContent) {
        if (newContent == null) {
            return;
        }

        String[] displayedWords = new String[newContent.length];

        for (int i = 0; i < displayedWords.length; i++) {
            displayedWords[i] = "\"" + newContent[i].replace(" ", "⎵") + "\"";
        }

        for (int i = 0; i < numberOfRows; i++) {
            if (i < displayedWords.length) {
                textFields[i].setText(displayedWords[i]);
            } else {
                textFields[i].setText("");
            }
        }
    }

    public void updateText(String newText) {
        text.setText(newText);
        text.setCaretPosition(newText.length());
    }

    public void addDeleteButtonListeners(ActionListener[] listeners) {
        for (int i = 0; i < deleteButtons.length; i++) {
            deleteButtons[i].addActionListener(listeners[i]);
        }
    }

    public void setPauseButtonToPaused() {
        pauseButton.setIcon(playIcon);
    }

    public void setPauseButtonToPlay() {
        pauseButton.setIcon(pauseIcon);
    }

    public void setPlayIcon() {
        pauseButton.setIcon(playIcon);
    }

    public void addSettingsButtonListener(ActionListener listener) {
        settingsButton.addActionListener(listener);
    }

    public void addInfoButtonListener(ActionListener listener) {
        infoButton.addActionListener(listener);
    }

    public void addResetButtonListener(ActionListener listener) {
        resetButton.addActionListener(listener);
    }

    public void addPauseButtonListener(ActionListener listener) {
        pauseButton.addActionListener(listener);
    }

    public void updateButtonVisibility(boolean[] visibility) {
        for (int i = 0; i < deleteButtons.length; i++) {
            deleteButtons[i].setVisible(visibility[i]);
        }
    }
}
