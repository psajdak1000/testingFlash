package org.example.flashCards;

// Importujemy potrzebne klasy z bibliotek Swing (GUI) i Java (IO, kolekcje)
import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * Panel odpowiedzialny za tworzenie fiszek (pytanie + odpowiedź).
 * Użytkownik może wpisać dane, dodać fiszkę do listy oraz zapisać je do pliku.
 */
public class FlashCardBuilderPanel extends JPanel {

    // Pole tekstowe na pytanie
    private JTextArea question;

    // Pole tekstowe na odpowiedź
    private JTextArea answer;
    private JTextArea note;

    // Lista przechowująca utworzone fiszki
    private ArrayList<FlashCard> flashCardList;

    /**
     * Konstruktor panelu. Buduje UI i logikę dodawania oraz zapisywania fiszek.
     */
    public FlashCardBuilderPanel() {
        // Inicjalizujemy pustą listę fiszek
        flashCardList = new ArrayList<>();

        // Ustawiamy układ panelu na BorderLayout (dzieli przestrzeń na NORTH, CENTER itd.)
        setLayout(new BorderLayout());

        // Główny wewnętrzny panel do trzymania komponentów
        JPanel mainPanel = new JPanel();

        // Ustawienie czcionki
        Font greatFont = new Font("Helvetica Neue", Font.BOLD, 21);

        // Tworzenie pola tekstowego na pytanie
        question = new JTextArea(6, 20);
        question.setLineWrap(true); // zawijanie linii
        question.setFont(greatFont);

        // Dodanie scrolla do pytania (tylko pionowy)
        JScrollPane qScrollPane = new JScrollPane(question);
        qScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants
                .VERTICAL_SCROLLBAR_ALWAYS);
        qScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants
                .HORIZONTAL_SCROLLBAR_NEVER);

        // Tworzenie pola tekstowego na odpowiedź
        answer = new JTextArea(6, 20);
        answer.setLineWrap(true);
        answer.setWrapStyleWord(true); // zawijanie słów
        answer.setFont(greatFont);


        // Scroll do pola odpowiedzi
        JScrollPane aScrollPane = new JScrollPane(answer);
        aScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        aScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);




        // Tworzenie pola tekstowego na notatke
        note = new JTextArea(4, 20);
        note.setLineWrap(true);
        note.setWrapStyleWord(true); // zawijanie słów
        note.setFont(greatFont);
        //scorll do notatki
        JScrollPane nScrollPane = new JScrollPane(note);
        nScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        nScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Przycisk dodający fiszkę do listy
        JButton nextButton = new JButton("Dodaj fiszkę");
        nextButton.addActionListener(e -> {

            // Tworzenie nowej fiszki na podstawie pól tekstowych
            FlashCard card = new FlashCard(question.getText(), answer.getText(), note.getText());
            flashCardList.add(card); // dodajemy do listy
            clearFields(); // czyścimy pola
        });

        //domyślny FlowLayout, który układa komponenty poziomo, jak tekst zamieniam na
        //BoxLayout.Y_AXIS, wszystkie komponenty zostaną rozmieszczone pionowo
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Dodajemy komponenty do wewnętrznego panelu
        //pytanie
        mainPanel.add(new JLabel("Pytanie"));
        mainPanel.add(qScrollPane);
        //odpowiedz
        mainPanel.add(new JLabel("Odpowiedź"));
        mainPanel.add(aScrollPane);

        //nota
        mainPanel.add(new JLabel("Notatka"));
        mainPanel.add(nScrollPane);

        mainPanel.add(nextButton);

        // Umieszczamy główny panel wewnątrz (CENTER) panelu FlashCardBuilderPanel
        add(mainPanel, BorderLayout.CENTER);

        // ====== MENU GÓRNE ======

        // Pasek menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Plik");
        JMenuItem saveItem = new JMenuItem("Zapisz fiszki");

        // Obsługa kliknięcia "Zapisz fiszki"
        saveItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(); // wybór pliku
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                saveToFile(fileChooser.getSelectedFile()); // zapis do pliku
            }
        });

        // Składamy menu
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);
        add(menuBar, BorderLayout.NORTH); // menu na górze panelu
    }

    /**
     * Zapisuje wszystkie fiszki z listy do wybranego pliku tekstowego.
     * Każda fiszka zapisana jako: pytanie/odpowiedź
     */
    private void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (FlashCard card : flashCardList) {
                writer.write(card.getQuestion() + "/");
                writer.write(card.getAnswer() + "/");
                writer.write(card.getNote() + "\n");

            }
            JOptionPane.showMessageDialog(this, "Zapisano fiszki.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Błąd przy zapisie.", "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Czyści pola tekstowe i ustawia kursor z powrotem w polu pytania.
     */
    private void clearFields() {
        question.setText("");
        answer.setText("");
        note.setText("");
        question.requestFocus(); // ustawia fokus z powrotem na pytaniu
    }
}
