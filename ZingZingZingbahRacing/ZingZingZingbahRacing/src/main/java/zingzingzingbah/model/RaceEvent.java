package zingzingzingbah.model;

/**
 * Generic race event that can carry any type of data payload.
 *
 * Java 2 concepts demonstrated:
 *   - Generics: RaceEvent<T> allows typed event data (e.g. Integer for lap number,
 *     String for messages, Kart for collision events)
 *   - Enum for event type categorization
 */
public class RaceEvent<T> {

    public enum EventType {
        RACE_START,
        COUNTDOWN,
        LAP_COMPLETE,
        CHECKPOINT_PASS,
        RACE_FINISH,
        COLLISION,
        OFF_TRACK
    }

    private final EventType type;
    private final T data;
    private final long timestamp;

    public RaceEvent(EventType type, T data) {
        this.type      = type;
        this.data      = data;
        this.timestamp = System.currentTimeMillis();
    }

    public EventType getType()  { return type; }
    public T getData()          { return data; }
    public long getTimestamp()  { return timestamp; }

    @Override
    public String toString() {
        return "[" + type.name() + "] " + data;
    }
}
