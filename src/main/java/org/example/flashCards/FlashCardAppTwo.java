package org.example.flashCards;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class FlashCardAppTwo {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Nie udało się załadować FlatLaf Dark.");
        }

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
