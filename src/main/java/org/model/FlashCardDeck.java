package org.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FlashCardDeck {
    private final List<FlashCard> cards = new ArrayList<>();
    private Iterator<FlashCard> iterator;

    public void add(FlashCard card) {
        cards.add(card);
    }

    public List<FlashCard> getAll() {
        return Collections.unmodifiableList(cards);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public void resetIterator() {
        iterator = cards.iterator();
    }

    public boolean hasNext() {
        return iterator != null && iterator.hasNext();
    }

    public FlashCard next() {
        if (iterator == null) resetIterator();
        return iterator.next();
    }

    public int size() {
        return cards.size();
    }

    public void clear() {
        cards.clear();
        iterator = null;
    }
}
