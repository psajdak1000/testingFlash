package org.ui.view;

import org.ui.controller.BuilderController;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class BuilderPanel extends JPanel {
    private final JTextArea questionArea;
    private final JTextArea answerArea;
    private final JTextArea noteArea;
    private final BuilderController controller;

    public BuilderPanel() {
        this.controller = new BuilderController();
        setLayout(new BorderLayout());

        // Pasek menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem saveItem = new JMenuItem("Zapisz fiszki");
        saveItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    controller.saveCards(chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Zapisano fiszki.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Błąd przy zapisie.", "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);
        add(menuBar, BorderLayout.NORTH);

        // Główny panel z polami
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        Font font = new Font("Helvetica Neue", Font.BOLD, 21);

        // Pytanie
        mainPanel.add(new JLabel("Pytanie"));
        questionArea = new JTextArea(6, 20);
        questionArea.setLineWrap(true);
        questionArea.setFont(font);
        JScrollPane qScroll = new JScrollPane(questionArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(qScroll);

        // Odpowiedź
        mainPanel.add(new JLabel("Odpowiedź"));
        answerArea = new JTextArea(6, 20);
        answerArea.setLineWrap(true);
        answerArea.setFont(font);
        JScrollPane aScroll = new JScrollPane(answerArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(aScroll);

        // Notatka
        mainPanel.add(new JLabel("Notatka"));
        noteArea = new JTextArea(4, 20);
        noteArea.setLineWrap(true);
        noteArea.setFont(font);
        JScrollPane nScroll = new JScrollPane(noteArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(nScroll);

        // Przycisk dodający fiszkę
        JButton addButton = new JButton("Dodaj fiszkę");
        addButton.addActionListener(e -> {
            controller.addCard(
                    questionArea.getText(),
                    answerArea.getText(),
                    noteArea.getText()
            );
            clearFields();
        });
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(addButton);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void clearFields() {
        questionArea.setText("");
        answerArea.setText("");
        noteArea.setText("");
        questionArea.requestFocus();
    }
}

