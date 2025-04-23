package org.ui.controller;

import org.model.FlashCard;
import org.persistence.FileCardStore;
import org.service.FlashCardService;

import java.io.File;
import java.io.IOException;

public class PlayerController {
    private final FlashCardService service;

    public PlayerController() {
        this.service = new FlashCardService(new FileCardStore());
    }

    public void loadCards(File file) throws IOException {
        service.loadFromFile(file);
    }

    public FlashCard nextCard() {
        return service.hasNext() ? service.nextCard() : null;
    }

    public void rateCard(FlashCard card, int rating) {
        service.rateCurrent(card, rating);
    }
}
