package org.persistence;

import org.model.FlashCard;

import java.io.*;
import java.util.*;

public class FileCardStore implements CardStore {
    @Override
    public List<FlashCard> load(File file) throws IOException {
        List<FlashCard> deck = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("/", 3);
                deck.add(new FlashCard(parts[0], parts.length>1?parts[1]:"", parts.length>2?parts[2]:""));
            }
        }
        return deck;
    }

    @Override
    public void save(List<FlashCard> cards, File file) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            for (FlashCard c : cards) {
                out.write(c.getQuestion() + "/" + c.getAnswer() + "/" + c.getNote());
                out.newLine();
            }
        }
    }
}
