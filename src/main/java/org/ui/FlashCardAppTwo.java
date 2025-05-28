package org.ui;

import org.ui.view.BuilderPanel;
import org.ui.view.PlayerPanel;

import javax.swing.*;

public class FlashCardAppTwo {

    public static void main(String[] args) {
        // Uruchamiamy UI w wątku EDT
        SwingUtilities.invokeLater(FlashCardAppTwo::createAndShowGUI);
    }

    /**
     * Buduje i wyświetla główne okno aplikacji.
     */
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("FlashCard App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);

        // Główne zakładki aplikacji
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tworzenie fiszek", new BuilderPanel());
        tabbedPane.addTab("Odtwarzanie fiszek", new PlayerPanel());

        frame.getContentPane().add(tabbedPane);
        frame.setLocationRelativeTo(null); // wyśrodkuj okno na ekranie
        frame.setVisible(true);
    }
}

