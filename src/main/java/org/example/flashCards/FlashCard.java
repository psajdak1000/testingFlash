package org.example.flashCards;

import java.util.Objects;

public class FlashCard {
    private String question;
    private String answer;
    private String note;
    private CardStatus status;
    private int rating; // 0 = nie oceniono, 1-4 = skala ocen

    public FlashCard(String question, String answer, String note) {
        this.question = (question != null) ? question.trim() : "";
        this.answer = (answer != null) ? answer.trim() : "";
        this.note = (note != null) ? note.trim() : "";
        this.status = CardStatus.NEW;
        this.rating = 0;
    }

    // Konstruktor z pełnymi parametrami (dla wczytywania z pliku)
    public FlashCard(String question, String answer, String note, CardStatus status, int rating) {
        this.question = (question != null) ? question.trim() : "";
        this.answer = (answer != null) ? answer.trim() : "";
        this.note = (note != null) ? note.trim() : "";
        this.status = (status != null) ? status : CardStatus.NEW;
        this.rating = rating;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getNote() {
        return note;
    }

    public CardStatus getStatus() {
        return status;
    }

    public int getRating() {
        return rating;
    }

    // Setters z walidacją
    public void setQuestion(String question) {
        this.question = (question != null) ? question.trim() : "";
    }

    public void setAnswer(String answer) {
        this.answer = (answer != null) ? answer.trim() : "";
    }

    public void setNote(String note) {
        this.note = (note != null) ? note.trim() : "";
    }

    public void setStatus(CardStatus status) {
        this.status = (status != null) ? status : CardStatus.NEW;
    }

    public void setRating(int rating) {
        this.rating = Math.max(0, Math.min(4, rating)); // Ograniczenie do zakresu 0-4
        updateStatusBasedOnRating();
    }

    /**
     * Aktualizuje status fiszki na podstawie ostatniej oceny
     */
    private void updateStatusBasedOnRating() {
        switch (rating) {
            case 1: // "Nie umiem"
            case 2: // "Średnio"
                this.status = CardStatus.TO_REVIEW;
                break;
            case 3: // "Dobrze"
            case 4: // "Umiem i jestem pewien"
                this.status = CardStatus.MASTERED;
                break;
            default:
                // Pozostaw obecny status dla rating = 0
                break;
        }
    }

    // Metody pomocnicze
    public boolean hasNote() {
        return note != null && !note.isEmpty();
    }

    public boolean isEmpty() {
        return (question == null || question.isEmpty()) &&
                (answer == null || answer.isEmpty());
    }

    public boolean isToReview() {
        return status == CardStatus.TO_REVIEW;
    }

    public boolean isMastered() {
        return status == CardStatus.MASTERED;
    }

    public boolean isNew() {
        return status == CardStatus.NEW;
    }

    /**
     * Zwraca tekstową reprezentację oceny
     */
    public String getRatingText() {
        switch (rating) {
            case 1: return "❌ Nie umiem";
            case 2: return "😐 Średnio";
            case 3: return "👍 Dobrze";
            case 4: return "💯 Umiem i jestem pewien";
            default: return "Nie oceniono";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FlashCard flashCard = (FlashCard) obj;
        return rating == flashCard.rating &&
                Objects.equals(question, flashCard.question) &&
                Objects.equals(answer, flashCard.answer) &&
                Objects.equals(note, flashCard.note) &&
                status == flashCard.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, answer, note, status, rating);
    }

    @Override
    public String toString() {
        return "FlashCard{" +
                "question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", note='" + note + '\'' +
                ", status=" + status +
                ", rating=" + rating +
                '}';
    }
}