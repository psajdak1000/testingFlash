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
    private ListIterator<FlashCard> cardListIterator;
    private FlashCard currentCard;
    private JLabel positionLabel;

    private boolean isShowAnswer;
    private int currentIndex = 0;

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
        showAnswerButton.setEnabled(true);
        showAnswerButton.addActionListener(e -> handleShowAnswer());

        //przycisk wczesniej
        showPreviousButton = new JButton("Pokaz wczesniejsza");
        showPreviousButton.setEnabled(true);
        showPreviousButton.addActionListener(e -> showPreviousCard());

        //nastepny
        showNextButton = new JButton("Pokaż następną");
        showNextButton.setEnabled(true);
        showNextButton.addActionListener(e -> showNextCard());

        //finishbutton
        finishButton = new JButton("Zakończ naukę");
        finishButton.addActionListener(e -> {
            endSession();
        });


        // Przycisk podpowiedzi
        showHintButton = new JButton("Pokaz podpowiedz");
        showHintButton.setEnabled(true); // 1) start — jeszcze nie wczytane fiszki
        showHintButton.addActionListener(e -> {
            if (currentCard != null && !isFinished) {
                noteDisplay.setText(currentCard.getNote());
                noteDisplay.setVisible(true);
            }
        });


        //panel z przyciskami

        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        buttonPanel.add(showHintButton);
        buttonPanel.add(showAnswerButton);
        buttonPanel.add(showPreviousButton);
        buttonPanel.add(showNextButton);
        buttonPanel.add(finishButton);
        buttonPanel.revalidate();
        buttonPanel.repaint();

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
        revalidate();
        repaint();


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
        menuBar.add(Box.createHorizontalGlue());
        positionLabel = new JLabel("0/0");
        menuBar.add(positionLabel);
        add(menuBar, BorderLayout.NORTH);
    }

    private void saveRating(int value) {
        if (currentCard == null) return;
        ratings.put(currentCard, value);
        System.out.println("Oceniono: " + currentCard.getQuestion() + " → " + value);

        if (cardListIterator.hasNext()) {
            showNextCard();
        }
    }

    // 1. W klasie FlashCardPlayerPanel :
    private void updateCardView(FlashCard card) {
        currentCard = card;
        display.setText(card.getQuestion());
        // reset stanu odpowiedzi i podpowiedzi
//        showAnswerButton.setText("Pokaż odpowiedź");
//        isShowAnswer = true;
        noteDisplay.setText("");
        noteDisplay.setVisible(false);
        ratingPanel.setVisible(false);
        updateNavButtons();                       // odśwież Prev/Next
        updatePositionLabel();                    // uaktualnij numerację
    }

    private void updatePositionLabel() {
        if (cardList != null) {
//  wiersz currentCard w liście  +1
            int humanIndex = cardList.indexOf(currentCard) + 1;
            int total = cardList.size();
            positionLabel.setText(humanIndex + "/" + total);
        }
    }

    // 2.  pomocnicza metodę do włączania/wyłączania nawigacji:
    private void updateNavButtons() {
//        // Jeśli sesja zakończona (currentCard == null), wszystkie nawigacyjne wyłączone
//        if (currentCard == null) {
//            showPreviousButton.setEnabled(false);
//            showNextButton.setEnabled(false);
//        } else {
//            showPreviousButton.setEnabled(cardListIterator.hasPrevious());
//            showNextButton.setEnabled(cardListIterator.hasNext());
//        }
        showPreviousButton.setEnabled(currentIndex > 0);
        showNextButton.setEnabled(currentIndex < cardList.size() - 1);
    }

    private void showNextCard() {
//        if (!cardListIterator.hasNext()) return;
//        currentIndex++;
//        updateCardView(cardListIterator.next());
//        updateNavButtons();
        if (currentIndex < cardList.size() - 1) {
            currentIndex++;
            updateCardView(cardList.get(currentIndex));
        }
    }

    private void showPreviousCard() {
//        if (!cardListIterator.hasPrevious()) return;
//        currentIndex--;
//        updateCardView(cardListIterator.previous());
//        updateNavButtons();

        if (currentIndex > 0) {
            currentIndex--;
            updateCardView(cardList.get(currentIndex));
        }
    }


    private void handleShowAnswer() {
//        if (cardList == null || cardList.isEmpty()) return;
//
//        if (isShowAnswer) {
//            display.setText(currentCard.getAnswer());
//            noteDisplay.setVisible(false);
//            ratingPanel.setVisible(true);
//            showAnswerButton.setText("Następna fiszka");
//            isShowAnswer = true;
//        } else {
//            if (cardListIterator.hasNext()) {
//                showNextCard();
//            } else if (cardListIterator.hasPrevious()) {
//                showPreviousCard();
//
//            }
//            else {
//                display.setText("To była ostatnia fiszka.");
//                showAnswerButton.setEnabled(false);
//                showHintButton.setEnabled(false);  // 3) wyłączamy też podpowiedź
//                ratingPanel.setVisible(false);
//                isFinished = true;
//                currentCard = null;                // czyścimy referencję dla bezpieczeństw
//            }
//        }

        if (currentCard == null) return;
        display.setText(currentCard.getAnswer());
        noteDisplay.setVisible(false);
        ratingPanel.setVisible(true);
    }

    private void endSession() {
        display.setText("DZIĘKI ZA NAUKĘ");
        // wyłącz wszystkie przyciski
        showAnswerButton.setEnabled(false);
        showHintButton.setEnabled(false);
        showPreviousButton.setEnabled(false);
        showNextButton.setEnabled(false);
        finishButton.setEnabled(false);
        // ukryj/pokaż co trzeba
        ratingPanel.setVisible(false);
        noteDisplay.setVisible(false);
        currentCard = null;
    }

//

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
            currentIndex = 0;
            //od razu pierwsza karta:
            updateCardView(cardList.get(currentIndex));
            showAnswerButton.setEnabled(true);
            showHintButton.setEnabled(true);
            finishButton.setEnabled(true);// 2) po wczytaniu – już można używać podpowiedzi
            updateNavButtons();// wlaczam/ wylaczam prev i next
            isFinished = false;
            updatePositionLabel();
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
