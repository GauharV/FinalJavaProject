package com.zingbah.racing.data;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Generic FIFO queue for items a kart is holding.
 * Backed by a LinkedList so it satisfies the Java 2 Queue/LinkedList requirement.
 *
 * @param <T> the item type (e.g., PowerUp)
 */
public class ItemQueue<T> {

    private final Queue<T> items = new LinkedList<>();

    /** Add an item to the back of the queue. */
    public void enqueue(T item) {
        items.offer(item);
    }

    /** Remove and return the item at the front. Returns null if empty. */
    public T dequeue() {
        return items.poll();
    }

    /** Peek at the front item without removing it. Returns null if empty. */
    public T peek() {
        return items.peek();
    }

    public boolean isEmpty() { return items.isEmpty(); }
    public int     size()    { return items.size();    }
    public void    clear()   { items.clear();          }
}
