package zingzingzingbah.screen;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import zingzingzingbah.GameApp;
import zingzingzingbah.model.Character;

import java.util.List;
import java.util.Random;

/**
 * Character selection screen drawn on a Canvas.
 * The player clicks a character card to select it, then clicks "START RACE!".
 *
 * Java 2 concepts demonstrated:
 *   - List<Character> iteration
 *   - Event-driven design (mouse handlers)
 */
public class CharacterSelectScreen extends Pane {

    // -----------------------------------------------------------------------
    // Layout constants
    // -----------------------------------------------------------------------
    private static final double W          = 900;
    private static final double H          = 600;
    private static final double CARD_W     = 142;
    private static final double CARD_H     = 235;
    private static final double CARD_GAP   = 12;
    private static final double CARD_Y     = 190;
    // Total 5 cards + 4 gaps = 5*142 + 4*12 = 758; start X = (900-758)/2 = 71
    private static final double CARD_START = (W - (5 * CARD_W + 4 * CARD_GAP)) / 2.0;

    private static final double BTN_X  = 310;
    private static final double BTN_Y  = 455;
    private static final double BTN_W  = 280;
    private static final double BTN_H  = 52;

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------
    private final GameApp        gameApp;
    private final Canvas         canvas;
    private final List<Character> characters;
    private int selectedIndex  = 0;
    private int hoveredIndex   = -1;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public CharacterSelectScreen(GameApp gameApp) {
        this.gameApp    = gameApp;
        this.characters = Character.getAllCharacters();
        this.canvas     = new Canvas(W, H);
        getChildren().add(canvas);
        setupMouseHandlers();
        draw();
    }

    // -----------------------------------------------------------------------
    // Mouse input
    // -----------------------------------------------------------------------
    private void setupMouseHandlers() {
        canvas.setOnMouseMoved(e -> {
            hoveredIndex = cardIndexAt(e.getX(), e.getY());
            draw();
        });

        canvas.setOnMouseClicked(e -> {
            int idx = cardIndexAt(e.getX(), e.getY());
            if (idx >= 0) {
                selectedIndex = idx;
                draw();
            }
            if (isOverStartButton(e.getX(), e.getY())) {
                gameApp.startRace(characters.get(selectedIndex));
            }
        });
    }

    /** @return index 0-4 of the card under (mx, my), or -1 if none. */
    private int cardIndexAt(double mx, double my) {
        for (int i = 0; i < characters.size(); i++) {
            double cx = CARD_START + i * (CARD_W + CARD_GAP);
            if (mx >= cx && mx <= cx + CARD_W && my >= CARD_Y && my <= CARD_Y + CARD_H) {
                return i;
            }
        }
        return -1;
    }

    private boolean isOverStartButton(double mx, double my) {
        return mx >= BTN_X && mx <= BTN_X + BTN_W && my >= BTN_Y && my <= BTN_Y + BTN_H;
    }

    // -----------------------------------------------------------------------
    // Drawing
    // -----------------------------------------------------------------------
    private void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, W, H);

        drawBackground(gc);
        drawTitle(gc);
        for (int i = 0; i < characters.size(); i++) {
            drawCard(gc, i);
        }
        drawStartButton(gc);
        drawSelectedInfo(gc);
    }

    private void drawBackground(GraphicsContext gc) {
        LinearGradient bg = new LinearGradient(
                0, 0, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#080820")),
                new Stop(1.0, Color.web("#1C0835")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // Static star field
        Random rng = new Random(123);
        for (int i = 0; i < 100; i++) {
            double sx   = rng.nextDouble() * W;
            double sy   = rng.nextDouble() * H;
            double size = rng.nextDouble() * 2.5 + 0.5;
            gc.setFill(Color.color(1, 1, 1, 0.2 + rng.nextDouble() * 0.6));
            gc.fillOval(sx - size / 2, sy - size / 2, size, size);
        }
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setTextAlign(TextAlignment.CENTER);

        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 54));
        gc.fillText("ZING ZING ZINGBAH", W / 2 + 3, 78);

        // Main title
        gc.setFill(Color.web("#FFD700"));
        gc.fillText("ZING ZING ZINGBAH", W / 2, 75);

        gc.setFill(Color.web("#FF69B4"));
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 34));
        gc.fillText("R A C I N G", W / 2, 118);

        gc.setFill(Color.color(1, 1, 1, 0.65));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        gc.fillText("SELECT YOUR BOOHBAH", W / 2, 155);
    }

    private void drawCard(GraphicsContext gc, int i) {
        double cx = CARD_START + i * (CARD_W + CARD_GAP);
        Character ch      = characters.get(i);
        boolean selected  = (i == selectedIndex);
        boolean hovered   = (i == hoveredIndex) && !selected;

        // Card background
        double alpha = selected ? 0.30 : hovered ? 0.16 : 0.09;
        gc.setFill(Color.color(1, 1, 1, alpha));
        gc.fillRoundRect(cx, CARD_Y, CARD_W, CARD_H, 16, 16);

        // Card border
        if (selected) {
            gc.setStroke(ch.getBodyColor());
            gc.setLineWidth(3.0);
        } else if (hovered) {
            gc.setStroke(Color.color(1, 1, 1, 0.5));
            gc.setLineWidth(1.5);
        } else {
            gc.setStroke(Color.color(1, 1, 1, 0.22));
            gc.setLineWidth(1.0);
        }
        gc.strokeRoundRect(cx, CARD_Y, CARD_W, CARD_H, 16, 16);

        // Selected glow
        if (selected) {
            gc.setFill(Color.color(
                    ch.getBodyColor().getRed(),
                    ch.getBodyColor().getGreen(),
                    ch.getBodyColor().getBlue(), 0.18));
            gc.fillRoundRect(cx - 6, CARD_Y - 6, CARD_W + 12, CARD_H + 12, 20, 20);
        }

        // Boohbah character drawing
        drawBoohbah(gc, cx + CARD_W / 2.0, CARD_Y + 90, ch, selected);

        // Name
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(ch.getName(), cx + CARD_W / 2, CARD_Y + 168);

        // Description
        gc.setFill(Color.color(1, 1, 1, 0.60));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.fillText(ch.getDescription(), cx + CARD_W / 2, CARD_Y + 184);

        // Stat bars
        double barX = cx + 8;
        double barW = CARD_W - 16;
        drawStatBar(gc, barX, CARD_Y + 196, barW, "SPD",
                    ch.getTopSpeed() / 340.0, ch.getBodyColor());
        drawStatBar(gc, barX, CARD_Y + 210, barW, "ACC",
                    ch.getAcceleration() / 138.0, ch.getBodyColor());
        drawStatBar(gc, barX, CARD_Y + 224, barW, "HND",
                    ch.getHandling(), ch.getBodyColor());
    }

    private void drawStatBar(GraphicsContext gc, double x, double y, double width,
                              String label, double value, Color barColor) {
        // Label
        gc.setFill(Color.color(1, 1, 1, 0.55));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, x, y + 9);

        // Track
        double bx = x + 28;
        double bw = width - 30;
        gc.setFill(Color.color(0, 0, 0, 0.45));
        gc.fillRoundRect(bx, y, bw, 10, 4, 4);

        // Fill
        gc.setFill(barColor);
        gc.fillRoundRect(bx, y, bw * Math.min(value, 1.0), 10, 4, 4);
    }

    /**
     * Draws a Boohbah character — a round colorful alien with bumps, eyes, and a smile.
     */
    private void drawBoohbah(GraphicsContext gc, double cx, double cy,
                              Character ch, boolean selected) {
        double r = 42;

        // Selection glow
        if (selected) {
            for (double ring = r + 18; ring > r + 2; ring -= 3) {
                double a = (ring - r - 2) / 16.0 * 0.18;
                gc.setFill(Color.color(
                        ch.getBodyColor().getRed(),
                        ch.getBodyColor().getGreen(),
                        ch.getBodyColor().getBlue(), a));
                gc.fillOval(cx - ring, cy - ring, ring * 2, ring * 2);
            }
        }

        // Body shadow
        gc.setFill(Color.color(0, 0, 0, 0.25));
        gc.fillOval(cx - r + 4, cy - r + 4, r * 2, r * 2);

        // Body
        gc.setFill(ch.getBodyColor());
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Body sheen (highlight)
        gc.setFill(Color.color(1, 1, 1, 0.28));
        gc.fillOval(cx - r * 0.52, cy - r * 0.62, r * 0.65, r * 0.45);

        // Head bump (top knob)
        double hr = 15;
        gc.setFill(ch.getBodyColor());
        gc.fillOval(cx - hr, cy - r - hr * 0.6, hr * 2, hr * 1.8);
        gc.setFill(ch.getAccentColor());
        gc.fillOval(cx - hr * 0.55, cy - r - hr * 0.3, hr * 1.1, hr * 1.1);

        // Arms (side bumps)
        gc.setFill(ch.getBodyColor());
        gc.fillOval(cx - r - 10, cy - 14, 20, 25);   // left arm
        gc.fillOval(cx + r  -  8, cy - 14, 20, 25);  // right arm

        // Eyes — whites
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - 17, cy - 14, 12, 15);
        gc.fillOval(cx +  5, cy - 14, 12, 15);

        // Eyes — pupils
        gc.setFill(Color.web("#222222"));
        gc.fillOval(cx - 13, cy - 10, 7, 9);
        gc.fillOval(cx +  6, cy - 10, 7, 9);

        // Eye shine
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - 11, cy - 9,  3, 3);
        gc.fillOval(cx +  8, cy - 9,  3, 3);

        // Smile
        gc.setStroke(Color.web("#333333"));
        gc.setLineWidth(2.2);
        gc.strokeArc(cx - 14, cy + 4, 28, 15, 200, 140, ArcType.OPEN);
    }

    private void drawStartButton(GraphicsContext gc) {
        boolean hovered = false;  // button glow is static for simplicity

        gc.setFill(Color.web("#FFD700"));
        gc.fillRoundRect(BTN_X, BTN_Y, BTN_W, BTN_H, 28, 28);

        gc.setStroke(Color.color(1, 1, 1, 0.6));
        gc.setLineWidth(2);
        gc.strokeRoundRect(BTN_X, BTN_Y, BTN_W, BTN_H, 28, 28);

        gc.setFill(Color.web("#3D1500"));
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(">> START RACE! <<", BTN_X + BTN_W / 2, BTN_Y + 33);
    }

    private void drawSelectedInfo(GraphicsContext gc) {
        Character ch = characters.get(selectedIndex);
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Selected: " + ch.getName() + "  |  Arrow keys to drive  |  ESC to return to menu",
                    W / 2, H - 20);
    }
}
