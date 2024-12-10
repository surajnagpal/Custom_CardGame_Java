package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * This Class is the construct being used for a deck of 4 cards in each game.
 * The CardDeck is assigned a unique deck number and provides functionality for:
 * adding cards, drawing cards, and printing the current deck, which is done via the toString() method
 * The class is thread-safe, ensuring proper synchronization when accessed by multiple threads.
 * 
 * @author [Suraj]
 * @version 5.0
 */
public class CardDeck {

    // Unique deck id for this deck
    private final int deckNumber;
    
    // LinkedList to store the cards in the deck, better to use a singly-linked list as we only want access to what is at the front of the deck
    private final LinkedList<Card> cards = new LinkedList<>();

    /**
     * Constructs a new deck object with the specified deck number during instantiation.
     *
     * @param deckNumber: the unique deck number identifying this deck
     * @throws IllegalArgumentException if the deck number is less than 1
     */
    public CardDeck(int deckNumber) {
        if (deckNumber < 1) {
            throw new IllegalArgumentException(
                String.format("Deck number must be positive. Received: %d", deckNumber)
            );
        }
        this.deckNumber = deckNumber;
    }

    /**
     * Returns the unique deck id.
     *
     * @return the unique number for this deck
     */
    public int getDeckNumber() {
        return deckNumber;
    }

    /**
     * Retrieves a copy of the current list of cards in the deck.
     * This method ensures thread safety by synchronizing access to the internal list.
     * 
     * @return a list containing all cards currently in the deck, of type Card.
     */
    public synchronized List<Card> getCards() {
        return new ArrayList<>(cards);
    }

    /**
     * Adds a card to the end of the deck.
     * This method is synchronized to ensure that only one thread can add a card
     * at a time, maintaining thread safety.
     *
     * @param card: a new card is added to the LinkedList holding the cards towards the end
     * @throws IllegalArgumentException if the value is null
     */
    public synchronized void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Card cannot be null");
        }
        cards.addLast(card);
    }

    /**
     * Draws a card from the beginning of the deck.
     * If the deck is empty, the method returns {@code null}.
     * This method is synchronized to ensure thread safety during concurrent access.
     *
     * @return the first card is removed as long as the LinkedList holding the cards isn't empty
     */
    public synchronized Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.removeFirst();
    }

    /**
     * Returns a string representation of the deck.
     * The string includes the deck number and the sorted list of card values in the deck.
     * This method is synchronized to ensure thread safety.
     *
     * @return a string representation of the deck
     */
    @Override
    public synchronized String toString() {
        StringBuilder result = new StringBuilder();
        result.append("deck").append(deckNumber).append(" contents:");
        
        List<Card> sortedCards = new ArrayList<>(cards);
        Collections.sort(sortedCards, (card1, card2) -> 
            Integer.compare(card1.getValue(), card2.getValue()));
        
        for (Card card : sortedCards) {
            result.append(" ").append(card.getValue());
        }
        
        return result.toString();
    }
}

