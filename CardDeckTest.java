package test;

import main.Card;
import main.CardDeck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CardDeck class.
 * These tests validate the behavior of CardDeck under various scenarios, including
 * adding and drawing cards, handling edge cases like an empty deck, verifying order,
 * and ensuring thread safety.
 * 
 * @author [Suraj]
 * @version 5.0
 */
public class CardDeckTest {
    private CardDeck deck;

    /**
     * Sets up a new CardDeck instance before each test.
     */
    @BeforeEach
    public void setUp() {
        deck = new CardDeck(1);
    }

    /**
     * Tests adding a card to the deck and then drawing it.
     * Ensures the drawn card matches the added card.
     */
    @Test
    public void testAddAndDrawCard() {
        Card card = new Card(5);
        deck.addCard(card);

        Card drawnCard = deck.drawCard();
        assertEquals(5, drawnCard.getValue(), "Drawn card should match added card");
    }

    /**
     * Tests drawing a card from an empty deck.
     * Ensures that attempting to draw from an empty deck returns null.
     */
    @Test
    public void testDrawFromEmptyDeck() {
        assertNull(deck.drawCard(), "Drawing from empty deck should return null");
    }

    /**
     * Tests the toString() method of the CardDeck class.
     * Verifies that the deck contents are represented in the correct format.
     */
    @Test
    public void testToStringFormat() {
        deck.addCard(new Card(3));
        deck.addCard(new Card(1));
        deck.addCard(new Card(2));
        assertEquals("deck1 contents: 1 2 3", deck.toString().trim(), "Deck contents should be sorted and formatted correctly");
    }

    /**
     * Tests thread safety of the CardDeck class by having multiple threads add cards concurrently.
     * Verifies that all cards are added correctly without any data loss or corruption.
     *
     * @throws InterruptedException if any thread is interrupted during execution
     */
    @Test
    public void testThreadSafety() throws InterruptedException {
        final int numThreads = 5;
        final int numCardsPerThread = 100;
        Thread[] threads = new Thread[numThreads];

        // Multiple threads adding cards
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < numCardsPerThread; j++) {
                    deck.addCard(new Card(j));
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify total number of cards
        assertEquals(numThreads * numCardsPerThread, deck.getCards().size(), 
            "Deck should contain all added cards");
    }

    /**
     * Tests the First-In-First-Out (FIFO) order of the CardDeck.
     * Ensures that cards are drawn in the same order they were added.
     */
    @Test
    public void testFIFOOrder() {
        deck.addCard(new Card(1));
        deck.addCard(new Card(2));
        deck.addCard(new Card(3));

        assertEquals(1, deck.drawCard().getValue(), "First card drawn should be 1");
        assertEquals(2, deck.drawCard().getValue(), "Second card drawn should be 2");
        assertEquals(3, deck.drawCard().getValue(), "Third card drawn should be 3");
    }
}
