package org.ui.view;

import org.model.FlashCard;
import org.ui.controller.PlayerController;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel odtwarzania fiszek.
 * <ul>
 *   <li>„Podpowiedź” – pokazuje notatkę fiszki</li>
 *   <li>„Pokaż odpowiedź / Następna fiszka” – główny przycisk sterujący</li>
 *   <li>Oceny 1-4 – trafiają do ProgressTracker poprzez PlayerController</li>
 * </ul>
 */
public class PlayerPanel extends JPanel {

    /* ==== zależności + stan ==== */
    private final PlayerController controller = new PlayerController();
    private FlashCard current;                        // aktualna fiszka
    private boolean waitingForAnswer = true;          // true ⇒ pokaż odp.; false ⇒ następna
    private final Map<FlashCard, Integer> ratings = new HashMap<>();

    /* ==== UI komponenty ==== */
    private JTextArea qaArea, noteArea;
    private JButton btnShow, btnHint;
    private JPanel ratingPanel;

    public PlayerPanel() {
        setLayout(new BorderLayout());
        add(buildMenuBar(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
    }

    /* ------------------------------------------------------------------ */
    /* -------------------------  BUDOWA UI  ----------------------------- */
    /* ------------------------------------------------------------------ */

    /** Pasek menu – tylko „Plik → Wczytaj zestaw” */
    private JMenuBar buildMenuBar() {
        JMenuItem load = new JMenuItem("Wczytaj zestaw");
        load.addActionListener(e -> chooseAndLoadFile());

        JMenu menu = new JMenu("Plik");
        menu.add(load);

        JMenuBar bar = new JMenuBar();
        bar.add(menu);
        return bar;
    }

    /** Panel środkowy (pytanie/odpowiedź, przyciski, podpowiedź, ocena) */
    private JPanel buildCenterPanel() {
        qaArea   = makeTextArea(10);
        noteArea = makeTextArea(3);
        noteArea.setVisible(false);

        /* ----- przyciski ----- */
        btnHint = new JButton("Podpowiedź");
        btnHint.setEnabled(false);
        btnHint.addActionListener(e -> showHint());

        btnShow = new JButton("Pokaż odpowiedź");
        btnShow.setEnabled(false);
        btnShow.addActionListener(e -> handleMainButton());

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(btnHint);
        buttons.add(btnShow);

        /* ----- panel oceniania ----- */
        ratingPanel = buildRatingPanel();

        /* ----- układ pionowy ----- */
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        box.add(scroll(qaArea));
        box.add(Box.createVerticalStrut(10));
        box.add(buttons);
        box.add(Box.createVerticalStrut(10));
        box.add(new JLabel("Podpowiedź:"));
        box.add(scroll(noteArea));
        box.add(Box.createVerticalStrut(10));
        box.add(new JLabel("Jak Ci poszło?"));
        box.add(ratingPanel);

        return box;
    }

    /** Fabryka pól tekstowych o jednolitym stylu */
    private JTextArea makeTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 20);
        ta.setFont(new Font("Helvetica Neue", Font.BOLD, 22));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(false);
        return ta;
    }

    private JScrollPane scroll(JComponent c) {                 // wygodny skrót
        return new JScrollPane(c,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /** Panel z przyciskami oceny 1-4 */
    private JPanel buildRatingPanel() {
        JPanel p = new JPanel(new FlowLayout());
        addRateBtn(p, "❌ Nie umiem", 1);
        addRateBtn(p, "😐 Średnio",   2);
        addRateBtn(p, "👍 Dobrze",    3);
        addRateBtn(p, "💯 Umiem",     4);
        p.setVisible(false);
        return p;
    }

    private void addRateBtn(JPanel p, String txt, int val) {
        JButton b = new JButton(txt);
        b.addActionListener(e -> rate(val));
        p.add(b);
    }

    /* ------------------------------------------------------------------ */
    /* ----------------------------  LOGIKA  ----------------------------- */
    /* ------------------------------------------------------------------ */

    /** Dialog wyboru pliku + załadowanie przez PlayerController */
    private void chooseAndLoadFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            controller.loadCards(file);
            showNext();                       // start nauki od 1. fiszki
            btnShow.setEnabled(true);
            btnHint.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Błąd podczas wczytywania pliku",
                    "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Pokazuje podpowiedź (note) bieżącej fiszki */
    private void showHint() {
        if (current == null) return;
        noteArea.setText(current.getNote());
        noteArea.setVisible(true);
    }

    /** Obsługa głównego przycisku: etap 1 (pokaż odp.) / etap 2 (następna) */
    private void handleMainButton() {
        if (current == null) return;

        if (waitingForAnswer) {                          // pokaż odpowiedź
            qaArea.setText(current.getAnswer());
            noteArea.setVisible(false);
            ratingPanel.setVisible(true);
            btnShow.setText("Następna fiszka");
            waitingForAnswer = false;
        } else {                                         // kolejna fiszka
            showNext();
        }
    }

    /** Ładuje kolejną fiskę lub kończy talię */
    private void showNext() {
        current = controller.nextCard();

        if (current != null) {                           // kolejna fiszka
            qaArea.setText(current.getQuestion());
            noteArea.setText("");
            noteArea.setVisible(false);
            ratingPanel.setVisible(false);
            btnHint.setEnabled(true);
            btnShow.setText("Pokaż odpowiedź");
            waitingForAnswer = true;
        } else {                                         // koniec talii
            qaArea.setText("Koniec fiszek.");
            btnShow.setEnabled(false);
            btnHint.setEnabled(false);
            ratingPanel.setVisible(false);
            waitingForAnswer = true;
        }
    }

    /** Zapisuje ocenę 1-4 i przechodzi do następnej fiszek */
    private void rate(int value) {
        if (current == null) return;
        controller.rateCard(current, value);             // ProgressTracker
        ratings.put(current, value);                     // lokalny podgląd
        showNext();
    }
}
