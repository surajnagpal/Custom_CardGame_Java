package main;

/**
 * This Class is the construct that represents a singular card with an integer value.
 * This class ensures that each card has a non-negative value, while providing
 * functionality to retrieve the card's value and a string representation via the toString()
 * 
 * @author [Joshua Masih]
 * @version 5.0
 */
public class Card {

    // Private final variable to store the card's value, which must always be non-negative.
    private final int value;

    /**
     * Constructs a Card object with the specified value within the proper bounds. 
     * Ensures that the card's value is non-negative; otherwise, throws an exception.
     *
     * @param value the integer value of the card
     * @throws IllegalArgumentException if the value is negative
     */
        public Card(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Card value cannot be negative");
        }
        this.value = value;
    }

    /**
     * Returns the value of the card.
     *
     * @return the integer value of this specific card object.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns a string representation of the card.
     *
     * @return a string representation of the card's value
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

}