package zingzingzingbah.screen;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import zingzingzingbah.GameApp;
import zingzingzingbah.engine.RacingEngine;
import zingzingzingbah.model.Character;
import zingzingzingbah.model.Kart;
import zingzingzingbah.track.Track;

import java.util.List;

/**
 * The main race screen.
 * Contains the Canvas, game loop (AnimationTimer), HUD, countdown,
 * and post-race results overlay.
 *
 * Java 2 concepts demonstrated:
 *   - List<Kart> iteration from RacingEngine
 *   - AnimationTimer (observer/event pattern)
 *   - enum-like state machine via raceState flag
 */
public class RaceScreen extends Pane {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------
    private static final double W = 900;
    private static final double H = 600;
    private static final double COUNTDOWN_DURATION = 4.0;  // 3..2..1..GO
    private static final int    TOTAL_LAPS         = RacingEngine.TOTAL_LAPS;

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------
    private final GameApp       gameApp;
    private final Track         track;
    private final RacingEngine  engine;
    private final Kart          playerKart;

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------
    private final Canvas         canvas;
    private final GraphicsContext gc;

    // -----------------------------------------------------------------------
    // Game loop state
    // -----------------------------------------------------------------------
    private AnimationTimer timer;
    private long           lastTime     = 0;
    private double         countdown    = COUNTDOWN_DURATION;
    private boolean        raceStarted  = false;
    private boolean        showResults  = false;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public RaceScreen(GameApp gameApp, Character selectedCharacter) {
        this.gameApp = gameApp;
        this.track   = new Track();
        this.engine  = new RacingEngine(track, selectedCharacter,
                                         Character.getAllCharacters());
        this.playerKart = engine.getPlayerKart();

        canvas = new Canvas(W, H);
        gc     = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------
    public void startGame() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) { lastTime = now; return; }

                double delta = (now - lastTime) / 1_000_000_000.0;
                delta = Math.min(delta, 0.05);  // cap to 50 ms — no spiral of death
                lastTime = now;

                gameLoop(delta);
            }
        };
        timer.start();
    }

    // -----------------------------------------------------------------------
    // Game loop
    // -----------------------------------------------------------------------
    private void gameLoop(double delta) {
        if (!showResults) {
            if (!raceStarted) {
                // Countdown phase
                countdown -= delta;
                if (countdown <= 0) {
                    raceStarted = true;
                }
            } else {
                // Active race
                engine.update(delta);
                if (engine.isRaceFinished()) {
                    showResults = true;
                    timer.stop();
                }
            }
        }
        render();
    }

    // -----------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------
    private void render() {
        // Background — outer grass
        LinearGradient grass = new LinearGradient(
                0, 0, 0, H, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#2D7A3A")),
                new Stop(1.0, Color.web("#1E5C2A")));
        gc.setFill(grass);
        gc.fillRect(0, 0, W, H);

        // Draw track
        track.draw(gc);

        // Draw all karts
        List<Kart> karts = engine.getKarts();
        for (Kart k : karts) {
            k.draw(gc);
        }

        // HUD
        drawHUD(gc);

        // Countdown overlay
        if (!raceStarted) {
            drawCountdown(gc);
        }

        // Results overlay
        if (showResults) {
            drawResults(gc);
        }
    }

    // -----------------------------------------------------------------------
    // HUD
    // -----------------------------------------------------------------------
    private void drawHUD(GraphicsContext gc) {
        // Top bar background
        gc.setFill(Color.color(0, 0, 0, 0.55));
        gc.fillRoundRect(10, 10, 220, 44, 10, 10);
        gc.fillRoundRect(W - 230, 10, 220, 44, 10, 10);
        gc.fillRoundRect(W / 2 - 80, 10, 160, 44, 10, 10);

        gc.setTextAlign(TextAlignment.LEFT);

        // Lap counter (top-left)
        gc.setFill(Color.color(1, 1, 1, 0.6));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.fillText("LAP", 22, 27);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 22));
        gc.fillText(Math.min(playerKart.getLap() + 1, TOTAL_LAPS) + " / " + TOTAL_LAPS, 22, 48);

        // Speed (top-right)
        gc.setFill(Color.color(1, 1, 1, 0.6));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("SPEED", W - 22, 27);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 22));
        gc.fillText((int) playerKart.getSpeed() + " px/s", W - 22, 48);

        // Position (top-center)
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.color(1, 1, 1, 0.6));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.fillText("POS", W / 2, 27);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 22));
        gc.fillText(playerKart.getRacePosition() + " / " + engine.getKarts().size(), W / 2, 48);

        // Event log (bottom-right)
        String[] log = engine.getRecentLog();
        gc.setFill(Color.color(0, 0, 0, 0.45));
        gc.fillRoundRect(W - 240, H - 130, 230, 120, 10, 10);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        for (int i = 0; i < log.length; i++) {
            double alpha = 1.0 - i * 0.22;
            gc.setFill(Color.color(1, 1, 1, alpha));
            gc.fillText(log[i], W - 228, H - 110 + i * 18);
        }

        // Off-track warning
        if (!track.isOnTrack(playerKart.getX(), playerKart.getY())) {
            gc.setFill(Color.color(1, 0.3, 0, 0.80));
            gc.setFont(Font.font("Arial", FontWeight.BLACK, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("OFF TRACK — RETURN TO TRACK!", W / 2, H - 20);
        }

        // Character name bottom-left
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRoundRect(10, H - 40, 200, 30, 8, 8);
        gc.setFill(playerKart.getCharacter().getBodyColor());
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(playerKart.getCharacter().getName(), 22, H - 20);

        // Control hint (small, bottom center)
        gc.setFill(Color.color(1, 1, 1, 0.35));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("Arrow Keys / WASD to drive  |  ESC = Menu", W / 2, H - 8);
    }

    // -----------------------------------------------------------------------
    // Countdown overlay
    // -----------------------------------------------------------------------
    private void drawCountdown(GraphicsContext gc) {
        String text;
        Color  color;

        if (countdown > 3.0) {
            text  = "3";
            color = Color.web("#FF4444");
        } else if (countdown > 2.0) {
            text  = "2";
            color = Color.web("#FF9900");
        } else if (countdown > 1.0) {
            text  = "1";
            color = Color.web("#FFDD00");
        } else {
            text  = "GO!";
            color = Color.web("#44FF44");
        }

        double scale = 1.0 + Math.sin((COUNTDOWN_DURATION - countdown) * 6) * 0.08;
        gc.save();
        gc.translate(W / 2, H / 2 - 20);
        gc.scale(scale, scale);

        // Shadow
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 108));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(text, 3, 3);

        // Main
        gc.setFill(color);
        gc.fillText(text, 0, 0);

        gc.restore();
    }

    // -----------------------------------------------------------------------
    // Results overlay
    // -----------------------------------------------------------------------
    private void drawResults(GraphicsContext gc) {
        // Dark overlay
        gc.setFill(Color.color(0, 0, 0, 0.75));
        gc.fillRect(0, 0, W, H);

        // Panel
        gc.setFill(Color.color(0.05, 0.05, 0.2, 0.95));
        gc.fillRoundRect(200, 120, 500, 360, 24, 24);
        gc.setStroke(Color.web("#FFD700"));
        gc.setLineWidth(3);
        gc.strokeRoundRect(200, 120, 500, 360, 24, 24);

        int pos = engine.getFinishPosition();
        String headline = pos == 1 ? "YOU WON!" : "RACE FINISHED";
        Color headColor  = pos == 1 ? Color.web("#FFD700") : Color.WHITE;

        gc.setFont(Font.font("Arial", FontWeight.BLACK, 48));
        gc.setFill(headColor);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(headline, W / 2, 200);

        gc.setFill(Color.color(1, 1, 1, 0.8));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.fillText("You finished in place  " + pos + " / " + engine.getKarts().size(), W / 2, 248);

        // Final positions table
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        gc.setFill(Color.color(1, 1, 1, 0.55));
        gc.fillText("FINAL STANDINGS", W / 2, 286);

        List<Kart> karts = engine.getKarts();
        for (int i = 0; i < karts.size(); i++) {
            Kart k  = karts.get(i);
            boolean isPlayerKart = k.isPlayer();
            gc.setFill(isPlayerKart
                    ? k.getCharacter().getBodyColor()
                    : Color.color(1, 1, 1, 0.65));
            gc.setFont(Font.font("Arial",
                    isPlayerKart ? FontWeight.BLACK : FontWeight.NORMAL, 14));
            gc.fillText(k.getRacePosition() + ".  " + k.getCharacter().getName()
                    + (isPlayerKart ? "  ← YOU" : ""),
                    W / 2, 308 + i * 20);
        }

        // Buttons
        drawResultButton(gc, 260, 440, 160, 40, Color.web("#FFD700"),
                         Color.web("#3D1500"), "PLAY AGAIN");
        drawResultButton(gc, 480, 440, 160, 40, Color.web("#555577"),
                         Color.WHITE, "MAIN MENU");

        // Mouse click note
        gc.setFill(Color.color(1, 1, 1, 0.4));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.fillText("Click PLAY AGAIN or MAIN MENU", W / 2, H - 30);

        // Register button clicks once results are shown
        canvas.setOnMouseClicked(e -> {
            if (e.getX() >= 260 && e.getX() <= 420 && e.getY() >= 440 && e.getY() <= 480) {
                // Play Again — same character
                if (timer != null) timer.stop();
                gameApp.startRace(playerKart.getCharacter());
            } else if (e.getX() >= 480 && e.getX() <= 640 && e.getY() >= 440 && e.getY() <= 480) {
                // Main Menu
                if (timer != null) timer.stop();
                gameApp.showCharacterSelect();
            }
        });
    }

    private void drawResultButton(GraphicsContext gc, double x, double y, double w, double h,
                                   Color bg, Color fg, String label) {
        gc.setFill(bg);
        gc.fillRoundRect(x, y, w, h, 20, 20);
        gc.setFill(fg);
        gc.setFont(Font.font("Arial", FontWeight.BLACK, 15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + w / 2, y + h / 2 + 5);
    }

    // -----------------------------------------------------------------------
    // Key input handlers (called from GameApp via Scene event handlers)
    // -----------------------------------------------------------------------
    public void handleKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case UP,    W -> playerKart.setAccelerating(true);
            case DOWN,  S -> playerKart.setBraking(true);
            case LEFT,  A -> playerKart.setTurningLeft(true);
            case RIGHT, D -> playerKart.setTurningRight(true);
            case ESCAPE    -> {
                if (timer != null) timer.stop();
                gameApp.showCharacterSelect();
            }
            default -> {}
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case UP,    W -> playerKart.setAccelerating(false);
            case DOWN,  S -> playerKart.setBraking(false);
            case LEFT,  A -> playerKart.setTurningLeft(false);
            case RIGHT, D -> playerKart.setTurningRight(false);
            default -> {}
        }
    }
}
