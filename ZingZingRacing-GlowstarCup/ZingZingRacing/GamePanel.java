import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game rendering and input panel.
 */
public class GamePanel extends JPanel implements KeyListener {

    private static final int COUNTDOWN_FRAMES = 195;

    private RacingEngine engine;
    private Racer playerRacer;
    private CupSeries cupSeries;
    private List<BoohbahCharacter> roster;
    private List<Racer> frozenResults;

    private int countdownFrames = 0;
    private boolean raceOver = false;
    private boolean raceScored = false;

    private boolean keyUp;
    private boolean keyDown;
    private boolean keyLeft;
    private boolean keyRight;
    private boolean keyAbility;

    private int eventBannerFrames = 0;
    private String eventBannerText = "";
    private Color eventBannerColor = new Color(255, 215, 0);

    private final Timer gameTimer;

    private final Font fontHudSmall = new Font("Arial", Font.BOLD, 15);
    private final Font fontHudLarge = new Font("Arial", Font.BOLD, 24);
    private final Font fontCountdown = new Font("Arial", Font.BOLD, 110);
    private final Font fontResult = new Font("Arial", Font.BOLD, 30);

    private JPanel mainContainer;
    private CardLayout cardLayout;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        gameTimer = new Timer(16, e -> tick());
    }

    public void startGame(BoohbahCharacter playerChar, List<BoohbahCharacter> allChars) {
        roster = new ArrayList<BoohbahCharacter>(allChars);
        cupSeries = new CupSeries(roster, playerChar);
        startCurrentRace();
    }

    private void startCurrentRace() {
        gameTimer.stop();

        Track track = new Track(cupSeries.getCurrentCircuit());
        engine = new RacingEngine(track);

        double startAngle = track.getStartAngle();

        double[] playerStart = track.getStartPosition(0);
        playerRacer = new Racer(cupSeries.getPlayerCharacter(), playerStart[0], playerStart[1], startAngle, true);
        engine.addRacer(playerRacer);

        int gridSlot = 1;
        for (BoohbahCharacter character : roster) {
            if (character == playerRacer.getCharacter() || gridSlot >= 5) {
                continue;
            }
            double[] start = track.getStartPosition(gridSlot);
            Racer ai = new Racer(character, start[0], start[1], startAngle, false);
            engine.addRacer(ai);
            gridSlot++;
        }

        countdownFrames = 0;
        raceOver = false;
        raceScored = false;
        frozenResults = null;
        eventBannerFrames = 0;
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
        keyAbility = false;

        requestFocusInWindow();
        gameTimer.start();
    }

    private void tick() {
        if (engine == null) {
            repaint();
            return;
        }

        if (countdownFrames < COUNTDOWN_FRAMES) {
            countdownFrames++;
        } else if (!raceOver) {
            String previousEvent = engine.getLastEvent();
            engine.update(keyUp, keyDown, keyLeft, keyRight, keyAbility);
            keyAbility = false;

            String newEvent = engine.getLastEvent();
            if (!newEvent.equals(previousEvent)) {
                handleEventBanner(newEvent);
            }

            if (engine.isRaceOver()) {
                finishRace();
            }
        }

        if (eventBannerFrames > 0) {
            eventBannerFrames--;
        }

        repaint();
    }

    private void finishRace() {
        if (raceScored || engine == null || cupSeries == null) {
            return;
        }

        frozenResults = engine.getRacePositions();
        cupSeries.recordRace(frozenResults);
        raceScored = true;
        raceOver = true;
    }

    private void advanceCup() {
        if (cupSeries == null) {
            returnToSelect();
            return;
        }

        if (cupSeries.hasNextCircuit()) {
            cupSeries.advanceToNextCircuit();
            startCurrentRace();
        } else {
            returnToSelect();
        }
    }

    private void returnToSelect() {
        gameTimer.stop();
        engine = null;
        playerRacer = null;
        cupSeries = null;
        frozenResults = null;
        roster = null;
        raceOver = false;
        raceScored = false;
        countdownFrames = 0;
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
        keyAbility = false;

        if (mainContainer != null) {
            cardLayout.show(mainContainer, "SELECT");
        }
    }

    private void handleEventBanner(String event) {
        if (event == null || event.isEmpty()) {
            return;
        }

        if (event.startsWith("LAP:")) {
            eventBannerText = event.substring(5).trim();
            eventBannerColor = new Color(255, 215, 0);
            eventBannerFrames = 130;
        } else if (event.startsWith("ABILITY:")) {
            eventBannerText = event.substring(9).trim() + "!";
            eventBannerColor = new Color(110, 230, 255);
            eventBannerFrames = 95;
        } else if (event.startsWith("FINISH:")) {
            eventBannerText = "Finish!";
            eventBannerColor = new Color(255, 240, 160);
            eventBannerFrames = 100;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackdropFill(g2d);

        if (engine == null) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(fontHudLarge);
            drawCentered(g2d, "Select a racer to begin the cup.", getHeight() / 2);
            g2d.dispose();
            return;
        }

        Track track = engine.getTrack();
        double scale = Math.min(getWidth() / (double) track.getWorldWidth(),
                                getHeight() / (double) track.getWorldHeight());
        double offsetX = (getWidth() - track.getWorldWidth() * scale) / 2.0;
        double offsetY = (getHeight() - track.getWorldHeight() * scale) / 2.0;

        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        track.draw(g2d);
        for (Racer racer : engine.getRacers()) {
            racer.draw(g2d);
        }
        g2d.setTransform(oldTransform);

        drawHud(g2d);

        if (countdownFrames < COUNTDOWN_FRAMES) {
            drawCountdown(g2d);
        }
        if (eventBannerFrames > 0) {
            drawEventBanner(g2d);
        }
        if (raceOver) {
            drawResults(g2d);
        }

        g2d.dispose();
    }

    private void drawBackdropFill(Graphics2D g2d) {
        g2d.setColor(new Color(5, 10, 25));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawHud(Graphics2D g2d) {
        if (playerRacer == null || cupSeries == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRoundRect(18, 16, 260, 104, 18, 18);

        g2d.setColor(new Color(255, 215, 0));
        g2d.setFont(fontHudLarge);
        g2d.drawString("LAP " + playerRacer.getLap() + " / " + Racer.TOTAL_LAPS, 32, 48);

        List<Racer> positions = engine.getRacePositions();
        int position = positions.indexOf(playerRacer) + 1;
        g2d.setColor(Color.WHITE);
        g2d.setFont(fontHudSmall);
        g2d.drawString("POSITION  " + position + " / " + positions.size(), 32, 74);
        g2d.drawString("SPEED  " + Math.max(0, (int) (playerRacer.getSpeed() * 33)) + " km/h", 32, 97);

        drawCupHeader(g2d, width / 2 - 170, 16, 340, 92);
        drawAbilityHud(g2d, width - 324, 16, 306, 104);

        g2d.setColor(new Color(230, 230, 240, 170));
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Arrow keys drive  |  SPACE move  |  ESC quit cup", 18, height - 16);
    }

    private void drawCupHeader(Graphics2D g2d, int x, int y, int w, int h) {
        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRoundRect(x, y, w, h, 18, 18);

        Track track = engine.getTrack();
        g2d.setColor(new Color(255, 224, 120));
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        drawCenteredAt(g2d, cupSeries.getCupName(), x + w / 2, y + 31);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        drawCenteredAt(g2d,
                "Round " + cupSeries.getCurrentCircuitNumber() + " / " + cupSeries.getTotalCircuits()
                        + "  -  " + track.getName(),
                x + w / 2, y + 57);

        g2d.setColor(new Color(190, 205, 230));
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        drawCenteredAt(g2d, track.getSubtitle(), x + w / 2, y + 79);
    }

    private void drawAbilityHud(Graphics2D g2d, int x, int y, int w, int h) {
        BoohbahCharacter character = playerRacer.getCharacter();
        g2d.setColor(new Color(0, 0, 0, 145));
        g2d.fillRoundRect(x, y, w, h, 18, 18);

        g2d.setColor(character.getAbilityAccent());
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString(character.getAbilityName(), x + 16, y + 34);

        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        g2d.setColor(new Color(220, 235, 245));
        g2d.drawString(character.getAbilityDescription(), x + 16, y + 58);

        int barX = x + 16;
        int barY = y + 72;
        int barW = w - 32;
        int barH = 14;

        g2d.setColor(new Color(35, 42, 56));
        g2d.fillRoundRect(barX, barY, barW, barH, 7, 7);

        String stateText;
        if (playerRacer.isAbilityActive()) {
            double ratio = playerRacer.getAbilityTimerFrames() /
                           (double) character.getAbilityDurationFrames();
            int fill = (int) Math.round(barW * Math.max(0.0, Math.min(ratio, 1.0)));
            g2d.setColor(character.getAbilityAccent());
            g2d.fillRoundRect(barX, barY, fill, barH, 7, 7);
            stateText = "ACTIVE";
        } else if (playerRacer.getAbilityCooldownFrames() > 0) {
            double ratio = 1.0 - (playerRacer.getAbilityCooldownFrames() /
                                  (double) character.getAbilityCooldownFrames());
            int fill = (int) Math.round(barW * Math.max(0.0, Math.min(ratio, 1.0)));
            g2d.setColor(new Color(130, 150, 175));
            g2d.fillRoundRect(barX, barY, fill, barH, 7, 7);
            stateText = String.format("CHARGING %1.1fs", playerRacer.getAbilityCooldownFrames() / 60.0);
        } else {
            g2d.setColor(new Color(100, 255, 180));
            g2d.fillRoundRect(barX, barY, barW, barH, 7, 7);
            stateText = "READY";
        }

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(new Color(18, 24, 35));
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(stateText, barX + (barW - metrics.stringWidth(stateText)) / 2, barY + 11);
    }

    private void drawCountdown(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();
        int value = 3 - countdownFrames / 60;
        String label = value > 0 ? String.valueOf(value) : "GO!";
        Color color = value > 0 ? new Color(255, 120, 120) : new Color(110, 255, 140);

        float t = (countdownFrames % 60) / 60f;
        int alpha = (int) (255 * (1f - t * 0.4f));

        g2d.setComposite(AlphaComposite.SrcOver);
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.setFont(fontCountdown);
        FontMetrics metrics = g2d.getFontMetrics();
        int tx = width / 2 - metrics.stringWidth(label) / 2;
        int ty = height / 2 + metrics.getAscent() / 2 - 18;
        g2d.drawString(label, tx + 4, ty + 4);

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g2d.drawString(label, tx, ty);
    }

    private void drawEventBanner(Graphics2D g2d) {
        int width = getWidth();
        int bannerWidth = Math.max(260, g2d.getFontMetrics(new Font("Arial", Font.BOLD, 28))
                .stringWidth(eventBannerText) + 64);
        int x = width / 2 - bannerWidth / 2;
        int y = 116;

        g2d.setColor(new Color(8, 10, 22, 180));
        g2d.fillRoundRect(x, y, bannerWidth, 62, 18, 18);
        g2d.setColor(eventBannerColor);
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawRoundRect(x, y, bannerWidth, 62, 18, 18);

        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        drawCenteredAt(g2d, eventBannerText, width / 2, y + 40);
    }

    private void drawResults(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(0, 0, 0, 205));
        g2d.fillRect(0, 0, width, height);

        int boxWidth = 980;
        int boxHeight = 470;
        int boxX = width / 2 - boxWidth / 2;
        int boxY = height / 2 - boxHeight / 2;

        g2d.setColor(new Color(20, 28, 60, 245));
        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 22, 22);
        g2d.setColor(new Color(255, 215, 0));
        g2d.setStroke(new BasicStroke(3f));
        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 22, 22);

        Track track = engine.getTrack();
        g2d.setFont(fontResult);
        g2d.setColor(new Color(255, 225, 120));
        drawCenteredAt(g2d, track.getName() + " Results", width / 2, boxY + 42);

        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        g2d.setColor(new Color(190, 205, 230));
        drawCenteredAt(g2d,
                cupSeries.getCupName() + "  |  Round " + cupSeries.getCurrentCircuitNumber()
                        + " of " + cupSeries.getTotalCircuits(),
                width / 2, boxY + 66);

        drawRaceResultsColumn(g2d, boxX + 28, boxY + 95, 390, 282);
        drawCupStandingsColumn(g2d, boxX + 450, boxY + 95, 500, 282);
        drawResultsFooter(g2d, boxX, boxY, boxWidth, boxHeight);
    }

    private void drawRaceResultsColumn(Graphics2D g2d, int x, int y, int w, int h) {
        List<Racer> results = frozenResults == null ? engine.getRacePositions() : frozenResults;

        g2d.setColor(new Color(10, 14, 30, 180));
        g2d.fillRoundRect(x, y, w, h, 18, 18);
        g2d.setColor(new Color(115, 160, 255, 150));
        g2d.drawRoundRect(x, y, w, h, 18, 18);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Race Finish", x + 18, y + 28);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        for (int i = 0; i < results.size(); i++) {
            Racer racer = results.get(i);
            int rowY = y + 68 + i * 42;
            boolean player = racer.isPlayer();

            if (player) {
                g2d.setColor(new Color(255, 215, 0, 40));
                g2d.fillRoundRect(x + 12, rowY - 22, w - 24, 30, 10, 10);
            }

            g2d.setColor(player ? new Color(255, 220, 110) : Color.WHITE);
            g2d.drawString((i + 1) + ".", x + 18, rowY);
            g2d.setColor(racer.getCharacter().getColor());
            g2d.drawString(racer.getCharacter().getName() + (player ? "  (YOU)" : ""), x + 68, rowY);

            g2d.setColor(new Color(205, 215, 235));
            String points = "+" + cupSeries.getPointAward(i);
            g2d.drawString(points, x + w - 62, rowY);
        }
    }

    private void drawCupStandingsColumn(Graphics2D g2d, int x, int y, int w, int h) {
        List<BoohbahCharacter> standings = cupSeries.getStandings();

        g2d.setColor(new Color(10, 14, 30, 180));
        g2d.fillRoundRect(x, y, w, h, 18, 18);
        g2d.setColor(new Color(125, 200, 150, 150));
        g2d.drawRoundRect(x, y, w, h, 18, 18);

        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Cup Standings", x + 18, y + 28);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        for (int i = 0; i < standings.size(); i++) {
            BoohbahCharacter character = standings.get(i);
            int rowY = y + 68 + i * 42;
            boolean player = character == cupSeries.getPlayerCharacter();

            if (player) {
                g2d.setColor(new Color(255, 215, 0, 40));
                g2d.fillRoundRect(x + 12, rowY - 22, w - 24, 30, 10, 10);
            }

            g2d.setColor(player ? new Color(255, 220, 110) : Color.WHITE);
            g2d.drawString((i + 1) + ".", x + 18, rowY);
            g2d.setColor(character.getColor());
            g2d.drawString(character.getName() + (player ? "  (YOU)" : ""), x + 68, rowY);

            g2d.setColor(new Color(205, 215, 235));
            g2d.drawString(cupSeries.getPoints(character) + " pts", x + w - 88, rowY);
        }
    }

    private void drawResultsFooter(Graphics2D g2d, int boxX, int boxY, int boxWidth, int boxHeight) {
        BoohbahCharacter playerCharacter = cupSeries.getPlayerCharacter();
        int playerStanding = cupSeries.getStandingPosition(playerCharacter);

        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(255, 225, 120));
        String summary;
        if (cupSeries.isComplete()) {
            if (playerStanding == 1) {
                summary = "You take the " + cupSeries.getCupName() + "!";
            } else {
                summary = ordinal(playerStanding) + " overall in the " + cupSeries.getCupName();
            }
        } else {
            summary = "Cup standing: " + ordinal(playerStanding) + "  |  "
                    + cupSeries.getPoints(playerCharacter) + " pts";
        }
        drawCenteredAt(g2d, summary, boxX + boxWidth / 2, boxY + boxHeight - 62);

        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(190, 200, 225));
        String prompt = cupSeries.isComplete()
                ? "Press ENTER to return to racer select"
                : "Press ENTER for " + cupSeries.getNextCircuitName();
        drawCenteredAt(g2d, prompt, boxX + boxWidth / 2, boxY + boxHeight - 30);
    }

    private String ordinal(int value) {
        if (value % 100 >= 11 && value % 100 <= 13) {
            return value + "TH";
        }
        switch (value % 10) {
            case 1:
                return value + "ST";
            case 2:
                return value + "ND";
            case 3:
                return value + "RD";
            default:
                return value + "TH";
        }
    }

    private void drawCentered(Graphics2D g2d, String text, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(text, getWidth() / 2 - metrics.stringWidth(text) / 2, y);
    }

    private void drawCenteredAt(Graphics2D g2d, String text, int centerX, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        g2d.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                keyUp = true;
                break;
            case KeyEvent.VK_DOWN:
                keyDown = true;
                break;
            case KeyEvent.VK_LEFT:
                keyLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
                keyRight = true;
                break;
            case KeyEvent.VK_SPACE:
                keyAbility = true;
                break;
            case KeyEvent.VK_ENTER:
                if (raceOver) {
                    advanceCup();
                }
                break;
            case KeyEvent.VK_ESCAPE:
                returnToSelect();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                keyUp = false;
                break;
            case KeyEvent.VK_DOWN:
                keyDown = false;
                break;
            case KeyEvent.VK_LEFT:
                keyLeft = false;
                break;
            case KeyEvent.VK_RIGHT:
                keyRight = false;
                break;
            case KeyEvent.VK_SPACE:
                keyAbility = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void setNavigation(JPanel mainContainer, CardLayout cardLayout) {
        this.mainContainer = mainContainer;
        this.cardLayout = cardLayout;
    }
}
