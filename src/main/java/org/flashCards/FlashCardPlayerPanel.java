package flashCards;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class FlashCardPlayerPanel extends JPanel {

    private JTextArea display;
    private ArrayList<FlashCard> cardList;
    private Iterator<FlashCard> cardIterator;
    private FlashCard currentCard;
    private boolean isShowAnswer;
    private JButton showAnswerButton;

    public FlashCardPlayerPanel() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();

        Font mFont = new Font("Helvetica Neue", Font.BOLD, 22);

        display = new JTextArea(10, 20);
        display.setFont(mFont);
        display.setWrapStyleWord(true);
        display.setLineWrap(true);
        display.setEditable(false);

        JScrollPane qScrollPane = new JScrollPane(display);
        qScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        showAnswerButton = new JButton("Pokaż odpowiedź");
        showAnswerButton.addActionListener(e -> handleShowAnswer());

        mainPanel.add(qScrollPane);
        mainPanel.add(showAnswerButton);

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

    private void handleShowAnswer() {
        if (cardList == null || cardList.isEmpty()) return;

        if (isShowAnswer) {
            display.setText(currentCard.getAnswer());
            showAnswerButton.setText("Następna fiszka");
            isShowAnswer = false;
        } else {
            if (cardIterator.hasNext()) {
                showNextCard();
            } else {
                display.setText("To była ostatnia fiszka.");
                showAnswerButton.setEnabled(false);
            }
        }
    }

    private void showNextCard() {
        currentCard = cardIterator.next();
        display.setText(currentCard.getQuestion());
        showAnswerButton.setText("Pokaż odpowiedź");
        isShowAnswer = true;
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
        }
    }

    private void makeCard(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, "/");
        if (tokenizer.hasMoreTokens()) {
            String question = tokenizer.nextToken().trim();
            String answer = tokenizer.hasMoreTokens() ? tokenizer.nextToken().trim() : "";
            cardList.add(new FlashCard(question, answer));
        }
    }
}
