package test;

import main.Card;
import main.CardDeck;
import main.CardGame;
import main.Player;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CardGame class.
 * These tests verify the correct setup and initialization of the game,
 * including proper handling of players, decks, and card distribution.
 * 
 * @author [Suraj]
 * @version 5.0
 */
public class CardGameTest {

    /**
     * Helper method to create and initialize a CardGame instance.
     * This method simulates creating a card pack for a given number of players,
     * initializing the game, and distributing hands.
     *
     * @param n The number of players.
     * @return A fully initialized CardGame instance.
     * @throws Exception if reflection access fails.
     */
    private CardGame createCardGame(int n) throws Exception {
        List<Card> cardPack = new ArrayList<>();
        for (int i = 0; i < 8 * n; i++) { 
            cardPack.add(new Card(i % 8)); 
        }
        CardGame game = new CardGame(n, cardPack);
        game.distributeHands();
        return game;
    }

    /**
     * Tests the number of players initialized in the game.
     * Verifies that the number of players matches the input parameter.
     *
     * @throws Exception if reflection access fails.
     */
    @Test
    public void testNumberOfPlayers() throws Exception {
        CardGame game = createCardGame(4);

        Field playersField = CardGame.class.getDeclaredField("players");
        playersField.setAccessible(true);
        List<Player> players = (List<Player>) playersField.get(game);

        assertEquals(4, players.size(), "Number of players should match the input.");
    }

    /**
     * Tests the number of decks initialized in the game.
     * Verifies that the number of decks matches the number of players.
     *
     * @throws Exception if reflection access fails.
     */
    @Test
    public void testNumberOfDecks() throws Exception {
        CardGame game = createCardGame(4);

        Field decksField = CardGame.class.getDeclaredField("decks");
        decksField.setAccessible(true);
        List<CardDeck> decks = (List<CardDeck>) decksField.get(game);

        assertEquals(4, decks.size(), "Number of decks should match the input.");
    }

    /**
     * Tests that the card pack is empty after game initialization.
     * Ensures that all cards are distributed during setup.
     *
     * @throws Exception if reflection access fails.
     */
    @Test
    public void testCardPackEmptyAfterInitialization() throws Exception {
        CardGame game = createCardGame(4);

        Field cardPackField = CardGame.class.getDeclaredField("cardPack");
        cardPackField.setAccessible(true);
        List<Card> remainingPack = (List<Card>) cardPackField.get(game);

        assertTrue(remainingPack.isEmpty(), "Card pack should be empty after initialization.");
    }

    /**
     * Tests that each deck contains cards after initialization.
     * Ensures that cards are distributed correctly to each deck.
     *
     * @throws Exception if reflection access fails.
     */
    @Test
    public void testDecksContainCards() throws Exception {
        CardGame game = createCardGame(4);

        Field decksField = CardGame.class.getDeclaredField("decks");
        decksField.setAccessible(true);
        List<CardDeck> decks = (List<CardDeck>) decksField.get(game);

        for (CardDeck deck : decks) {
            List<Card> deckCards = deck.getCards();
            assertFalse(deckCards.isEmpty(), "Each deck should have cards after initialization.");
        }
    }

    /**
     * Tests that each player has the correct number of cards in their hand.
     * Verifies that all players start with the expected hand size.
     *
     * @throws Exception if reflection access fails.
     */
    @Test
    public void testPlayersHaveInitialHands() throws Exception {
        CardGame game = createCardGame(4);

        Field playersField = CardGame.class.getDeclaredField("players");
        playersField.setAccessible(true);
        List<Player> players = (List<Player>) playersField.get(game);

        for (Player player : players) {
            Field handField = Player.class.getDeclaredField("hand");
            handField.setAccessible(true);
            List<Card> hand = (List<Card>) handField.get(player);

            assertEquals(4, hand.size(), "Each player should have 4 cards in their hand.");
        }
    }
}
