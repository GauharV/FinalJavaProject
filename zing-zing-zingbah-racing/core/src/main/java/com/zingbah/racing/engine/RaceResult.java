package com.zingbah.racing.engine;

import com.zingbah.racing.characters.BoohbahCharacter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Holds the final race standings as a generic list sorted by finish position.
 * Demonstrates generics (Java 2 requirement): RaceResult<T extends Comparable<T>>
 */
public class RaceResult<T extends Comparable<T>> {

    public static class Entry<T> {
        public final int              position;
        public final BoohbahCharacter character;
        public final boolean          isPlayer;
        public final T                value;       // e.g. finish time or score

        public Entry(int position, BoohbahCharacter character, boolean isPlayer, T value) {
            this.position  = position;
            this.character = character;
            this.isPlayer  = isPlayer;
            this.value     = value;
        }

        @Override
        public String toString() {
            return position + ". " + character.displayName + (isPlayer ? " (YOU)" : "");
        }
    }

    private final List<Entry<T>> entries = new ArrayList<>();

    public void add(Entry<T> entry) {
        entries.add(entry);
        entries.sort(Comparator.comparingInt(e -> e.position));
    }

    public List<Entry<T>> getEntries() { return entries; }

    public Entry<T> getPlayerEntry() {
        return entries.stream().filter(e -> e.isPlayer).findFirst().orElse(null);
    }

    public int size() { return entries.size(); }
}
