package org.service;

import org.model.FlashCard;
import org.model.FlashCardDeck;
import org.persistence.CardStore;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FlashCardService {
    private final FlashCardDeck deck = new FlashCardDeck();
    private final CardStore store;
    private final ProgressTracker tracker = new ProgressTracker();

    public FlashCardService(CardStore store) {
        this.store = store;
    }

    /**
     * Dodaje nową fiszkę do talii.
     */
    /**
     * Dodaje nową fiszkę do talii.
     */
    public void addCard(FlashCard card) {
        deck.add(card);
    }

    /**
     * Wczytuje fiszki z pliku, kasuje dotychczasowe, tasuje i resetuje iterator.
     */
    public void loadFromFile(File file) throws IOException {
        List<FlashCard> loaded = store.load(file);
        deck.clear();
        for (FlashCard c : loaded) {
            deck.add(c);
        }
        deck.shuffle();
        deck.resetIterator();
    }

    /**
     * Zapisuje aktualne fiszki do pliku.
     */
    public void saveToFile(File file) throws IOException {
        store.save(deck.getAll(), file);
    }

    public boolean hasNext() {
        return deck.hasNext();
    }

    public FlashCard nextCard() {
        return deck.next();
    }

    public void rateCurrent(FlashCard card, int rating) {
        tracker.recordRating(card, rating);
    }

    public Map<FlashCard, Integer> getRatings() {
        return tracker.getRatings();
    }
}
