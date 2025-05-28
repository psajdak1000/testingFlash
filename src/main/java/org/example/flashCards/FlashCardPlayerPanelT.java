package org.example.flashCards;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class FlashCardPlayerPanelT extends JPanel {

    // === STAŁE ===
    private static final Font MAIN_FONT = new Font("Helvetica Neue", Font.BOLD, 22);
    private static final String CARD_SEPARATOR = "/";
    private static final int DEFAULT_RATING = 0;

    // === KOMPONENTY GUI ===
    private JTextArea display;
    private JTextArea noteDisplay;
    private JLabel positionLabel;

    private JButton showAnswerButton;
    private JButton showPreviousButton;
    private JButton showHintButton;
    private JButton showNextButton;
    private JButton finishButton;

    private JPanel ratingPanel;
    private JButton rate1Button;
    private JButton rate2Button;
    private JButton rate3Button;
    private JButton rate4Button;

    // === DANE ===
    private ArrayList<FlashCard> cardList;
    private int currentIndex = 0;
    private FlashCard currentCard;
    private Map<FlashCard, Integer> ratings = new HashMap<>();

    // === STAN APLIKACJI ===
    private boolean isFinished = false;
    private boolean isShowingAnswer = false;

    public FlashCardPlayerPanelT() {
        initializeComponents();
        setupLayout();
        setupMenuBar();

        // Początkowy stan - wszystkie przyciski wyłączone do czasu wczytania fiszek
        setButtonsEnabled(false);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Display area
        display = new JTextArea(10, 20);
        display.setFont(MAIN_FONT);
        display.setWrapStyleWord(true);
        display.setLineWrap(true);
        display.setEditable(false);

        // Note display
        noteDisplay = new JTextArea(3, 20);
        noteDisplay.setFont(MAIN_FONT);
        noteDisplay.setWrapStyleWord(true);
        noteDisplay.setLineWrap(true);
        noteDisplay.setEditable(false);
        noteDisplay.setVisible(false);

        // Navigation buttons
        showAnswerButton = new JButton("Pokaż odpowiedź");
        showAnswerButton.addActionListener(e -> handleShowAnswer());

        showPreviousButton = new JButton("Poprzednia");
        showPreviousButton.addActionListener(e -> showPreviousCard());

        showNextButton = new JButton("Następna");
        showNextButton.addActionListener(e -> showNextCard());

        showHintButton = new JButton("Pokaż podpowiedź");
        showHintButton.addActionListener(e -> showHint());

        finishButton = new JButton("Zakończ naukę");
        finishButton.addActionListener(e -> endSession());

        // Rating buttons
        setupRatingPanel();
    }

    private void setupRatingPanel() {
        ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        rate1Button = new JButton("❌ Nie umiem");
        rate2Button = new JButton("😐 Średnio");
        rate3Button = new JButton("👍 Dobrze");
        rate4Button = new JButton("💯 Perfekcyjnie");

        rate1Button.addActionListener(e -> saveRating(1));
        rate2Button.addActionListener(e -> saveRating(2));
        rate3Button.addActionListener(e -> saveRating(3));
        rate4Button.addActionListener(e -> saveRating(4));

        ratingPanel.add(rate1Button);
        ratingPanel.add(rate2Button);
        ratingPanel.add(rate3Button);
        ratingPanel.add(rate4Button);
        ratingPanel.setVisible(false);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Question/Answer display
        JScrollPane qScrollPane = new JScrollPane(display);
        qScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        qScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Note display
        JScrollPane noteScrollPane = new JScrollPane(noteDisplay);
        noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        buttonPanel.add(showHintButton);
        buttonPanel.add(showAnswerButton);
        buttonPanel.add(showPreviousButton);
        buttonPanel.add(showNextButton);
        buttonPanel.add(finishButton);

        // Add components to main panel
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

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");

        // Load flashcards
        JMenuItem loadItem = new JMenuItem("Wczytaj fiszki");
        loadItem.addActionListener(e -> loadFlashCards());

        // Save/Load progress - POJEDYNCZE HANDLERY
        JMenuItem saveProgressItem = new JMenuItem("Zapisz postęp");
        saveProgressItem.addActionListener(e -> saveProgress());

        JMenuItem loadProgressItem = new JMenuItem("Wczytaj postęp");
        loadProgressItem.addActionListener(e -> loadProgress());

        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(saveProgressItem);
        fileMenu.add(loadProgressItem);

        menuBar.add(fileMenu);
        menuBar.add(Box.createHorizontalGlue());

        positionLabel = new JLabel("0/0");
        menuBar.add(positionLabel);

        add(menuBar, BorderLayout.NORTH);
    }

    // === METODY POMOCNICZE ===

    private void setButtonsEnabled(boolean enabled) {
        showAnswerButton.setEnabled(enabled);
        showPreviousButton.setEnabled(enabled);
        showNextButton.setEnabled(enabled);
        showHintButton.setEnabled(enabled);
        finishButton.setEnabled(enabled);
    }

    private void updateCardView() {
        if (cardList == null || cardList.isEmpty() || currentIndex < 0 || currentIndex >= cardList.size()) {
            return;
        }

        currentCard = cardList.get(currentIndex);
        display.setText(currentCard.getQuestion());

        // Reset stanu
        isShowingAnswer = false;
        showAnswerButton.setText("Pokaż odpowiedź");
        noteDisplay.setText("");
        noteDisplay.setVisible(false);
        ratingPanel.setVisible(false);

        updateNavigationButtons();
        updatePositionLabel();
    }

    private void updateNavigationButtons() {
        if (cardList == null || isFinished) {
            showPreviousButton.setEnabled(false);
            showNextButton.setEnabled(false);
            return;
        }

        showPreviousButton.setEnabled(currentIndex > 0);
        showNextButton.setEnabled(currentIndex < cardList.size() - 1);
    }

    private void updatePositionLabel() {
        if (cardList != null && !cardList.isEmpty()) {
            positionLabel.setText((currentIndex + 1) + "/" + cardList.size());
        } else {
            positionLabel.setText("0/0");
        }
    }

    // === LOGIKA NAWIGACJI ===

    private void showNextCard() {
        if (cardList == null || isFinished || currentIndex >= cardList.size() - 1) {
            return;
        }

        currentIndex++;
        updateCardView();
    }

    private void showPreviousCard() {
        if (cardList == null || isFinished || currentIndex <= 0) {
            return;
        }

        currentIndex--;
        updateCardView();
    }

    private void handleShowAnswer() {
        if (currentCard == null || isFinished) return;

        if (!isShowingAnswer) {
            // Pokaż odpowiedź
            display.setText(currentCard.getAnswer());
            showAnswerButton.setText("Pokaż pytanie");
            ratingPanel.setVisible(true);
            isShowingAnswer = true;
        } else {
            // Wróć do pytania
            display.setText(currentCard.getQuestion());
            showAnswerButton.setText("Pokaż odpowiedź");
            ratingPanel.setVisible(false);
            isShowingAnswer = false;
        }

        noteDisplay.setVisible(false);
    }

    private void showHint() {
        if (currentCard != null && !isFinished) {
            noteDisplay.setText(currentCard.getNote());
            noteDisplay.setVisible(true);
        }
    }

    private void saveRating(int value) {
        if (currentCard == null) return;

        ratings.put(currentCard, value);
        System.out.println("Oceniono: " + currentCard.getQuestion() + " → " + value);

        // Automatyczne przejście do następnej karty (jeśli istnieje)
        if (currentIndex < cardList.size() - 1) {
            showNextCard();
        } else {
            // Ostatnia karta - można zakończyć lub wrócić do pytania
            ratingPanel.setVisible(false);
            display.setText("To była ostatnia fiszka! Możesz zakończyć naukę.");
        }
    }

    private void endSession() {
        display.setText("DZIĘKI ZA NAUKĘ!");
        setButtonsEnabled(false);
        ratingPanel.setVisible(false);
        noteDisplay.setVisible(false);
        isFinished = true;
        currentCard = null;
    }

    // === WCZYTYWANIE I ZAPISYWANIE ===

    private void loadFlashCards() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            loadFile(fileChooser.getSelectedFile());
        }
    }

    private void saveProgress() {
        if (cardList == null || cardList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak fiszek do zapisania!", "Błąd", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            saveProgressToJson(fileChooser.getSelectedFile());
        }
    }

    private void loadProgress() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            loadProgressFromJson(fileChooser.getSelectedFile());
        }
    }

    private void loadFile(File file) {
        cardList = new ArrayList<>();
        ratings.clear(); // Wyczyść poprzednie oceny

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) { // Pomiń puste linie
                    makeCard(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pliku: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!cardList.isEmpty()) {
            currentIndex = 0;
            updateCardView();
            setButtonsEnabled(true);
            isFinished = false;
            JOptionPane.showMessageDialog(this, "Wczytano " + cardList.size() + " fiszek.");
        } else {
            JOptionPane.showMessageDialog(this, "Nie znaleziono fiszek w pliku!", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void makeCard(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, CARD_SEPARATOR);
        if (tokenizer.hasMoreTokens()) {
            String question = tokenizer.nextToken().trim();
            String answer = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : "";
            String note = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : "";

            if (!question.isEmpty()) { // Tylko jeśli pytanie nie jest puste
                cardList.add(new FlashCard(question, answer, note));
            }
        }
    }

    // === METODY JSON (POPRAWIONE) ===

    public void saveProgressToJson(File file) {
        if (cardList == null || cardList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak danych do zapisania!", "Błąd", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (FlashCard card : cardList) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("question", card.getQuestion());
            entry.put("answer", card.getAnswer());
            entry.put("note", card.getNote()); // Dodajemy również note
            entry.put("rating", ratings.getOrDefault(card, DEFAULT_RATING));
            data.add(entry);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            JOptionPane.showMessageDialog(this, "Zapisano postęp do pliku JSON.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd zapisu JSON: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadProgressFromJson(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> data = mapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});

            // Opcja 1: Wczytaj zarówno fiszki jak i oceny
            cardList = new ArrayList<>();
            ratings.clear();

            for (Map<String, Object> entry : data) {
                String question = (String) entry.get("question");
                String answer = (String) entry.get("answer");
                String note = (String) entry.getOrDefault("note", ""); // Bezpiecznie pobierz note

                // Bezpieczne pobieranie ratingu
                Integer ratingObj = (Integer) entry.get("rating");
                int rating = (ratingObj != null) ? ratingObj : DEFAULT_RATING;

                if (question != null && !question.isEmpty()) {
                    FlashCard card = new FlashCard(question, answer, note);
                    cardList.add(card);
                    ratings.put(card, rating);
                }
            }

            if (!cardList.isEmpty()) {
                currentIndex = 0;
                updateCardView();
                setButtonsEnabled(true);
                isFinished = false;
                JOptionPane.showMessageDialog(this, "Wczytano " + cardList.size() + " fiszek z postępem.");
            } else {
                JOptionPane.showMessageDialog(this, "Nie znaleziono danych w pliku JSON!", "Ostrzeżenie", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd wczytywania JSON: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }
}