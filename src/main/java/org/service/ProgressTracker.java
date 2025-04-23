package org.service;

import org.model.FlashCard;

import java.util.HashMap;
import java.util.Map;

public class ProgressTracker {
    private final Map<FlashCard, Integer> ratings = new HashMap<>();

    /**
     * Zapisuje ocenę dla podanej fiszki (1–4).
     */
    public void recordRating(FlashCard card, int rating) {
        if (rating < 1 || rating > 4) {
            throw new IllegalArgumentException("Rating must be between 1 and 4");
        }
        ratings.put(card, rating);
    }

    /**
     * Zwraca mapę fiszka → ocena.
     */
    public Map<FlashCard, Integer> getRatings() {
        return new HashMap<>(ratings);
    }
}
