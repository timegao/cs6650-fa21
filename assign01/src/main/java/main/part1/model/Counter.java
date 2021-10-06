package main.part1.model;

/**
 * Synchronized counter
 */
public class Counter {
    private int count;

    public synchronized void increaseBy(int number) {
        this.count += number;
    }

    public synchronized int getCount() {
        return this.count;
    }
}
