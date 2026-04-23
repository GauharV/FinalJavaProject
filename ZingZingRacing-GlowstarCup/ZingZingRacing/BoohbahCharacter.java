import java.awt.Color;

/**
 * Immutable data for one selectable Boohbah racer.
 */
public class BoohbahCharacter {

    public enum AbilityType {
        PRISM_SHIELD,
        COMET_DASH,
        MOON_LAUNCH,
        RIBBON_DRIFT,
        WILD_CHARGE
    }

    private final String name;
    private final Color color;
    private final double topSpeed;
    private final double acceleration;
    private final double handling;
    private final String description;
    private final AbilityType abilityType;
    private final String abilityName;
    private final String abilityDescription;
    private final int abilityDurationFrames;
    private final int abilityCooldownFrames;

    public BoohbahCharacter(String name, Color color,
                            double topSpeed, double acceleration,
                            double handling, String description,
                            AbilityType abilityType,
                            String abilityName,
                            String abilityDescription,
                            int abilityDurationFrames,
                            int abilityCooldownFrames) {
        this.name = name;
        this.color = color;
        this.topSpeed = topSpeed;
        this.acceleration = acceleration;
        this.handling = handling;
        this.description = description;
        this.abilityType = abilityType;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
        this.abilityDurationFrames = abilityDurationFrames;
        this.abilityCooldownFrames = abilityCooldownFrames;
    }

    public String getName() { return name; }
    public Color getColor() { return color; }
    public double getTopSpeed() { return topSpeed; }
    public double getAcceleration() { return acceleration; }
    public double getHandling() { return handling; }
    public String getDescription() { return description; }
    public AbilityType getAbilityType() { return abilityType; }
    public String getAbilityName() { return abilityName; }
    public String getAbilityDescription() { return abilityDescription; }
    public int getAbilityDurationFrames() { return abilityDurationFrames; }
    public int getAbilityCooldownFrames() { return abilityCooldownFrames; }

    public Color getAbilityAccent() {
        switch (abilityType) {
            case PRISM_SHIELD:
                return new Color(110, 220, 255);
            case COMET_DASH:
                return new Color(255, 210, 55);
            case MOON_LAUNCH:
                return new Color(255, 245, 195);
            case RIBBON_DRIFT:
                return new Color(255, 120, 200);
            case WILD_CHARGE:
                return new Color(80, 230, 130);
            default:
                return color;
        }
    }
}
