package org.ui.controller;

import org.model.FlashCard;
import org.persistence.FileCardStore;
import org.service.FlashCardService;

import java.io.File;
import java.io.IOException;

public class BuilderController {
    private final FlashCardService service;

    public BuilderController() {
        this.service = new FlashCardService(new FileCardStore());
    }

    /**
     * Tworzy i dodaje nową fiszkę.
     */
    public void addCard(String question, String answer, String note) {
        FlashCard card = new FlashCard(question, answer, note);
        service.addCard(card);
    }

    /**
     * Zapisuje wszystkie stworzone fiszki do pliku.
     */
    public void saveCards(File file) throws IOException {
        service.saveToFile(file);
    }

    /**
     * Wczytuje fiszki z pliku.
     */
    public void loadCards(File file) throws IOException {
        service.loadFromFile(file);
    }
}

