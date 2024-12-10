package test;

import main.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Player class.
 * These tests verify core functionality, including:
 * - Checking for a winning hand.
 * - Adding cards to the player's hand.
 * - Choosing cards to discard.
 * - Simulating a player turn.
 * 
 * Reflection is used to access private methods and fields for comprehensive testing.
 * {@link https://stackoverflow.com Stack Overflow} was referenced for reflection techniques.
 * 
 * @author [Joshua Masih]
 * @version 5.0
 */
public class PlayerTest {

    /**
     * Tests if the player correctly identifies a winning hand when all cards have the same value.
     */
    @Test
    public void testHasWinningHand_WithWinningHand() {
        CardGame game = new CardGame(2, List.of(new Card(1), new Card(1), new Card(1), new Card(1)));
        Player player = new Player(1, new CardDeck(1), new CardDeck(2), 1, 
                List.of(new Card(1), new Card(1), new Card(1), new Card(1)), 
                game); 

        assertTrue(player.hasWinningHand(), "Player should win with four cards of the same value.");
    }

    /**
     * Tests if the player correctly identifies the absence of a winning hand when card values differ.
     */
    @Test
    public void testHasWinningHand_WithoutWinningHand() {
        CardGame game = new CardGame(2, List.of(new Card(1), new Card(2), new Card(3), new Card(4)));
        Player player = new Player(1, new CardDeck(1), new CardDeck(2), 1, 
                List.of(new Card(1), new Card(2), new Card(3), new Card(4)), 
                game);

        assertFalse(player.hasWinningHand(), "Player should not win with different card values.");
    }

    /**
     * Tests adding a card to the player's hand and verifies the updated hand size and content.
     */
    @Test
    public void testAddToHand() {
        CardGame game = new CardGame(2, List.of(new Card(1), new Card(2), new Card(3), new Card(4)));
        Player player = new Player(1, new CardDeck(1), new CardDeck(2), 1, 
                List.of(new Card(1), new Card(2), new Card(3), new Card(5)), 
                game);

        player.addToHand(new Card(5));
        
        try {
            List<Card> hand = getPrivateField(player, "hand");

            // Verify player's hand has 5 cards after addition
            assertEquals(5, hand.size(), "Player's hand should have 5 cards after adding one.");
            // Verify the newly added card has the correct value
            assertEquals(5, hand.get(4).getValue(), "The added card should have value 5.");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    /**
     * Tests the card selection logic for discarding, ensuring preference for non-preferred cards.
     */
    @Test
    public void testChooseCardToDiscard_PrefersNonPreferredCard() {
        CardGame game = new CardGame(2, List.of(new Card(1), new Card(2), new Card(3), new Card(4)));
        CardDeck leftDeck = new CardDeck(1);
        CardDeck rightDeck = new CardDeck(2);
        Player player = new Player(1, leftDeck, rightDeck, 1, 
                List.of(new Card(1), new Card(2), new Card(3), new Card(4)), 
                game);

        try {
            Method chooseCardToDiscardMethod = Player.class.getDeclaredMethod("chooseCardToDiscard");
            chooseCardToDiscardMethod.setAccessible(true);
            Card discardedCard = (Card) chooseCardToDiscardMethod.invoke(player);

            // Verify that the discarded card is not the preferred value
            assertNotEquals(1, discardedCard.getValue(), "Player should prefer to discard a non-preferred card.");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    /**
     * Tests card selection logic when all cards have the preferred value.
     */
    @Test
    public void testChooseCardToDiscard_AllPreferredValues() {
        CardGame game = new CardGame(2, List.of(new Card(1), new Card(1), new Card(1), new Card(1)));
        CardDeck leftDeck = new CardDeck(1);
        CardDeck rightDeck = new CardDeck(2);
        Player player = new Player(1, leftDeck, rightDeck, 1, 
                List.of(new Card(1), new Card(1), new Card(1), new Card(1)), 
                game);

        try {
            Method chooseCardToDiscardMethod = Player.class.getDeclaredMethod("chooseCardToDiscard");
            chooseCardToDiscardMethod.setAccessible(true);
            Card discardedCard = (Card) chooseCardToDiscardMethod.invoke(player);

            // Verify the discarded card is one of the preferred values
            assertEquals(1, discardedCard.getValue(), "Player should discard one of the preferred cards.");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }   

    /**
     * Tests the full play turn sequence, ensuring the player draws and discards correctly.
     */
    @Test
    public void testPlayTurn() {
        CardDeck leftDeck = new CardDeck(1);
        CardDeck rightDeck = new CardDeck(2);

        // Add a card to the left deck for the player to draw
        leftDeck.addCard(new Card(5));
        Player player = new Player(1, leftDeck, rightDeck, 1, 
                List.of(new Card(1), new Card(2), new Card(3), new Card(4)), 
                new CardGame(2, List.of(new Card(1), new Card(2), new Card(3), new Card(4))));

        player.playTurn();

        try {
            List<Card> hand = getPrivateField(player, "hand");

            // Verify the player's hand size and contents
            assertEquals(4, hand.size(), "Player should have 4 cards after playing a turn.");
            assertTrue(hand.stream().anyMatch(card -> card.getValue() == 5), "Player should have drawn the card from the left deck.");
            assertEquals(1, rightDeck.getCards().size(), "Right deck should have one discarded card.");
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    /**
     * Utility method to access private fields using reflection.
     * @param obj The object containing the field.
     * @param fieldName The name of the field to access.
     * @return The value of the field.
     * @throws NoSuchFieldException, IllegalAccessException if the field is not found or inaccessible.
     * 
     * {@link https://stackoverflow.com Stack Overflow} was referenced for reflection techniques.
     */
    private List<Card> getPrivateField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (List<Card>) field.get(obj);
    }
}
