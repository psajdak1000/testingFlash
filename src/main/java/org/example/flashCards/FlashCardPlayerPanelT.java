package org.example.flashCards;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class FlashCardPlayerPanelT extends JPanel {

    private JTextArea display;
    private JTextArea noteDisplay;
    private ArrayList<FlashCard> cardList;
    private ListIterator<FlashCard> cardListIterator;
    private FlashCard currentCard;
    private boolean isShowAnswer;
    private JButton showAnswerButton;
    private JButton showPreviousButton;
    private JButton showHintButton;

    private JPanel ratingPanel;
    private JButton rate1Button;
    private JButton rate2Button;
    private JButton rate3Button;
    private JButton rate4Button;

    private boolean isFinished = false;

    private Map<FlashCard, Integer> ratings = new HashMap<>();

    public FlashCardPlayerPanelT() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Font mFont = new Font("Helvetica Neue", Font.BOLD, 22);

        // pole z pytaniem/odpowiedzią
        display = new JTextArea(10, 20);
        display.setFont(mFont);
        display.setWrapStyleWord(true);
        display.setLineWrap(true);
        display.setEditable(false);

        JScrollPane qScrollPane = new JScrollPane(display);
        qScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // pole z notatką ukryte na starcie
        noteDisplay = new JTextArea(3, 20);
        noteDisplay.setFont(mFont);
        noteDisplay.setWrapStyleWord(true);
        noteDisplay.setLineWrap(true);
        noteDisplay.setEditable(false);
        noteDisplay.setVisible(false);

        JScrollPane noteScrollPane = new JScrollPane(noteDisplay);
        noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        showHintButton = new JButton("Pokaż podpowiedź");
        showHintButton.setEnabled(false);
        showHintButton.addActionListener(e -> {
            if (!isShowAnswer && !isFinished) {
                noteDisplay.setText(currentCard.getNote());
                noteDisplay.setVisible(true);
            }
        });

        // panel z przyciskami
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        showAnswerButton = new JButton("Pokaż odpowiedź");
        showAnswerButton.setEnabled(false);
        showAnswerButton.addActionListener(e -> handleShowAnswer());

        showPreviousButton = new JButton("Poprzednia");
        showPreviousButton.setEnabled(false);
        showPreviousButton.addActionListener(e -> showPreviousCard());

        buttonPanel.add(showHintButton);
        buttonPanel.add(showAnswerButton);
        buttonPanel.add(showPreviousButton);

        // panel oceniania
        ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rate1Button = new JButton("❌ Nie umiem");
        rate2Button = new JButton("😐 Średnio");
        rate3Button = new JButton("👍 Dobrze");
        rate4Button = new JButton("💯 Umiem");
        ratingPanel.add(rate1Button);
        ratingPanel.add(rate2Button);
        ratingPanel.add(rate3Button);
        ratingPanel.add(rate4Button);
        ratingPanel.setVisible(false);

        rate1Button.addActionListener(e -> saveRating(1));
        rate2Button.addActionListener(e -> saveRating(2));
        rate3Button.addActionListener(e -> saveRating(3));
        rate4Button.addActionListener(e -> saveRating(4));

        // dodawanie komponentów do mainPanel
        mainPanel.add(qScrollPane);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JLabel("Podpowiedź:"));
        mainPanel.add(noteScrollPane);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JLabel("Jak Ci poszło?"));
        mainPanel.add(ratingPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void handleShowAnswer() {
        if (cardList == null || cardList.isEmpty()) return;

        if (isShowAnswer) {
            display.setText(currentCard.getAnswer());
            noteDisplay.setVisible(false);
            ratingPanel.setVisible(true);
            showAnswerButton.setText("Następna fiszka");
            isShowAnswer = false;
        } else {
            if (cardListIterator.hasNext()) {
                showNextCard();
                updatePrevButton();
            } else if (cardListIterator.hasPrevious()) {
                showPreviousCard();
            }
        }
    }

    private void showPreviousCard() {
        if (cardListIterator == null) return;

        // Cofamy się o jeden "pusty" krok, aby wskazać bieżącą kartę
        if (cardListIterator.hasPrevious()) {
            cardListIterator.previous();
        }

        // Teraz pobieramy faktycznie poprzednią kartę (jeśli istnieje)
        if (cardListIterator.hasPrevious()) {
            currentCard = cardListIterator.previous();
            display.setText(currentCard.getQuestion());
            showAnswerButton.setText("Pokaż odpowiedź");
            showHintButton.setEnabled(true);

            isShowAnswer = true;
            noteDisplay.setText("");
            noteDisplay.setVisible(false);
            ratingPanel.setVisible(false);

            // Po pokazaniu poprzedniej karty aktualizujemy przycisk "Poprzednia"
            updatePrevButton();
        }
    }

    private void showNextCard() {
        currentCard = cardListIterator.next();
        display.setText(currentCard.getQuestion());
        showAnswerButton.setText("Pokaż odpowiedź");
        showHintButton.setEnabled(true);

        isShowAnswer = true;
        noteDisplay.setText("");
        noteDisplay.setVisible(false);
        ratingPanel.setVisible(false);

        updatePrevButton();
    }

    private void saveRating(int rating) {
        ratings.put(currentCard, rating);
        if (cardListIterator.hasNext()) {
            showNextCard();
        } else {
            display.setText("To była ostatnia fiszka.");
            showAnswerButton.setEnabled(false);
            ratingPanel.setVisible(false);
            isFinished = true;
        }
    }

    private void loadFile(File file) {
        cardList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                makeCard(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pliku", "Błąd", JOptionPane.ERROR_MESSAGE);
        }

        if (!cardList.isEmpty()) {
            cardListIterator = cardList.listIterator();
            showNextCard();
            updatePrevButton();
            showAnswerButton.setEnabled(true);
            showHintButton.setEnabled(true);
            isFinished = false;
        }
    }

    /**
     * Włącza lub wyłącza przycisk "Poprzednia" w zależności od pozycji iteratora.
     */
    private void updatePrevButton() {
        if (showPreviousButton == null) return;
        boolean enable = cardListIterator != null && cardListIterator.previousIndex() > 0;
        showPreviousButton.setEnabled(enable);
    }

    private void makeCard(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, "/");
        if (tokenizer.hasMoreTokens()) {
            String question = tokenizer.nextToken().trim();
            String answer = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : "";
            String note = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : "";
            cardList.add(new FlashCard(question, answer, note));
        }
    }
}

