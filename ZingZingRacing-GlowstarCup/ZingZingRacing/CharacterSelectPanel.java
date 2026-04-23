import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Character selection screen.
 */
public class CharacterSelectPanel extends JPanel {

    private final List<BoohbahCharacter> characters;
    private int selectedIndex = 0;

    private final JPanel mainContainer;
    private final CardLayout cardLayout;
    private final GamePanel gamePanel;
    private CharacterCard[] cards;

    public CharacterSelectPanel(JPanel mainContainer,
                                CardLayout cardLayout,
                                GamePanel gamePanel) {
        this.mainContainer = mainContainer;
        this.cardLayout = cardLayout;
        this.gamePanel = gamePanel;

        characters = new ArrayList<BoohbahCharacter>();
        characters.add(new BoohbahCharacter("Zumbah",
                new Color(138, 43, 226), 5.6, 0.76, 0.82,
                "Steady all-round star",
                BoohbahCharacter.AbilityType.PRISM_SHIELD,
                "Prism Shield",
                "Stable grip and rough-terrain armor.",
                180, 390));
        characters.add(new BoohbahCharacter("ZingZing",
                new Color(255, 215, 0), 7.25, 0.62, 0.56,
                "Straight-line speed demon",
                BoohbahCharacter.AbilityType.COMET_DASH,
                "Comet Dash",
                "Huge straightaway burst.",
                135, 420));
        characters.add(new BoohbahCharacter("Humbah",
                new Color(245, 245, 200), 5.15, 0.97, 0.82,
                "Rocket-start specialist",
                BoohbahCharacter.AbilityType.MOON_LAUNCH,
                "Moon Launch",
                "Rapid speed recovery and launch.",
                155, 360));
        characters.add(new BoohbahCharacter("Jingbah",
                new Color(255, 105, 180), 5.55, 0.72, 1.0,
                "Master of twisty sections",
                BoohbahCharacter.AbilityType.RIBBON_DRIFT,
                "Ribbon Drift",
                "Extra grip through sharp corners.",
                165, 390));
        characters.add(new BoohbahCharacter("Toombah",
                new Color(50, 205, 80), 7.8, 0.59, 0.66,
                "Big-speed bruiser",
                BoohbahCharacter.AbilityType.WILD_CHARGE,
                "Wild Charge",
                "Keeps momentum on rough sections.",
                170, 405));

        buildUI();
    }

    private void buildUI() {
        setBackground(new Color(12, 16, 42));
        setLayout(new BorderLayout(0, 0));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(8, 12, 30));
        titleBar.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        JButton backButton = new JButton("Back");
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(34, 47, 89));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> cardLayout.show(mainContainer, "TITLE"));

        JLabel title = new JLabel("CHOOSE YOUR BOOHBAH", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 34));
        title.setForeground(new Color(255, 224, 120));

        JLabel sub = new JLabel("Three circuits. Highest score wins the Glowstar Cup.", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 15));
        sub.setForeground(new Color(200, 210, 240));

        JPanel titleTextPane = new JPanel();
        titleTextPane.setOpaque(false);
        titleTextPane.setLayout(new BoxLayout(titleTextPane, BoxLayout.Y_AXIS));
        title.setAlignmentX(CENTER_ALIGNMENT);
        sub.setAlignmentX(CENTER_ALIGNMENT);
        titleTextPane.add(title);
        titleTextPane.add(Box.createVerticalStrut(6));
        titleTextPane.add(sub);

        titleBar.add(backButton, BorderLayout.WEST);
        titleBar.add(titleTextPane, BorderLayout.CENTER);
        titleBar.add(Box.createHorizontalStrut(70), BorderLayout.EAST);
        add(titleBar, BorderLayout.NORTH);

        JPanel cardsRow = new JPanel(new GridLayout(1, characters.size(), 20, 0));
        cardsRow.setBackground(new Color(12, 16, 42));
        cardsRow.setBorder(BorderFactory.createEmptyBorder(24, 36, 18, 36));

        cards = new CharacterCard[characters.size()];
        for (int i = 0; i < characters.size(); i++) {
            cards[i] = new CharacterCard(characters.get(i), i);
            cardsRow.add(cards[i]);
        }
        add(cardsRow, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 14));
        bottomBar.setBackground(new Color(8, 12, 30));

        JButton startButton = new JButton("START GLOWSTAR CUP");
        startButton.setFont(new Font("Arial", Font.BOLD, 20));
        startButton.setBackground(new Color(220, 100, 0));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(e -> startGame());

        JLabel hint = new JLabel("Arrows drive. SPACE uses your move.");
        hint.setFont(new Font("Arial", Font.PLAIN, 13));
        hint.setForeground(new Color(160, 170, 205));

        bottomBar.add(startButton);
        bottomBar.add(hint);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void refreshCards() {
        for (CharacterCard card : cards) {
            card.repaint();
        }
    }

    private void startGame() {
        gamePanel.startGame(characters.get(selectedIndex), new ArrayList<BoohbahCharacter>(characters));
        cardLayout.show(mainContainer, "GAME");
        gamePanel.requestFocusInWindow();
    }

    private class CharacterCard extends JPanel {
        private final BoohbahCharacter character;
        private final int index;
        private boolean hovered = false;

        CharacterCard(BoohbahCharacter character, int index) {
            this.character = character;
            this.index = index;
            setOpaque(false);
            setPreferredSize(new Dimension(180, 370));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedIndex = index;
                    refreshCards();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            boolean selected = selectedIndex == index;

            Color bg = selected ? new Color(46, 40, 14)
                    : hovered ? new Color(26, 32, 62)
                    : new Color(18, 22, 48);
            g2d.setColor(bg);
            g2d.fillRoundRect(0, 0, w - 1, getHeight() - 1, 22, 22);

            if (selected) {
                g2d.setColor(new Color(255, 215, 0));
                g2d.setStroke(new BasicStroke(3f));
            } else if (hovered) {
                g2d.setColor(new Color(110, 150, 240));
                g2d.setStroke(new BasicStroke(2f));
            } else {
                g2d.setColor(new Color(55, 65, 102));
                g2d.setStroke(new BasicStroke(1.2f));
            }
            g2d.drawRoundRect(1, 1, w - 3, getHeight() - 3, 22, 22);

            int centerX = w / 2;
            int bodyY = 92;
            int bodyRadius = 44;

            if (selected) {
                Color accent = character.getAbilityAccent();
                g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
                g2d.fillOval(centerX - bodyRadius - 14, bodyY - bodyRadius - 14,
                             (bodyRadius + 14) * 2, (bodyRadius + 14) * 2);
            }

            g2d.setColor(character.getColor());
            g2d.fillOval(centerX - bodyRadius, bodyY - bodyRadius, bodyRadius * 2, bodyRadius * 2);
            g2d.setColor(new Color(255, 255, 255, 75));
            g2d.fillOval(centerX - bodyRadius + 8, bodyY - bodyRadius + 8, bodyRadius - 6, bodyRadius - 6);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(centerX - bodyRadius, bodyY - bodyRadius, bodyRadius * 2, bodyRadius * 2);

            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX - 18, bodyY - 14, 13, 17);
            g2d.fillOval(centerX + 5, bodyY - 14, 13, 17);
            g2d.setColor(new Color(20, 20, 20));
            g2d.fillOval(centerX - 15, bodyY - 10, 7, 10);
            g2d.fillOval(centerX + 8, bodyY - 10, 7, 10);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centerX - 12, bodyY - 7, 2, 2);
            g2d.fillOval(centerX + 11, bodyY - 7, 2, 2);

            g2d.setColor(new Color(255, 130, 130, 90));
            g2d.fillOval(centerX - 30, bodyY + 4, 16, 8);
            g2d.fillOval(centerX + 14, bodyY + 4, 16, 8);

            g2d.setColor(new Color(40, 20, 10));
            g2d.setStroke(new BasicStroke(2.2f));
            g2d.drawArc(centerX - 12, bodyY + 8, 24, 14, 0, -180);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics metrics = g2d.getFontMetrics();
            g2d.drawString(character.getName(),
                    centerX - metrics.stringWidth(character.getName()) / 2,
                    bodyY + bodyRadius + 24);

            g2d.setColor(new Color(180, 190, 220));
            g2d.setFont(new Font("Arial", Font.ITALIC, 11));
            metrics = g2d.getFontMetrics();
            g2d.drawString(character.getDescription(),
                    centerX - metrics.stringWidth(character.getDescription()) / 2,
                    bodyY + bodyRadius + 44);

            int statY = bodyY + bodyRadius + 60;
            drawStat(g2d, "SPD", character.getTopSpeed() / 8.0, centerX, statY, new Color(255, 100, 100));
            drawStat(g2d, "ACC", character.getAcceleration(), centerX, statY + 28, new Color(110, 230, 120));
            drawStat(g2d, "HDL", character.getHandling(), centerX, statY + 56, new Color(110, 170, 255));

            int chipY = statY + 88;
            g2d.setColor(new Color(character.getAbilityAccent().getRed(),
                                   character.getAbilityAccent().getGreen(),
                                   character.getAbilityAccent().getBlue(), 55));
            g2d.fillRoundRect(16, chipY, w - 32, 28, 14, 14);
            g2d.setColor(character.getAbilityAccent());
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(16, chipY, w - 32, 28, 14, 14);

            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            metrics = g2d.getFontMetrics();
            g2d.drawString(character.getAbilityName(),
                    centerX - metrics.stringWidth(character.getAbilityName()) / 2,
                    chipY + 19);

            g2d.setColor(new Color(205, 215, 240));
            g2d.setFont(new Font("Arial", Font.PLAIN, 11));
            drawWrappedCentered(g2d, character.getAbilityDescription(), centerX, chipY + 47, w - 30, 14);

            g2d.dispose();
        }

        private void drawStat(Graphics2D g2d, String label, double value,
                              int centerX, int y, Color barColor) {
            int barWidth = 118;
            int barHeight = 10;
            int barX = centerX - barWidth / 2;

            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.setColor(new Color(170, 180, 210));
            g2d.drawString(label, barX, y + barHeight);

            int labelWidth = 28;
            g2d.setColor(new Color(38, 42, 65));
            g2d.fillRoundRect(barX + labelWidth, y, barWidth - labelWidth, barHeight, 4, 4);

            int fill = (int) ((barWidth - labelWidth) * Math.max(0.0, Math.min(value, 1.0)));
            g2d.setColor(barColor);
            g2d.fillRoundRect(barX + labelWidth, y, fill, barHeight, 4, 4);
        }

        private void drawWrappedCentered(Graphics2D g2d, String text, int centerX, int startY, int maxWidth, int lineGap) {
            String[] words = text.split(" ");
            String line = "";
            int y = startY;
            for (String word : words) {
                String nextLine = line.isEmpty() ? word : line + " " + word;
                if (g2d.getFontMetrics().stringWidth(nextLine) > maxWidth && !line.isEmpty()) {
                    drawCenteredLine(g2d, line, centerX, y);
                    line = word;
                    y += lineGap;
                } else {
                    line = nextLine;
                }
            }
            if (!line.isEmpty()) {
                drawCenteredLine(g2d, line, centerX, y);
            }
        }

        private void drawCenteredLine(Graphics2D g2d, String text, int centerX, int y) {
            FontMetrics metrics = g2d.getFontMetrics();
            g2d.drawString(text, centerX - metrics.stringWidth(text) / 2, y);
        }
    }
}
