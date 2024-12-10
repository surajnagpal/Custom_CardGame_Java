package test;

import main.Card;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for the Card class.
 * These tests validate correct behavior of the Card class under normal, edge,
 * and concurrent conditions, ensuring robust and reliable functionality.
 *  
 * @author [Joshua Masih]
 * @version 5.0
 */
public class CardTest {

    /**
     * Tests that a Card object can be created with a valid value.
     * Ensures the value is correctly set and retrieved via getValue().
     */
    @Test
    void testValidCardCreation() {
        Card card = new Card(5);
        assertEquals(5, card.getValue(), "Card value should match the constructor parameter");
    }

    /**
     * Tests that creating a Card with a negative value throws an IllegalArgumentException.
     * Validates proper error handling for invalid inputs.
     */
    @Test
    void testNegativeValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Card(-1);
        }, "Negative card values should not be allowed");
    }

    /**
     * Tests the toString() method of the Card class.
     * Ensures the method returns the card's value as a string.
     */
    @Test
    public void testToString() {
        Card card = new Card(10);
        assertEquals("10", card.toString(), "toString should return value as string");
    }

    /**
     * Tests concurrent access to the Card's getValue() method.
     * Multiple threads read the value of a card simultaneously to verify
     * consistency and thread safety of the getValue() method.
     *
     * @throws InterruptedException if any thread is interrupted during execution
     */
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final Card card = new Card(5);
        final AtomicInteger sum = new AtomicInteger(0);
        final int numThreads = 10;
        final int operationsPerThread = 1000;
        Thread[] threads = new Thread[numThreads];

        // Multiple threads reading the card value
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    sum.addAndGet(card.getValue());
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify the sum is consistent with concurrent reads
        assertEquals(5 * numThreads * operationsPerThread, sum.get(), 
            "Concurrent reads should produce a consistent result");
    }

    /**
     * Tests concurrent access to the Card's toString() method.
     * Ensures that multiple threads calling toString() simultaneously produce
     * consistent and correct results.
     *
     * @throws InterruptedException if any thread is interrupted during execution
     */
    @Test
    public void testConcurrentToString() throws InterruptedException {
        final Card card = new Card(10);
        final String expectedString = "10";
        final int numThreads = 10;
        final int checksPerThread = 1000;
        Thread[] threads = new Thread[numThreads];
        AtomicInteger mismatchCount = new AtomicInteger(0);

        // Multiple threads calling toString
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < checksPerThread; j++) {
                    if (!card.toString().equals(expectedString)) {
                        mismatchCount.incrementAndGet();
                    }
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify no mismatches occurred
        assertEquals(0, mismatchCount.get(), "Concurrent toString calls should produce consistent results");
    }
}
