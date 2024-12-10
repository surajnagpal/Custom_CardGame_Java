package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This Class is the construct being used for a player and what defines them
 * The Player draws and discards cards, logging their actions to an output file
 * The Player class implements {@code Runnable} for concurrent execution, allowing for thread-safe objects
 * 
 * @author [Joshua Masih]
 * @version 5.0
 */


public class Player implements Runnable {
    private final int preferredValue; // The Player object's unique preferred card value
    private final List<Card> hand; // The Player's current hand of cards
    private final int playerId; // Unique id for the Player object
    private final CardDeck leftDeck; // Deck on the Player's left, to emulate a round-robin fashion game
    private final CardDeck rightDeck; // Deck on the Player's right, to emulate a round-robin fashion game
    private final ReentrantLock lock = new ReentrantLock(); // Lock for concurrency that makes sure there is only one player that accesses the functionality of adding or drawinng
    private final String outputFile; // the name of a generated output file, for each respective Player object 
    private final CardGame game; // a CardGame object
    public volatile int winningPlayerId = -1; //why volatile? 

public Player(int playerId, CardDeck leftDeck, CardDeck rightDeck, 
                 int preferredValue, List<Card> initialHand, CardGame game) {
        Objects.requireNonNull(leftDeck, "Left deck cannot be null");
        Objects.requireNonNull(rightDeck, "Right deck cannot be null");
        Objects.requireNonNull(initialHand, "Initial hand cannot be null");
        Objects.requireNonNull(game, "Card game cannot be null");

        if (playerId < 1) {
            throw new IllegalArgumentException(
                String.format("Player ID must be positive. Received: %d", playerId)
            );
        }
        this.playerId = playerId;                  
        this.leftDeck = leftDeck;
        this.rightDeck = rightDeck;
        this.preferredValue = preferredValue;
        this.hand = new ArrayList<>(initialHand);
        this.outputFile = "player" + playerId + "_output.txt";
        this.game = game;
        
        // Create new output file or clear existing one
        try (FileWriter writer = new FileWriter(outputFile, false)) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a card to the Player's hand 
     * 
     * @param card: a new card is added to the end of the hand list
     */
    public void addToHand(Card card) {
        hand.add(card);
    }


     /**
     * Returns the unique Player id.
     *
     * @return the denomination for the Player Object
     */
    public int getId() {
        return playerId;
    }

     /**
     * Returns the boolean value for if this player's winningHand has been achieved
     * <p>
     * This method has a check to make sure that their are four cards in a hand, which chase returns false
     * The next lines check if the Player has four of the same value cards that the object prefers, and if they all match:
     * </p>
     * @return a boolean that confirms the Player object has a winning hand
     */
    public boolean hasWinningHand() {
        if (hand.size() != 4) return false;
        int value = hand.get(0).getValue();
        return hand.stream().allMatch(card -> card.getValue() == value);
    }

    /**
     * Logs each action for the Player object to it's apprpriate output file w/in a try and catch statement to ensure exceptions are caught
     * This method is synchronized to ensure that only one thread can write to a while securely
     * This method begins by creating a BufferedWriter that writes each appropriate action
     * to the Player's output flle, and it flushes after it is done going to the next line
     *
     * @param action: a new action for the Player is recorded to the output file
     * @throws IOException if an I/O error occurs
     */
    public synchronized void logAction(String action) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))){
            writer.write(action);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the Player object's current hand, used every time an action needs to be logged
     * <p>
     * This is a static metod that runs through the entirety of the hand list, and prints each value
     * for the StringBuilder object that is creted locally within the method
     * </p>
     *
     * @return string of all the values for this Player Object's hand
     */
    String formatHand() {
        StringBuilder handStr = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            handStr.append(hand.get(i).getValue());
            if (i < hand.size() - 1) {
                handStr.append(" ");
            }
        }
        return handStr.toString();
    }


    /**
     * Allows the Player object to actually choose the next card they would like to add or discard
     * <p>
     * This method starts by putting a lock on the threads, so that only this player can play his turn
     * and has access to the decks without interruption, this happens by the player drawing a card from the leftDeck
     * and logging that action if it isn't null, they then need to discard a card, which is done via the discardCard() method
     * </p>
     *
     */
    public void playTurn() {
        lock.lock(); //locking the current thread so that the object can proceed with uninterupted access to adding & discarding from their leftDeck to their rightDeck, serves as a sanity check
        try {
            // Draw card from left deck
            Card drawnCard = leftDeck.drawCard();
            if (drawnCard != null) {
                hand.add(drawnCard);
                logAction("player " + playerId + " draws a " + drawnCard.getValue() + //log this action to display a drawn card to the hand of the Player Object, use formatHand() to show all of this deck
                         " from deck " + leftDeck.getDeckNumber());

                // Choose card to discard (non-preferred value if possible)
                Card discardCard = chooseCardToDiscard();
                hand.remove(discardCard);
                rightDeck.addCard(discardCard); //after the card to discard has been chosen, the player adds it to the right deck, continuing the round-robin fashion of the game
                logAction("player " + playerId + " discards a " + discardCard.getValue() +  //log this action to display the discarded card of the Player Object
                         " to deck " + rightDeck.getDeckNumber());
                logAction("player " + playerId + " current hand is " + formatHand()); //log this action to display the current hand of the Player Object
            }
        } finally {
            lock.unlock(); //ending the lock so that the next thread can take over adding/discarding cards
        }
    }


    /**
     * Main game strategy to decide what card the Player discards based on the preferred Value
     * <p>
     * This method ensures that the card that is going going to be discarded out of the current 5 
     * cards in the deck is most definitetly not one that the player preferres
     * Does so by running through a loop that checks if a card is unfavored, if that card is encountered, it is returned to be dealt with
     * </p>
     *
     * @return card: that is unfavored either from the top if all the same, otherwise via iteration
     */
    private Card chooseCardToDiscard() {
        // First try to discard a non-preferred card
        for (Card card : hand) {
            if (card.getValue() != preferredValue) {
                return card;
            }
        }
        // If all cards are preferred, discard the first one
        return hand.get(0);
    }


    /**
     * Allows the Player object to determine if this Player object or another object has won 
     * This method starts by logging the initial hand in the output file
     * Then proceeds to check through various checks ensuring various cases for a winning hand
     * This includes winning via an initial hand that could be dealt
     * The run method then proceeds to the next step if this base case isn't met through a loop 
     * that checks to make sure the thread for this player hasn't been interrupted, in the case that it has
     * the next step is to declare the win for the loosing players that have a bit of different output that is logged
     * this method is the only time the game object is used for the Player class, and that is to notify the win to the game for this player and then we break
     *      
     * @throws InterruptedException if the thread is interrupted
     * @throws Exception if an unexpected error occurs
     */
    @Override
    public void run() {
    // Log initial hand
    logAction("player " + playerId + " initial hand " + formatHand());

        try {
            Thread.sleep(50); // Small delay to ensure file operations complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logAction("Player " + playerId + " thread interrupted");
            return;
        }

        // Main game loop
        try {
            while (!Thread.currentThread().isInterrupted() && !game.gameEnded) {
                playTurn();
                if (hasWinningHand()) {
                    logAction("player " + playerId + " wins");
                    logAction("player " + playerId + " exits");
                    logAction("player " + playerId + " final hand: " + formatHand());
                    game.notifyWin(this);
                    break;
                }

                Thread.sleep(10); // Small delay to prevent CPU overload
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Catch any unexpected exceptions
            System.err.println("Unexpected error in player " + playerId + " thread: " + e.getMessage());
            logAction("Unexpected error occurred");
        } finally {
            if (!hasWinningHand() && winningPlayerId != -1) {
                notifyLoser(winningPlayerId);
            }
        }
    }

    public void notifyLoser(int winnerId) {
        logAction("player " + winnerId + " has informed player " +
                playerId + " that player " + winnerId + " has won");
        logAction("player " + playerId + " exits");
        logAction("player " + playerId + " hand: " + formatHand());
    }
} 