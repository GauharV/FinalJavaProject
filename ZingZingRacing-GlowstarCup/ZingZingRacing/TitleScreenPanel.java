import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;

/**
 * Landing screen shown before character select.
 */
public class TitleScreenPanel extends JPanel {

    private final Runnable onStart;

    public TitleScreenPanel(Runnable onStart) {
        this.onStart = onStart;

        setLayout(new GridBagLayout());
        setOpaque(true);
        setFocusable(true);

        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(650, 390));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        JLabel title = new JLabel("ZING ZING ZINGBAH RACING", JLabel.CENTER);
        title.setForeground(new Color(255, 225, 130));
        title.setFont(new Font("Arial", Font.BOLD, 42));

        JLabel subtitle = new JLabel("Glowstar Cup", JLabel.CENTER);
        subtitle.setForeground(new Color(140, 245, 255));
        subtitle.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel body = new JLabel(
                "<html><div style='text-align:center;'>Three circuits. Five racers. One cup.<br/>Score the most points across the full weekend to lift the Glowstar Cup.</div></html>",
                JLabel.CENTER);
        body.setForeground(new Color(225, 235, 255));
        body.setFont(new Font("Arial", Font.PLAIN, 18));

        JLabel schedule = new JLabel("Sky Ribbon  |  Moon Garden  |  Sunset Harbor", JLabel.CENTER);
        schedule.setForeground(new Color(255, 226, 165));
        schedule.setFont(new Font("Arial", Font.BOLD, 16));

        JButton startButton = new JButton("START GLOWSTAR CUP");
        startButton.setFont(new Font("Arial", Font.BOLD, 22));
        startButton.setBackground(new Color(244, 104, 20));
        startButton.setForeground(Color.WHITE);
        startButton.setBorderPainted(false);
        startButton.setFocusPainted(false);
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(e -> start());

        JLabel hint = new JLabel("Press ENTER or click to continue", JLabel.CENTER);
        hint.setForeground(new Color(180, 190, 225));
        hint.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel text = new JPanel(new GridBagLayout());
        text.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        text.add(title, gbc);
        gbc.gridy = 1;
        text.add(subtitle, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(18, 12, 10, 12);
        text.add(body, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 12, 18, 12);
        text.add(schedule, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 0, 0, 0);
        text.add(startButton, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 0, 0);
        text.add(hint, gbc);

        panel.add(text, BorderLayout.CENTER);
        add(panel);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "start");
        getActionMap().put("start", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                start();
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "start");
    }

    private void start() {
        if (onStart != null) {
            onStart.run();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, new Color(9, 14, 40),
                                                     0, getHeight(), new Color(15, 96, 118));
        g2d.setPaint(background);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        for (int i = -140; i < getWidth(); i += 170) {
            g2d.setColor(new Color(255, 255, 255, 18));
            g2d.fillRoundRect(i, 72 + (i / 8) % 110, 240, 30, 30, 30);
        }

        drawTrackRibbon(g2d);
        drawBoohbahOrb(g2d, getWidth() * 0.18, getHeight() * 0.68, new Color(255, 215, 0), 58);
        drawBoohbahOrb(g2d, getWidth() * 0.78, getHeight() * 0.28, new Color(255, 105, 180), 50);
        drawBoohbahOrb(g2d, getWidth() * 0.86, getHeight() * 0.76, new Color(50, 205, 80), 62);

        g2d.dispose();
    }

    private void drawTrackRibbon(Graphics2D g2d) {
        GeneralPath ribbon = new GeneralPath();
        ribbon.moveTo(110, getHeight() - 180);
        ribbon.curveTo(getWidth() * 0.18, getHeight() * 0.54,
                       getWidth() * 0.33, getHeight() * 0.24,
                       getWidth() * 0.54, getHeight() * 0.34);
        ribbon.curveTo(getWidth() * 0.78, getHeight() * 0.46,
                       getWidth() * 0.85, getHeight() * 0.84,
                       getWidth() - 130, getHeight() - 150);

        g2d.setColor(new Color(255, 96, 120, 55));
        g2d.setStroke(new BasicStroke(122f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(ribbon);

        g2d.setColor(new Color(220, 230, 255));
        g2d.setStroke(new BasicStroke(104f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(ribbon);

        g2d.setColor(new Color(44, 48, 62));
        g2d.setStroke(new BasicStroke(92f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(ribbon);

        g2d.setColor(new Color(255, 220, 90, 165));
        float[] dash = {28f, 22f};
        g2d.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
        g2d.draw(ribbon);
    }

    private void drawBoohbahOrb(Graphics2D g2d, double x, double y, Color color, int radius) {
        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 58));
        g2d.fillOval((int) x - radius - 14, (int) y - radius - 14, (radius + 14) * 2, (radius + 14) * 2);

        g2d.setColor(color);
        g2d.fillOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        g2d.setColor(new Color(255, 255, 255, 82));
        g2d.fillOval((int) x - radius + 10, (int) y - radius + 10, radius - 8, radius - 8);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawOval((int) x - radius, (int) y - radius, radius * 2, radius * 2);

        g2d.setColor(Color.WHITE);
        g2d.fillOval((int) x - 18, (int) y - 14, 14, 18);
        g2d.fillOval((int) x + 4, (int) y - 14, 14, 18);
        g2d.setColor(new Color(20, 20, 20));
        g2d.fillOval((int) x - 14, (int) y - 10, 8, 10);
        g2d.fillOval((int) x + 8, (int) y - 10, 8, 10);
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int) x - 11, (int) y - 7, 2, 2);
        g2d.fillOval((int) x + 11, (int) y - 7, 2, 2);
    }
}
