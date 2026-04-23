import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Dimension;

public class Game {
    public static final int MIN_WIDTH = 1280;
    public static final int MIN_HEIGHT = 800;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Zing Zing Zingbah Racing - Glowstar Cup");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
            frame.setSize(1440, 900);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);

            GamePanel gamePanel = new GamePanel();
            CharacterSelectPanel selectPanel = new CharacterSelectPanel(mainPanel, cardLayout, gamePanel);
            TitleScreenPanel titlePanel = new TitleScreenPanel(() -> {
                cardLayout.show(mainPanel, "SELECT");
                selectPanel.requestFocusInWindow();
            });

            gamePanel.setNavigation(mainPanel, cardLayout);

            mainPanel.add(titlePanel, "TITLE");
            mainPanel.add(selectPanel, "SELECT");
            mainPanel.add(gamePanel, "GAME");

            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            cardLayout.show(mainPanel, "TITLE");
            titlePanel.requestFocusInWindow();
        });
    }
}
