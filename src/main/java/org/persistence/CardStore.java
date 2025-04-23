package org.persistence;

import org.model.FlashCard;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface CardStore {
    List<FlashCard> load(File file) throws IOException;
    void save(List<FlashCard> cards, File file) throws IOException;
}