package org.example.flashCards;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class FlashCardPlayerPanel extends JPanel {

    private JTextArea display;
    private JTextArea noteDisplay;
    private ArrayList<FlashCard> cardList;
    private Iterator<FlashCard> cardIterator;
    private FlashCard currentCard;
    private boolean isShowAnswer;
    private JButton showAnswerButton;
    private JButton showHintButton;

    private JPanel ratingPanel;
    private JButton rate1Button;
    private JButton rate2Button;
    private JButton rate3Button;
    private JButton rate4Button;

    private boolean isFinished = false;



    private Map<FlashCard, Integer> ratings = new HashMap<>();


    public FlashCardPlayerPanel() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        Font mFont = new Font("Helvetica Neue", Font.BOLD, 22);

        //pole z pytaniem/odpowiedzia
        display = new JTextArea(10, 20);
        display.setFont(mFont);
        display.setWrapStyleWord(true);
        display.setLineWrap(true);
        display.setEditable(false);

        JScrollPane qScrollPane = new JScrollPane(display);
        qScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // pole z notatka ukryte na starcie
        noteDisplay = new JTextArea(3, 20);
        noteDisplay.setFont(mFont);
        noteDisplay.setWrapStyleWord(true);
        noteDisplay.setLineWrap(true);
        noteDisplay.setEditable(false);
        noteDisplay.setVisible(false);

        JScrollPane noteScrollPane = new JScrollPane(noteDisplay);
        noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


        // tutaj przycisk odpowiedzi tu skonczylem

        showAnswerButton = new JButton("Pokaz odpowiedz");
        showAnswerButton.setEnabled(false);
        showAnswerButton.addActionListener(e -> handleShowAnswer());


        // Przycisk podpowiedzi
        showHintButton = new JButton("Pokaz podpowiedz");
        showHintButton.setEnabled(false); // 1) start — jeszcze nie wczytane fiszki
        showHintButton.addActionListener(e -> {
            if (currentCard != null  && !isFinished) {
                noteDisplay.setText(currentCard.getNote());
                noteDisplay.setVisible(true);
            }
        });
        //panel z przyciskami

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(showHintButton);
        buttonPanel.add(showAnswerButton);

        // Panel oceniania
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



        // === Dodawanie komponentów do mainPanel w dobrej kolejności ===
        // Dodawanie do mainPanel
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


        // Menu z wczytywaniem fiszek
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem loadItem = new JMenuItem("Wczytaj zestaw fiszek");

        loadItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                loadFile(fileChooser.getSelectedFile());
            }
        });

        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        add(menuBar, BorderLayout.NORTH);
    }

    private void saveRating(int value) {
        if (currentCard != null) {
            ratings.put(currentCard, value);
            System.out.println("Oceniono fiszkę: " + currentCard.getQuestion() + " → " + value);
            // Możesz tu dodać przejście do następnej fiszki od razu, jeśli chcesz:
            if (cardIterator.hasNext()) {
                showNextCard();
            } else {
                display.setText("To była ostatnia fiszka.");
                showAnswerButton.setEnabled(false);
                ratingPanel.setVisible(false);
            }
        }
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
            if (cardIterator.hasNext()) {
                showNextCard();
            } else {
                display.setText("To była ostatnia fiszka.");
                showAnswerButton.setEnabled(false);
                showHintButton.setEnabled(false);  // 3) wyłączamy też podpowiedź
                ratingPanel.setVisible(false);
                isFinished = true;
                currentCard = null;                // czyścimy referencję dla bezpieczeństw
            }
        }
    }

    private void showNextCard() {
        currentCard = cardIterator.next();
        display.setText(currentCard.getQuestion());
        showAnswerButton.setText("Pokaż odpowiedź");
        // 2b) każda nowa fiszka przywraca aktywność przycisku
        showHintButton.setEnabled(true);

        isShowAnswer = true;
        //to dodane
        noteDisplay.setText("");
        noteDisplay.setVisible(false);
        ratingPanel.setVisible(false);

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
            cardIterator = cardList.iterator();
            showNextCard();
            showAnswerButton.setEnabled(true);
            showHintButton.setEnabled(true);  // 2) po wczytaniu – już można używać podpowiedzi
            isFinished = false;
        }
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
