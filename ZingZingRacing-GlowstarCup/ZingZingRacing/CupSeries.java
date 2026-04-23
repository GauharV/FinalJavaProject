import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages cup order and scoring across multiple circuits.
 */
public class CupSeries {
    private static final int[] POINTS_TABLE = {15, 12, 10, 8, 6};

    private final String cupName;
    private final List<Track.Layout> circuits;
    private final List<BoohbahCharacter> roster;
    private final BoohbahCharacter playerCharacter;
    private final Map<BoohbahCharacter, Integer> points;

    private int currentCircuitIndex;
    private List<Racer> lastRaceResults;
    private boolean complete;

    public CupSeries(List<BoohbahCharacter> roster, BoohbahCharacter playerCharacter) {
        this.cupName = "Glowstar Cup";
        this.circuits = new ArrayList<Track.Layout>();
        circuits.add(Track.Layout.SKY_RIBBON);
        circuits.add(Track.Layout.MOON_GARDEN);
        circuits.add(Track.Layout.SUNSET_HARBOR);

        this.roster = new ArrayList<BoohbahCharacter>(roster);
        this.playerCharacter = playerCharacter;
        this.points = new HashMap<BoohbahCharacter, Integer>();
        for (BoohbahCharacter character : roster) {
            points.put(character, Integer.valueOf(0));
        }

        this.currentCircuitIndex = 0;
        this.lastRaceResults = null;
        this.complete = false;
    }

    public String getCupName() { return cupName; }
    public BoohbahCharacter getPlayerCharacter() { return playerCharacter; }
    public Track.Layout getCurrentCircuit() { return circuits.get(currentCircuitIndex); }
    public int getCurrentCircuitNumber() { return currentCircuitIndex + 1; }
    public int getTotalCircuits() { return circuits.size(); }
    public boolean isComplete() { return complete; }
    public boolean hasNextCircuit() { return currentCircuitIndex < circuits.size() - 1; }

    public void advanceToNextCircuit() {
        if (hasNextCircuit()) {
            currentCircuitIndex++;
        }
    }

    public void recordRace(List<Racer> results) {
        lastRaceResults = new ArrayList<Racer>(results);
        for (int i = 0; i < results.size(); i++) {
            BoohbahCharacter character = results.get(i).getCharacter();
            int newTotal = getPoints(character) + getPointAward(i);
            points.put(character, Integer.valueOf(newTotal));
        }
        complete = currentCircuitIndex == circuits.size() - 1;
    }

    public int getPointAward(int zeroBasedPlace) {
        if (zeroBasedPlace < 0 || zeroBasedPlace >= POINTS_TABLE.length) {
            return 0;
        }
        return POINTS_TABLE[zeroBasedPlace];
    }

    public int getPoints(BoohbahCharacter character) {
        Integer total = points.get(character);
        return total == null ? 0 : total.intValue();
    }

    public List<Racer> getLastRaceResults() {
        return lastRaceResults == null ? new ArrayList<Racer>() : new ArrayList<Racer>(lastRaceResults);
    }

    public List<BoohbahCharacter> getStandings() {
        List<BoohbahCharacter> sorted = new ArrayList<BoohbahCharacter>(roster);
        sorted.sort((a, b) -> {
            int pointDiff = getPoints(b) - getPoints(a);
            if (pointDiff != 0) {
                return pointDiff;
            }

            int lastRaceDiff = getLastRacePlace(a) - getLastRacePlace(b);
            if (lastRaceDiff != 0) {
                return lastRaceDiff;
            }

            return a.getName().compareTo(b.getName());
        });
        return sorted;
    }

    public int getStandingPosition(BoohbahCharacter character) {
        List<BoohbahCharacter> standings = getStandings();
        return standings.indexOf(character) + 1;
    }

    public BoohbahCharacter getCupWinner() {
        List<BoohbahCharacter> standings = getStandings();
        return standings.isEmpty() ? playerCharacter : standings.get(0);
    }

    public String getNextCircuitName() {
        if (!hasNextCircuit()) {
            return "";
        }
        return circuits.get(currentCircuitIndex + 1).getDisplayName();
    }

    private int getLastRacePlace(BoohbahCharacter character) {
        if (lastRaceResults == null) {
            return roster.indexOf(character) + 1;
        }

        for (int i = 0; i < lastRaceResults.size(); i++) {
            if (lastRaceResults.get(i).getCharacter() == character) {
                return i + 1;
            }
        }
        return roster.size() + 1;
    }
}
