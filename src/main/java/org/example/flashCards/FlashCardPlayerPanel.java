package org.example.flashCards;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class FlashCardPlayerPanel extends JPanel {

    // === STAŁE ===
    private static final Font MAIN_FONT = new Font("Helvetica Neue", Font.BOLD, 22);
    private static final Font STATS_FONT = new Font("Helvetica Neue", Font.PLAIN, 14);
    private static final String CARD_SEPARATOR = "/";

    // === KOMPONENTY GUI ===
    private JTextArea display;
    private JTextArea noteDisplay;
    private JLabel positionLabel;
    private JLabel statsLabel;

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

    // Panel filtrów
    private JPanel filterPanel;
    private JButton showAllButton;
    private JButton showToReviewButton;
    private JButton showMasteredButton;
    private JButton showNewButton;

    // === DANE ===
    private ArrayList<FlashCard> allCards; // Wszystkie fiszki
    private ArrayList<FlashCard> filteredCards; // Aktualnie wyświetlane fiszki
    private int currentIndex = 0;
    private FlashCard currentCard;
    private FilterMode currentFilter = FilterMode.ALL;

    // === STAN APLIKACJI ===
    private boolean isFinished = false;
    private boolean isShowingAnswer = false;

    // === ENUM DLA FILTRÓW ===
    public enum FilterMode {
        ALL("Wszystkie"),
        TO_REVIEW("Do powtórki"),
        MASTERED("Opanowane"),
        NEW("Nowe");

        private final String displayName;

        FilterMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public FlashCardPlayerPanel() {
        initializeComponents();
        setupLayout();
        setupMenuBar();

        // Początkowy stan - wszystkie przyciski wyłączone do czasu wczytania fiszek
        setButtonsEnabled(false);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Display area
        display = new JTextArea(8, 20);
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

        // Statistics label
        statsLabel = new JLabel("Statystyki: Wczytaj fiszki aby zobaczyć dane");
        statsLabel.setFont(STATS_FONT);
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);

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

        // Filter buttons
        setupFilterPanel();
    }

    private void setupRatingPanel() {
        ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        ratingPanel.setBorder(BorderFactory.createTitledBorder("Jak Ci poszło?"));

        rate1Button = new JButton("❌ Nie umiem");
        rate2Button = new JButton("😐 Średnio");
        rate3Button = new JButton("👍 Dobrze");
        rate4Button = new JButton("💯 Umiem i jestem pewien");

        rate1Button.addActionListener(e -> rateCard(1));
        rate2Button.addActionListener(e -> rateCard(2));
        rate3Button.addActionListener(e -> rateCard(3));
        rate4Button.addActionListener(e -> rateCard(4));

        ratingPanel.add(rate1Button);
        ratingPanel.add(rate2Button);
        ratingPanel.add(rate3Button);
        ratingPanel.add(rate4Button);
        ratingPanel.setVisible(false);
    }

    private void setupFilterPanel() {
        filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtruj fiszki"));

        showAllButton = new JButton("Wszystkie");
        showToReviewButton = new JButton("Do powtórki");
        showMasteredButton = new JButton("Opanowane");
        showNewButton = new JButton("Nowe");

        showAllButton.addActionListener(e -> applyFilter(FilterMode.ALL));
        showToReviewButton.addActionListener(e -> applyFilter(FilterMode.TO_REVIEW));
        showMasteredButton.addActionListener(e -> applyFilter(FilterMode.MASTERED));
        showNewButton.addActionListener(e -> applyFilter(FilterMode.NEW));

        filterPanel.add(showAllButton);
        filterPanel.add(showToReviewButton);
        filterPanel.add(showMasteredButton);
        filterPanel.add(showNewButton);

        updateFilterButtonStates();
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Statistics
        mainPanel.add(statsLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        // Filter panel
        mainPanel.add(filterPanel);
        mainPanel.add(Box.createVerticalStrut(10));

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
        mainPanel.add(ratingPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");

        // Load flashcards
        JMenuItem loadItem = new JMenuItem("Wczytaj fiszki");
        loadItem.addActionListener(e -> loadFlashCards());

        // Save/Load progress
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

    // === METODY FILTROWANIA ===

    private void applyFilter(FilterMode mode) {
        if (allCards == null || allCards.isEmpty()) {
            return;
        }

        currentFilter = mode;
        filteredCards = new ArrayList<>();

        switch (mode) {
            case ALL:
                filteredCards.addAll(allCards);
                break;
            case TO_REVIEW:
                filteredCards.addAll(allCards.stream()
                        .filter(FlashCard::isToReview)
                        .collect(Collectors.toList()));
                break;
            case MASTERED:
                filteredCards.addAll(allCards.stream()
                        .filter(FlashCard::isMastered)
                        .collect(Collectors.toList()));
                break;
            case NEW:
                filteredCards.addAll(allCards.stream()
                        .filter(FlashCard::isNew)
                        .collect(Collectors.toList()));
                break;
        }

        currentIndex = 0;
        updateFilterButtonStates();
        updateCardView();
        updateStatistics();

        if (filteredCards.isEmpty()) {
            display.setText("Brak fiszek w tej kategorii: " + mode.getDisplayName());
            setButtonsEnabled(false);
        } else {
            setButtonsEnabled(true);
        }
    }

    private void updateFilterButtonStates() {
        showAllButton.setEnabled(currentFilter != FilterMode.ALL);
        showToReviewButton.setEnabled(currentFilter != FilterMode.TO_REVIEW);
        showMasteredButton.setEnabled(currentFilter != FilterMode.MASTERED);
        showNewButton.setEnabled(currentFilter != FilterMode.NEW);
    }

    // === STATYSTYKI ===

    private void updateStatistics() {
        if (allCards == null || allCards.isEmpty()) {
            statsLabel.setText("Statystyki: Wczytaj fiszki aby zobaczyć dane");
            return;
        }

        long newCount = allCards.stream().filter(FlashCard::isNew).count();
        long toReviewCount = allCards.stream().filter(FlashCard::isToReview).count();
        long masteredCount = allCards.stream().filter(FlashCard::isMastered).count();
        int totalCount = allCards.size();

        String stats = String.format(
                "📊 Razem: %d | 🆕 Nowe: %d | 🔄 Do powtórki: %d | ✅ Opanowane: %d | Filtr: %s (%d)",
                totalCount, newCount, toReviewCount, masteredCount,
                currentFilter.getDisplayName(), filteredCards.size()
        );

        statsLabel.setText(stats);
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
        if (filteredCards == null || filteredCards.isEmpty() || currentIndex < 0 || currentIndex >= filteredCards.size()) {
            return;
        }

        currentCard = filteredCards.get(currentIndex);
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
        if (filteredCards == null || isFinished) {
            showPreviousButton.setEnabled(false);
            showNextButton.setEnabled(false);
            return;
        }

        showPreviousButton.setEnabled(currentIndex > 0);
        showNextButton.setEnabled(currentIndex < filteredCards.size() - 1);
    }

    private void updatePositionLabel() {
        if (filteredCards != null && !filteredCards.isEmpty()) {
            positionLabel.setText((currentIndex + 1) + "/" + filteredCards.size());
        } else {
            positionLabel.setText("0/0");
        }
    }

    // === LOGIKA NAWIGACJI ===

    private void showNextCard() {
        if (filteredCards == null || isFinished || currentIndex >= filteredCards.size() - 1) {
            return;
        }

        currentIndex++;
        updateCardView();
    }

    private void showPreviousCard() {
        if (filteredCards == null || isFinished || currentIndex <= 0) {
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

    private void rateCard(int rating) {
        if (currentCard == null) return;

        currentCard.setRating(rating);
        System.out.println("Oceniono: " + currentCard.getQuestion() + " → " + rating +
                " (Status: " + currentCard.getStatus() + ")");

        updateStatistics();

        // Jeśli aktualny filtr to "Do powtórki" i fiszka została opanowana,
        // usuń ją z filteredCards
        if (currentFilter == FilterMode.TO_REVIEW && currentCard.isMastered()) {
            filteredCards.remove(currentIndex);
            if (currentIndex >= filteredCards.size()) {
                currentIndex = Math.max(0, filteredCards.size() - 1);
            }
            if (filteredCards.isEmpty()) {
                display.setText("Gratulacje! Opanowałeś wszystkie fiszki do powtórki! 🎉");
                setButtonsEnabled(false);
                ratingPanel.setVisible(false);
                return;
            }
            updateCardView();
        } else {
            // Automatyczne przejście do następnej karty (jeśli istnieje)
            if (currentIndex < filteredCards.size() - 1) {
                showNextCard();
            } else {
                // Ostatnia karta
                ratingPanel.setVisible(false);
                display.setText("To była ostatnia fiszka w tej kategorii! Możesz zakończyć naukę.");
            }
        }
    }

    private void endSession() {
        display.setText("DZIĘKI ZA NAUKĘ! 📚✨");
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
        if (allCards == null || allCards.isEmpty()) {
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
        allCards = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    makeCard(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd podczas wczytywania pliku: " + e.getMessage(),
                    "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!allCards.isEmpty()) {
            applyFilter(FilterMode.ALL);
            isFinished = false;
            JOptionPane.showMessageDialog(this, "Wczytano " + allCards.size() + " fiszek.");
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

            if (!question.isEmpty()) {
                allCards.add(new FlashCard(question, answer, note));
            }
        }
    }

    // === METODY JSON ===

    public void saveProgressToJson(File file) {
        if (allCards == null || allCards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak danych do zapisania!", "Błąd", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (FlashCard card : allCards) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("question", card.getQuestion());
            entry.put("answer", card.getAnswer());
            entry.put("note", card.getNote());
            entry.put("rating", card.getRating());
            entry.put("status", card.getStatus().name());
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

            allCards = new ArrayList<>();

            for (Map<String, Object> entry : data) {
                String question = (String) entry.get("question");
                String answer = (String) entry.get("answer");
                String note = (String) entry.getOrDefault("note", "");

                Integer ratingObj = (Integer) entry.get("rating");
                int rating = (ratingObj != null) ? ratingObj : 0;

                String statusStr = (String) entry.get("status");
                CardStatus status = CardStatus.NEW;
                if (statusStr != null) {
                    try {
                        status = CardStatus.valueOf(statusStr);
                    } catch (IllegalArgumentException e) {
                        // Zostaw domyślny status NEW
                    }
                }

                if (question != null && !question.isEmpty()) {
                    FlashCard card = new FlashCard(question, answer, note, status, rating);
                    allCards.add(card);
                }
            }

            if (!allCards.isEmpty()) {
                applyFilter(FilterMode.ALL);
                isFinished = false;
                JOptionPane.showMessageDialog(this, "Wczytano " + allCards.size() + " fiszek z postępem.");
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