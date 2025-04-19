package org.flashCards;

import javax.swing.*;

public class FlashCardAppTwo {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlashCardAppTwo().createAndShowGUI());
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("FlashCard App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);

        JTabbedPane tabbedPane = new JTabbedPane();

        FlashCardBuilderPanel builderPanel = new FlashCardBuilderPanel();
        FlashCardPlayerPanel playerPanel = new FlashCardPlayerPanel();

        tabbedPane.addTab("Tworzenie fiszek", builderPanel);
        tabbedPane.addTab("Odtwarzanie fiszek", playerPanel);

        frame.getContentPane().add(tabbedPane);
        frame.setLocationRelativeTo(null); // środek ekranu
        frame.setVisible(true);
    }
}
