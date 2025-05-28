package org.example.flashCards;



/*
  Enum reprezentujący status znajomości fiszki przez użytkownika
 */
public enum CardStatus {
    NEW("Nowa"),
    TO_REVIEW("Do powtórki"),
    MASTERED("Opanowana");

    private final String displayName;

    CardStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
