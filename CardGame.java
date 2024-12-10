package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * This Class is where the program objects are combined and actually assembled to prompt the user to begin the game.
 * CardGame starts the game by user via the command line how many players they wish to have, 
 * which also sets various variables, such as the amount of decks, players, and the overall number of cards available.
 * CardGame, after it has set n and has either generated or used a packFile proceeds with running the game via the startGame() method:
 * the functionality this method provides includes simply just setting up the game by distributing the hands for the player, so that they have the cards they need to get the game going.
 * The method is also responsible for calling various other methods that concurrently run each thread for n players
 * The method also takes care of finding a winner 
 * 
 * @author [Suraj]
 * @version 5.0
 */
public class CardGame {
    private final List<Player> players = new ArrayList<>(); // List of players
    private final List<CardDeck> decks = new ArrayList<>(); // List of decks
    private final List<Card> cardPack = new ArrayList<>(); // List of cards
    private final List<Thread> playerThreads = new ArrayList<>(); // List of player threads
    private static final Random RANDOM = new Random(); // Random object for generating random values
    private final int n; // Number of players
    public volatile boolean gameEnded = false; // Flag to indicate if the game has ended
    private List<String> methodCallLog = new ArrayList<>();  // List of method calls

    /**
     * Constructor for CardGame.
     * Initializes the game with the specified number of players and card pack.
     * 
     * @param n the number of players
     * @param cardPack the list of cards to be used in the game
     * @throws Exception if an error occurs while initializing the game
     */
    public CardGame(int n, List<Card> cardPack) {
        this.n = n;
        this.cardPack.addAll(cardPack);
        
        try {
            // Initialize decks
            for (int i = 1; i <= n; i++) {
                decks.add(new CardDeck(i));
            }
            
            // Initialize players with correct decks and preferred values
            for (int i = 1; i <= n; i++) {
                CardDeck leftDeck = decks.get((i - 1) % n);
                CardDeck rightDeck = decks.get(i % n);
                players.add(new Player(i, leftDeck, rightDeck, i, new ArrayList<>(), this));
            }
        } catch (Exception e) {
            System.err.println("Error initializing CardGame: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Distributes hands to players and remaining cards to decks (both in round-robin fashion).
     * 
     * @throws Exception if an error occurs while distributing hands
     */
    public void distributeHands() {
        try {
            // Distribute 4 cards to each player
            int playerIndex = 0;
            for (int i = 0; i < 4 * n; i++) {
                players.get(playerIndex).addToHand(cardPack.remove(0));
                playerIndex = (playerIndex + 1) % n;
            }
    
            // Distribute remaining cards to decks
            int deckIndex = 0;
            while (!cardPack.isEmpty()) {
                decks.get(deckIndex).addCard(cardPack.remove(0));
                deckIndex = (deckIndex + 1) % n;
            }
        } catch (Exception e) {
            System.err.println("Error distributing hands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notifies all players of the winner and ends the game.
     * 
     * @param winner the player who won the game
     */
    public synchronized void notifyWin(Player winner) {
        // This checks if the game has already ended.
        if (!gameEnded) {
            gameEnded = true;
    
            // fetches the ID of the winning player
            int winningPlayerId = winner.getId();
    
            // Announces the winner among the players
            System.out.println("Player " + winningPlayerId + " wins");
    
            // Notifies all players of the winner and interrupt their threads
            for (Player player : players) {
                if (player.getId() != winningPlayerId) {
                    player.winningPlayerId = winningPlayerId;
                }
            }
    
            // Stop execution of all players
            if (playerThreads != null) {
                for (Thread thread : playerThreads) {
                    if (thread.isAlive()) {
                        thread.interrupt();
                    }
                }
            }
    
            // logs all actions
            writeDeckStates();
        }
    }

        /**
     * Starts the game by distributing hands and starting player threads.
     * Runs a loop to check for immdiate winning hands and handles the game accordingly. 
     * Handles logging of player actions (winner & losers) and deck states.
     * 
     * @throws Exception if an error occurs while starting the game
     */
    public void startGame() {
        // Distribute cards to players
        distributeHands();
    
        // Log initial hands for all players
        for (Player player : players) {
            player.logAction("player " + player.getId() + " initial hand: " + player.formatHand());
        }
    
        // Check for initial winning hands
        for (Player player : players) {
            if (player.hasWinningHand()) {
                // Log the winner's information
                player.logAction("player " + player.getId() + " wins");
                player.logAction("player " + player.getId() + " exits");
                player.logAction("player " + player.getId() + " final hand: " + player.formatHand());
    
                // Notify the rest of the players about the winner
                notifyWin(player);
    
                // Log non-winners' responses and exit their threads
                for (Player nonWinningPlayer : players) {
                    if (nonWinningPlayer != player) {
                        nonWinningPlayer.logAction("player " + player.getId() + " has informed player " +
                                nonWinningPlayer.getId() + " that player " + player.getId() + " has won");
                        nonWinningPlayer.logAction("player " + nonWinningPlayer.getId() + " exits");
                        nonWinningPlayer.logAction("player " + nonWinningPlayer.getId() + " hand: " +
                                nonWinningPlayer.formatHand());
                    }
                }
    
                // Write deck states and terminate
                writeDeckStates();
                return;
            }
        }
    
        // If no initial winner, start player threads
        for (Player player : players) {
            Thread t = new Thread(player);
            playerThreads.add(t);
            t.start();
        }
    
        // Wait for all player threads to finish
        for (Thread t : playerThreads) {
            try {
                t.join(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle interruption gracefully
                break;
            }
        }
    
        // Write the final state of the decks
        writeDeckStates();
    }
    
    /**
     * Constructs a Card object with the specified value within the proper bounds. 
     * Ensures that the card's value is non-negative; otherwise, throws an exception.
     *
     * @param value the integer value of the card
     * @throws Exception if there's an incorrect deck trace
     */
    private void writeDeckStates() {
        decks.forEach(deck -> {
            try (FileWriter writer = new FileWriter("deck" + deck.getDeckNumber() + "_output.txt")) {
                writer.write(deck.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Main method to start the game.
     * Asks for input from the user to determine the number of players and the pack file to load.
     * Handles 'generate' command to create a random pack of cards.
     * 
     * @param args command line arguments
     * @throws IOException if an I/O error occurs while reading the pack file
     * @throws IOException if an I/O error occurs while generating a random pack
     * @throws Exception if an error occurs while starting the game
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Please enter the number of players: ");
        int n = scanner.nextInt();
        scanner.nextLine();

        List<Card> cardPack = new ArrayList<>();
        String packFile;

        while (true) {
            System.out.print("Please enter location of pack to load (or type 'generate' to create a new pack): ");
            packFile = scanner.nextLine();

            if (packFile.equalsIgnoreCase("generate")) {
                try {
                    generateRandomPack(n, "random_pack.txt");
                    packFile = "random_pack.txt";
                } catch (IOException e) {
                    System.out.println("Failed to generate random pack: " + e.getMessage());
                    continue;
                }
            }

            try (Scanner fileScanner = new Scanner(new File(packFile))) {
                cardPack.clear();
                while (fileScanner.hasNextInt()) {
                    cardPack.add(new Card(fileScanner.nextInt()));
                }
            } catch (IOException e) {
                System.out.println("Invalid pack file. Please try again.");
                continue;
            }

            if (cardPack.size() == 8 * n) {
                break; // Valid pack file loaded
            } else {
                System.out.println("Pack must contain exactly " + (8 * n) + " cards. Please try again.");
            }
        }

        CardGame game = new CardGame(n, cardPack);
        game.startGame();
    }

    /**
     * Generates a random pack of cards and saves it to a specified file.
     *
     * @param n        the number of players
     * @param fileName the name of the file to save the pack
     */
    public static void generateRandomPack(int n, String fileName) throws IOException {
        int totalCards = 8 * n;
        List<Integer> cardValues = new ArrayList<>();

        for (int i = 0; i < totalCards; i++) {
            cardValues.add(RANDOM.nextInt(2 * n));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int value : cardValues) {
                writer.write(value + "\n");
            }
        }
        System.out.println("Random pack generated and saved to " + fileName);
    }
}

