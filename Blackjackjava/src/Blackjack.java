import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Blackjack – Las Vegas Rules, 6 Decks
 * Full port of the React/JSX version including all game logic, splits,
 * doubles, surrender, insurance, and a Swing GUI that closely mirrors the
 * original colour scheme.
 *
 * Compile:  javac Blackjack.java
 * Run:      java Blackjack
 */
public class Blackjack extends JFrame {

    // ─── Constants ────────────────────────────────────────────────────────────
    static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    static final String[] RANKS = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
    static final int[] CHIP_VALUES = {1, 5, 25, 100, 500};

    static final Color BG_DARK      = new Color(8,   15,  10);
    static final Color BG_MID       = new Color(13,  42,  26);
    static final Color FELT_DARK    = new Color(14,  59,  36);
    static final Color FELT_MID     = new Color(29, 107,  64);
    static final Color GOLD         = new Color(241,196, 15);
    static final Color GOLD_DIM     = new Color(180,140,  8);
    static final Color GREEN_WIN    = new Color(46, 204,113);
    static final Color RED_LOSE     = new Color(231, 76, 60);
    static final Color PUSH_GRAY    = new Color(149,165,166);
    static final Color BORDER_GOLD  = new Color(139,105, 20);

    static final Map<Integer,Color[]> CHIP_COLORS = new LinkedHashMap<>();
    static {
        //               bg                          border                     text
        CHIP_COLORS.put(  1, new Color[]{new Color(245,245,245), new Color(187,187,187), Color.DARK_GRAY});
        CHIP_COLORS.put(  5, new Color[]{new Color(231, 76, 60), new Color(192, 57, 43), Color.WHITE});
        CHIP_COLORS.put( 25, new Color[]{new Color( 39,174, 96), new Color( 30,132, 73), Color.WHITE});
        CHIP_COLORS.put(100, new Color[]{new Color( 41,128,185), new Color( 26,102,148), Color.WHITE});
        CHIP_COLORS.put(500, new Color[]{new Color(142, 68,173), new Color(108, 52,131), Color.WHITE});
    }

    // ─── Card model ──────────────────────────────────────────────────────────
    static class Card {
        final String suit, rank;
        boolean faceDown;
        Card(String suit, String rank, boolean faceDown) {
            this.suit = suit; this.rank = rank; this.faceDown = faceDown;
        }
    }

    // ─── History entry ────────────────────────────────────────────────────────
    static class HistoryEntry {
        final String outcome, time;
        final int bet, net;
        HistoryEntry(String outcome, int bet, int net) {
            this.outcome = outcome; this.bet = bet; this.net = net;
            this.time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
    }

    // ─── Game state ───────────────────────────────────────────────────────────
    enum Phase { BETTING, PLAYING, DEALER_TURN, RESULT }

    LinkedList<Card> deck = new LinkedList<>();
    List<List<Card>>  playerHands = new ArrayList<>();
    List<Card>        dealerHand  = new ArrayList<>();
    List<Integer>     bets        = new ArrayList<>();
    List<String>      results     = new ArrayList<>();
    List<Integer>     doubledHands= new ArrayList<>();
    List<HistoryEntry>history     = new ArrayList<>();

    int     currentBet     = 0;
    int     balance        = 1000;
    int     currentHandIdx = 0;
    int     insuranceBet   = 0;
    boolean offeringInsurance = false;
    boolean insurancePaid     = false;
    boolean surrendered       = false;
    Phase   phase          = Phase.BETTING;

    // Stats
    int wins=0, losses=0, pushes=0, blackjacks=0;

    // ─── UI components ────────────────────────────────────────────────────────
    JPanel  tablePanel;
    JPanel  dealerPanel, playerPanel;
    JLabel  messageLabel;
    JLabel  balanceLabel, winsLabel, lossesLabel, pushesLabel, bjLabel;
    JPanel  bettingArea, actionArea, resultArea;
    JButton dealButton, hitButton, standButton, doubleButton, splitButton, surrenderButton, nextHandButton;
    JPanel  chipPanel;
    JScrollPane historyScroll;
    JPanel  historyPanel;
    boolean showHistory = false;
    JButton historyBtn;
    JPanel  insurancePanel;

    public Blackjack() {
        super("♠ BLACKJACK ♣");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(BG_DARK);
        buildUI();
        shuffleDeck(6);
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(760, 700));
        setVisible(true);
        updateUI();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UI CONSTRUCTION
    // ═══════════════════════════════════════════════════════════════════════════
    void buildUI() {
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0,0, BG_MID, 0, getHeight(), BG_DARK);
                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(16,12,16,12));
        setContentPane(root);

        // Header
        root.add(buildHeader());
        root.add(Box.createVerticalStrut(8));

        // Stats bar
        root.add(buildStatsBar());
        root.add(Box.createVerticalStrut(6));

        // History drawer
        historyPanel = buildHistoryPanel();
        historyPanel.setVisible(false);
        root.add(historyPanel);

        // Message banner
        messageLabel = new JLabel("Place your bet to begin!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Georgia", Font.BOLD, 13));
        messageLabel.setForeground(GOLD);
        messageLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(255,215,0,50), 1, true),
                new EmptyBorder(6,20,6,20)));
        messageLabel.setOpaque(true);
        messageLabel.setBackground(new Color(0,0,0,128));
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        root.add(messageLabel);
        root.add(Box.createVerticalStrut(8));

        // Insurance modal
        insurancePanel = buildInsurancePanel();
        insurancePanel.setVisible(false);
        insurancePanel.setAlignmentX(CENTER_ALIGNMENT);
        root.add(insurancePanel);

        // Table (felt)
        tablePanel = buildTable();
        tablePanel.setAlignmentX(CENTER_ALIGNMENT);
        root.add(tablePanel);
        root.add(Box.createVerticalStrut(10));

        // Betting area
        bettingArea = buildBettingArea();
        bettingArea.setAlignmentX(CENTER_ALIGNMENT);
        root.add(bettingArea);

        // Action buttons
        actionArea = buildActionArea();
        actionArea.setAlignmentX(CENTER_ALIGNMENT);
        actionArea.setVisible(false);
        root.add(actionArea);

        // Result button
        resultArea = buildResultArea();
        resultArea.setAlignmentX(CENTER_ALIGNMENT);
        resultArea.setVisible(false);
        root.add(resultArea);

        // Rules footer
        root.add(Box.createVerticalStrut(10));
        root.add(buildFooter());
    }

    JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("♠ BLACKJACK ♣", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 32));
        title.setForeground(GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);
        p.add(title);

        JLabel sub = new JLabel("LAS VEGAS RULES • 6 DECKS • DEALER STANDS SOFT 17", SwingConstants.CENTER);
        sub.setFont(new Font("Georgia", Font.PLAIN, 10));
        sub.setForeground(new Color(255,215,0,128));
        sub.setAlignmentX(CENTER_ALIGNMENT);
        p.add(sub);
        return p;
    }

    JPanel buildStatsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        bar.setOpaque(true);
        bar.setBackground(new Color(0,0,0,102));
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(255,215,0,38), 1, true),
                new EmptyBorder(4,12,4,12)));

        balanceLabel = statBox("BALANCE", "$1000", GOLD, 20);
        winsLabel    = statBox("W",  "0", GREEN_WIN, 16);
        lossesLabel  = statBox("L",  "0", RED_LOSE,  16);
        pushesLabel  = statBox("P",  "0", PUSH_GRAY, 16);
        bjLabel      = statBox("BJ", "0", GOLD,      16);

        bar.add(balanceLabel.getParent());
        bar.add(winsLabel.getParent());
        bar.add(lossesLabel.getParent());
        bar.add(pushesLabel.getParent());
        bar.add(bjLabel.getParent());

        historyBtn = styledButton("📋 History", new Color(255,255,255,13), new Color(255,255,255,26), Color.GRAY, 10);
        historyBtn.addActionListener(e -> { showHistory = !showHistory; historyPanel.setVisible(showHistory); pack(); });
        bar.add(historyBtn);
        return bar;
    }

    /** Returns the value label; its parent is the stat box panel. */
    JLabel statBox(String key, String val, Color valColor, int fontSize) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel kLbl = new JLabel(key, SwingConstants.CENTER);
        kLbl.setFont(new Font("Georgia", Font.PLAIN, 9));
        kLbl.setForeground(new Color(170,170,170));
        kLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel vLbl = new JLabel(val, SwingConstants.CENTER);
        vLbl.setFont(new Font("Georgia", Font.BOLD, fontSize));
        vLbl.setForeground(valColor);
        vLbl.setAlignmentX(CENTER_ALIGNMENT);

        box.add(kLbl);
        box.add(vLbl);
        return vLbl;
    }

    JPanel buildHistoryPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(new Color(0,0,0,178));
        wrapper.setBorder(new CompoundBorder(
                new LineBorder(new Color(255,215,0,51), 1, true),
                new EmptyBorder(6,10,6,10)));
        wrapper.setMaximumSize(new Dimension(500, 160));
        wrapper.setAlignmentX(CENTER_ALIGNMENT);

        JLabel hdr = new JLabel("RECENT HANDS");
        hdr.setFont(new Font("Georgia", Font.BOLD, 11));
        hdr.setForeground(GOLD);
        hdr.setBorder(new EmptyBorder(0,0,4,0));
        wrapper.add(hdr, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);

        historyScroll = new JScrollPane(list);
        historyScroll.setOpaque(false);
        historyScroll.getViewport().setOpaque(false);
        historyScroll.setBorder(null);
        historyScroll.setPreferredSize(new Dimension(480, 110));
        wrapper.add(historyScroll, BorderLayout.CENTER);

        return wrapper;
    }

    void refreshHistory() {
        JPanel list = (JPanel) historyScroll.getViewport().getView();
        list.removeAll();
        if (history.isEmpty()) {
            JLabel none = new JLabel("No hands yet");
            none.setForeground(new Color(85,85,85));
            none.setFont(new Font("Georgia", Font.PLAIN, 11));
            list.add(none);
        } else {
            for (HistoryEntry h : history) {
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false);
                row.setBorder(new MatteBorder(0,0,1,0, new Color(255,255,255,13)));
                Color netColor = h.net > 0 ? GREEN_WIN : h.net < 0 ? RED_LOSE : PUSH_GRAY;
                String netStr  = h.net > 0 ? "+$"+h.net : h.net < 0 ? "-$"+Math.abs(h.net) : "±0";
                JLabel time = label(h.time, Color.GRAY, 10, Font.PLAIN);
                JLabel out  = label(h.outcome, new Color(204,204,204), 10, Font.PLAIN);
                JLabel net  = label(netStr, netColor, 10, Font.BOLD);
                row.add(time, BorderLayout.WEST);
                row.add(out,  BorderLayout.CENTER);
                row.add(net,  BorderLayout.EAST);
                list.add(row);
            }
        }
        list.revalidate();
        list.repaint();
    }

    JPanel buildInsurancePanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(true);
        p.setBackground(new Color(0,0,0,204));
        p.setBorder(new CompoundBorder(
                new LineBorder(GOLD, 2, true),
                new EmptyBorder(10,20,10,20)));
        p.setMaximumSize(new Dimension(400, 120));

        JLabel title = label("🛡️ Insurance?", GOLD, 15, Font.BOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);
        p.add(title);

        JLabel cost = label("Cost: $? — Pays 2:1 if dealer has Blackjack", new Color(204,204,204), 11, Font.PLAIN);
        cost.setName("insuranceCost");
        cost.setAlignmentX(CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(4));
        p.add(cost);
        p.add(Box.createVerticalStrut(8));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btns.setOpaque(false);
        JButton take    = styledButton("Take Insurance", GOLD, GOLD.darker(), Color.BLACK, 12);
        JButton decline = styledButton("No Thanks", new Color(255,255,255,26), new Color(255,255,255,51), Color.WHITE, 12);
        take.addActionListener(e -> takeInsurance());
        decline.addActionListener(e -> declineInsurance());
        btns.add(take);
        btns.add(decline);
        p.add(btns);
        return p;
    }

    JPanel buildTable() {
        JPanel table = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Felt gradient
                RadialGradientPaint felt = new RadialGradientPaint(
                        new Point2D.Float(w/2f, h*0.4f), Math.max(w,h)*0.7f,
                        new float[]{0f, 0.5f, 1f},
                        new Color[]{FELT_MID, FELT_DARK, new Color(14,59,36)});
                g2.setPaint(felt);
                g2.fillRoundRect(0,0,w,h,24,24);
                // Border
                g2.setColor(BORDER_GOLD);
                g2.setStroke(new BasicStroke(6f));
                g2.drawRoundRect(3,3,w-6,h-6,22,22);
                // Inscription
                g2.setFont(new Font("Georgia", Font.PLAIN, 9));
                g2.setColor(new Color(255,255,255,38));
                String ins = "BLACKJACK PAYS 3 TO 2";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(ins, (w - fm.stringWidth(ins))/2, 18);
                g2.dispose();
            }
        };
        table.setLayout(new BoxLayout(table, BoxLayout.Y_AXIS));
        table.setOpaque(false);
        table.setBorder(new EmptyBorder(22,16,16,16));
        table.setMaximumSize(new Dimension(720, 340));

        // Dealer section
        JLabel dLbl = label("DEALER", new Color(255,255,255,102), 10, Font.PLAIN);
        dLbl.setAlignmentX(CENTER_ALIGNMENT);
        dLbl.setBorder(new EmptyBorder(0,0,6,0));
        dLbl.setName("dealerLabel");
        table.add(dLbl);

        dealerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        dealerPanel.setOpaque(false);
        dealerPanel.setPreferredSize(new Dimension(680, 110));
        table.add(dealerPanel);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255,255,255,20));
        sep.setBackground(new Color(255,255,255,20));
        table.add(sep);
        table.add(Box.createVerticalStrut(8));

        // Player section
        JLabel pLbl = label("PLAYER", new Color(255,255,255,102), 10, Font.PLAIN);
        pLbl.setAlignmentX(CENTER_ALIGNMENT);
        pLbl.setBorder(new EmptyBorder(0,0,6,0));
        pLbl.setName("playerLabel");
        table.add(pLbl);

        playerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        playerPanel.setOpaque(false);
        playerPanel.setPreferredSize(new Dimension(680, 130));
        table.add(playerPanel);

        return table;
    }

    JPanel buildBettingArea() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(620, 200));

        JLabel chipLbl = label("SELECT CHIPS", new Color(255,255,255,102), 11, Font.PLAIN);
        chipLbl.setAlignmentX(CENTER_ALIGNMENT);
        p.add(chipLbl);
        p.add(Box.createVerticalStrut(8));

        chipPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        chipPanel.setOpaque(false);
        for (int v : CHIP_VALUES) {
            ChipButton cb = new ChipButton(v);
            cb.addActionListener(e -> addChip(v));
            chipPanel.add(cb);
        }
        p.add(chipPanel);
        p.add(Box.createVerticalStrut(10));
        p.add(new BetDisplay());
        p.add(Box.createVerticalStrut(10));

        dealButton = styledButton("🃏  DEAL", GOLD, GOLD.darker(), Color.BLACK, 16);
        dealButton.setFont(new Font("Georgia", Font.BOLD, 16));
        dealButton.setPreferredSize(new Dimension(160, 44));
        dealButton.addActionListener(e -> deal());
        dealButton.setAlignmentX(CENTER_ALIGNMENT);
        p.add(dealButton);
        return p;
    }

    JPanel buildActionArea() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);

        hitButton      = actionBtn("✅  HIT",        new Color(39,174,96),  new Color(30,132,73));
        standButton    = actionBtn("✋  STAND",       new Color(192,57,43),  new Color(146,43,33));
        doubleButton   = actionBtn("✌️  DOUBLE",      new Color(41,128,185), new Color(26,102,148));
        splitButton    = actionBtn("✂️  SPLIT",       new Color(142,68,173), new Color(108,52,131));
        surrenderButton= actionBtn("🏳  SURRENDER",  new Color(100,80,0),   new Color(80,60,0));
        surrenderButton.setForeground(GOLD);

        hitButton.addActionListener(e -> hit());
        standButton.addActionListener(e -> stand());
        doubleButton.addActionListener(e -> doubleDown());
        splitButton.addActionListener(e -> split());
        surrenderButton.addActionListener(e -> surrender());

        p.add(hitButton); p.add(standButton); p.add(doubleButton);
        p.add(splitButton); p.add(surrenderButton);
        return p;
    }

    JPanel buildResultArea() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        nextHandButton = styledButton("🔄  NEXT HAND", GOLD, GOLD.darker(), Color.BLACK, 15);
        nextHandButton.setPreferredSize(new Dimension(180, 42));
        nextHandButton.addActionListener(e -> newRound());
        p.add(nextHandButton);
        return p;
    }

    JPanel buildFooter() {
        JLabel lbl = new JLabel(
                "<html><center>6 DECKS • BJ PAYS 3:2 • DEALER STANDS SOFT 17 • SPLIT UP TO 4 HANDS<br>" +
                        "DOUBLE ON ANY 2 CARDS • SURRENDER AVAILABLE FIRST 2 CARDS • INSURANCE ON DEALER ACE</center></html>",
                SwingConstants.CENTER);
        lbl.setFont(new Font("Georgia", Font.PLAIN, 9));
        lbl.setForeground(new Color(255,255,255,51));
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.add(lbl);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  CARD / HAND RENDERING
    // ═══════════════════════════════════════════════════════════════════════════
    /** A single playing card rendered via Swing painting. */
    static class CardView extends JPanel {
        final Card card;
        final boolean small;
        static final int W_LARGE=72, H_LARGE=104, W_SMALL=52, H_SMALL=76;
        static final int CORNER=12;

        CardView(Card card, boolean small) {
            this.card = card; this.small = small;
            int w = small ? W_SMALL : W_LARGE;
            int h = small ? H_SMALL : H_LARGE;
            setPreferredSize(new Dimension(w, h));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            if (card.faceDown) {
                // Card back
                GradientPaint gp = new GradientPaint(0,0, new Color(26,26,110), w,h, new Color(45,45,143));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,w,h,CORNER,CORNER);
                g2.setColor(new Color(74,74,192));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1,1,w-2,h-2,CORNER,CORNER);
                // Hatch
                g2.setColor(new Color(255,255,255,10));
                g2.setStroke(new BasicStroke(0.5f));
                for (int x=-h; x<w+h; x+=8) g2.drawLine(x,0,x+h,h);
                g2.dispose();
                return;
            }

            // Card face
            GradientPaint face = new GradientPaint(0,0, new Color(255,254,248), w,h, new Color(248,244,232));
            g2.setPaint(face);
            g2.fillRoundRect(0,0,w,h,CORNER,CORNER);
            g2.setColor(new Color(221,221,221));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(0,0,w-1,h-1,CORNER,CORNER);

            boolean red = card.suit.equals("♥") || card.suit.equals("♦");
            Color fgColor = red ? new Color(192,57,43) : new Color(26,26,46);
            g2.setColor(fgColor);

            int fontSize = small ? 11 : 14;
            g2.setFont(new Font("Georgia", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();

            // Top-left corner
            g2.drawString(card.rank, 4, fm.getAscent() + 2);
            g2.drawString(card.suit, 4, fm.getAscent() + fontSize + 4);

            // Centre suit (watermark)
            int bigFont = small ? 26 : 38;
            g2.setFont(new Font("SansSerif", Font.PLAIN, bigFont));
            g2.setColor(new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), 40));
            FontMetrics fm2 = g2.getFontMetrics();
            int sx = (w - fm2.stringWidth(card.suit))/2;
            int sy = h/2 + fm2.getAscent()/2 - 4;
            g2.drawString(card.suit, sx, sy);

            // Bottom-right (rotated)
            g2.setFont(new Font("Georgia", Font.BOLD, fontSize));
            fm = g2.getFontMetrics();
            g2.setColor(fgColor);
            AffineTransform old = g2.getTransform();
            g2.rotate(Math.PI, w/2.0, h/2.0);
            g2.drawString(card.rank, 4, fm.getAscent() + 2);
            g2.drawString(card.suit, 4, fm.getAscent() + fontSize + 4);
            g2.setTransform(old);
            g2.dispose();
        }
    }

    /** A hand panel: label + overlapping cards + total badge + result label. */
    JPanel buildHandPanel(List<Card> cards, String label, boolean isActive, String result, int bet, boolean small) {
        JPanel outer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(new Color(255,215,0,20));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setColor(new Color(255,215,0,128));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                } else {
                    g2.setColor(new Color(0,0,0,51));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setColor(new Color(255,255,255,13));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                }
                g2.dispose();
            }
        };
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(8,10,8,10));

        if (label != null && !label.isEmpty()) {
            JLabel lbl = label(label.toUpperCase(), new Color(170,170,170), 10, Font.PLAIN);
            lbl.setAlignmentX(CENTER_ALIGNMENT);
            outer.add(lbl);
        }

        // Overlapping cards
        JPanel cardRow = new JPanel(null); // absolute layout for overlap
        cardRow.setOpaque(false);
        int cw = small ? CardView.W_SMALL : CardView.W_LARGE;
        int ch = small ? CardView.H_SMALL : CardView.H_LARGE;
        int overlap = small ? 18 : 24;
        int total_w = cw + (cards.size()-1) * (cw - overlap);
        if (total_w < cw) total_w = cw;
        cardRow.setPreferredSize(new Dimension(Math.max(total_w+4, 80), ch+4));
        cardRow.setMinimumSize(cardRow.getPreferredSize());
        cardRow.setMaximumSize(cardRow.getPreferredSize());
        for (int i = 0; i < cards.size(); i++) {
            CardView cv = new CardView(cards.get(i), small);
            cv.setBounds(i*(cw-overlap), 2, cw, ch);
            cardRow.add(cv);
        }
        cardRow.setAlignmentX(CENTER_ALIGNMENT);
        outer.add(cardRow);

        // Total + bet badge
        int tot = handTotalVisible(cards);
        if (tot > 0) {
            JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
            badgeRow.setOpaque(false);
            if (phase != Phase.BETTING) {
                JLabel totalBadge = label(tot > 21 ? "BUST" : String.valueOf(tot),
                        tot > 21 ? RED_LOSE : tot == 21 ? GOLD : Color.WHITE, 13, Font.BOLD);
                totalBadge.setBorder(new CompoundBorder(
                        new LineBorder(tot > 21 ? RED_LOSE : new Color(255,255,255,26), 1, true),
                        new EmptyBorder(1,8,1,8)));
                totalBadge.setOpaque(true);
                totalBadge.setBackground(new Color(0,0,0,128));
                badgeRow.add(totalBadge);
            }
            if (bet > 0) {
                JLabel betLbl = label("Bet: $"+bet, GOLD, 11, Font.BOLD);
                badgeRow.add(betLbl);
            }
            badgeRow.setAlignmentX(CENTER_ALIGNMENT);
            outer.add(badgeRow);
        }

        // Result label
        if (result != null && !result.isEmpty()) {
            Color rc = switch(result) {
                case "win"       -> GREEN_WIN;
                case "blackjack" -> GOLD;
                case "push"      -> PUSH_GRAY;
                case "lose","bust" -> RED_LOSE;
                case "surrender" -> new Color(230,126,34);
                default          -> Color.WHITE;
            };
            JLabel rLbl = label(result.toUpperCase(), rc, 15, Font.BOLD);
            rLbl.setAlignmentX(CENTER_ALIGNMENT);
            outer.add(rLbl);
        }

        return outer;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DECK / CARD LOGIC
    // ═══════════════════════════════════════════════════════════════════════════
    void shuffleDeck(int numDecks) {
        deck.clear();
        for (int n = 0; n < numDecks; n++)
            for (String s : SUITS)
                for (String r : RANKS)
                    deck.add(new Card(s, r, false));
        Collections.shuffle(deck);
    }

    Card dealCard(boolean faceDown) {
        if (deck.size() < 52) shuffleDeck(6);
        Card c = deck.removeFirst();
        return new Card(c.suit, c.rank, faceDown);
    }
    Card dealCard() { return dealCard(false); }

    static int cardValue(String rank) {
        return switch(rank) {
            case "J","Q","K" -> 10;
            case "A"         -> 11;
            default          -> Integer.parseInt(rank);
        };
    }

    static int handTotal(List<Card> hand) {
        int total = 0, aces = 0;
        for (Card c : hand) {
            total += cardValue(c.rank);
            if (c.rank.equals("A")) aces++;
        }
        while (total > 21 && aces > 0) { total -= 10; aces--; }
        return total;
    }

    static int handTotalVisible(List<Card> hand) {
        int total = 0, aces = 0;
        for (Card c : hand) {
            if (c.faceDown) continue;
            total += cardValue(c.rank);
            if (c.rank.equals("A")) aces++;
        }
        while (total > 21 && aces > 0) { total -= 10; aces--; }
        return total;
    }

    static boolean isBust(List<Card> hand)      { return handTotal(hand) > 21; }
    static boolean isBlackjack(List<Card> hand)  { return hand.size() == 2 && handTotal(hand) == 21; }

    // ═══════════════════════════════════════════════════════════════════════════
    //  GAME ACTIONS
    // ═══════════════════════════════════════════════════════════════════════════
    void deal() {
        if (currentBet < 1) { setMsg("Minimum bet is $1!"); return; }
        if (currentBet > balance) { setMsg("Not enough chips!"); return; }
        balance -= currentBet;

        Card p1 = dealCard(), d1 = dealCard(), p2 = dealCard(), d2 = dealCard(true);

        playerHands.clear();
        List<Card> ph = new ArrayList<>(); ph.add(p1); ph.add(p2);
        playerHands.add(ph);
        dealerHand.clear(); dealerHand.add(d1); dealerHand.add(d2);
        bets.clear(); bets.add(currentBet);
        results.clear(); doubledHands.clear();
        currentHandIdx = 0;
        surrendered = false; insurancePaid = false; offeringInsurance = false;
        phase = Phase.PLAYING;

        // Insurance?
        if (d1.rank.equals("A")) {
            offeringInsurance = true;
            setMsg("Dealer shows Ace — Insurance? (costs half your bet)");
            updateInsuranceCost();
        } else if (isBlackjack(ph)) {
            List<Card> fullDealer = new ArrayList<>(dealerHand);
            fullDealer.get(1).faceDown = false; // peek
            if (isBlackjack(fullDealer)) {
                dealerHand.get(1).faceDown = false;
                results.add("push");
                balance += currentBet;
                phase = Phase.RESULT;
                setMsg("Both Blackjack — Push!");
                pushes++;
                addHistory("Push (BJ vs BJ)", currentBet, 0);
            } else {
                results.add("blackjack");
                int win = (int)(currentBet * 1.5);
                balance += currentBet + win;
                phase = Phase.RESULT;
                setMsg("Blackjack! You win $"+win+"!");
                wins++; blackjacks++;
                addHistory("Blackjack! 🃏", currentBet, win);
            }
        } else {
            setMsg("Your turn — Hit, Stand, Double, or Surrender");
        }
        updateUI();
    }

    void takeInsurance() {
        int ins = currentBet / 2;
        balance -= ins; insuranceBet = ins;
        offeringInsurance = false; insurancePaid = true;

        // Peek at hole card
        List<Card> fullDealer = new ArrayList<>();
        for (Card c : dealerHand) fullDealer.add(new Card(c.suit, c.rank, false));
        if (isBlackjack(fullDealer)) {
            dealerHand.get(1).faceDown = false;
            results.add("lose");
            balance += ins * 2;
            phase = Phase.RESULT;
            setMsg("Dealer Blackjack — Insurance pays! You break even.");
            losses++;
            addHistory("Lost (Dealer BJ, Ins)", currentBet, 0);
        } else {
            setMsg("No dealer Blackjack. Insurance lost. Your turn.");
        }
        updateUI();
    }

    void declineInsurance() {
        offeringInsurance = false;
        setMsg("Your turn — Hit, Stand, Double, or Surrender");
        updateUI();
    }

    void hit() {
        if (phase != Phase.PLAYING || offeringInsurance) return;
        playerHands.get(currentHandIdx).add(dealCard());
        int tot = handTotal(playerHands.get(currentHandIdx));
        if (tot > 21) { setMsg("Bust!"); advanceHand(); }
        else if (tot == 21) { advanceHand(); }
        else setMsg("Hit or Stand?");
        updateUI();
    }

    void stand() {
        if (phase != Phase.PLAYING || offeringInsurance) return;
        advanceHand();
        updateUI();
    }

    void doubleDown() {
        if (phase != Phase.PLAYING || offeringInsurance) return;
        if (playerHands.get(currentHandIdx).size() != 2) return;
        if (bets.get(currentHandIdx) > balance) { setMsg("Not enough chips to double!"); return; }
        balance -= bets.get(currentHandIdx);
        bets.set(currentHandIdx, bets.get(currentHandIdx) * 2);
        playerHands.get(currentHandIdx).add(dealCard());
        doubledHands.add(currentHandIdx);
        advanceHand();
        updateUI();
    }

    void split() {
        if (phase != Phase.PLAYING || offeringInsurance) return;
        List<Card> hand = playerHands.get(currentHandIdx);
        if (hand.size() != 2 || !hand.get(0).rank.equals(hand.get(1).rank)) return;
        if (bets.get(currentHandIdx) > balance) { setMsg("Not enough chips to split!"); return; }
        if (playerHands.size() >= 4) { setMsg("Max 4 splits reached"); return; }
        balance -= bets.get(currentHandIdx);

        List<Card> h1 = new ArrayList<>(); h1.add(hand.get(0)); h1.add(dealCard());
        List<Card> h2 = new ArrayList<>(); h2.add(hand.get(1)); h2.add(dealCard());
        playerHands.remove(currentHandIdx);
        playerHands.add(currentHandIdx, h2);
        playerHands.add(currentHandIdx, h1);
        bets.add(currentHandIdx, bets.get(currentHandIdx));
        setMsg("Split! Playing first hand.");
        updateUI();
    }

    void surrender() {
        if (phase != Phase.PLAYING || offeringInsurance) return;
        if (playerHands.get(currentHandIdx).size() != 2 || playerHands.size() > 1) return;
        int half = currentBet / 2;
        balance += half;
        surrendered = true;
        results.add("surrender");
        phase = Phase.RESULT;
        setMsg("Surrender — you get back $"+half);
        addHistory("Surrender", currentBet, -half);
        updateUI();
    }

    void advanceHand() {
        int next = currentHandIdx + 1;
        if (next < playerHands.size()) {
            currentHandIdx = next;
            setMsg("Playing hand "+(next+1)+" of "+playerHands.size());
        } else {
            runDealer();
        }
    }

    void runDealer() {
        phase = Phase.DEALER_TURN;
        // Reveal hole card
        if (!dealerHand.isEmpty()) dealerHand.get(dealerHand.size()-1).faceDown = false;

        boolean allBust = playerHands.stream().allMatch(Blackjack::isBust);
        updateUI();
        if (allBust) { resolveResults(); updateUI(); return; }

        // Dealer draws with delay
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (handTotal(dealerHand) < 17) {
                        dealerHand.add(dealCard());
                        updateUI();
                    } else {
                        timer.cancel();
                        resolveResults();
                        updateUI();
                    }
                });
            }
        }, 600, 600);
    }

    void resolveResults() {
        int dTot   = handTotal(dealerHand);
        boolean dBJ = isBlackjack(dealerHand);
        results.clear();
        int totalNet = 0;

        for (int i = 0; i < playerHands.size(); i++) {
            List<Card> hand = playerHands.get(i);
            int pTot  = handTotal(hand);
            boolean pBJ = isBlackjack(hand) && hand.size() == 2 && !doubledHands.contains(i);
            int bet   = bets.get(i);

            if (isBust(hand)) {
                results.add("bust"); totalNet -= bet;
                addHistory("Hand "+(i+1)+" Bust", bet, -bet);
                losses++;
            } else if (dBJ && !pBJ) {
                results.add("lose"); totalNet -= bet;
                addHistory("Hand "+(i+1)+" Lost (Dealer BJ)", bet, -bet);
                losses++;
            } else if (pBJ && !dBJ) {
                int win = (int)(bet * 1.5);
                balance += bet + win;
                results.add("blackjack"); totalNet += win;
                addHistory("Hand "+(i+1)+" Blackjack!", bet, win);
                wins++; blackjacks++;
            } else if (isBust(dealerHand) || pTot > dTot) {
                balance += bet * 2;
                results.add("win"); totalNet += bet;
                addHistory("Hand "+(i+1)+" Win", bet, bet);
                wins++;
            } else if (pTot == dTot) {
                balance += bet;
                results.add("push"); totalNet += 0;
                addHistory("Hand "+(i+1)+" Push", bet, 0);
                pushes++;
            } else {
                results.add("lose"); totalNet -= bet;
                addHistory("Hand "+(i+1)+" Lose", bet, -bet);
                losses++;
            }
        }

        phase = Phase.RESULT;
        if (totalNet > 0)      setMsg("You win $"+totalNet+"! 🎉");
        else if (totalNet < 0) setMsg("You lose $"+Math.abs(totalNet)+".");
        else                   setMsg("Push — bet returned.");
    }

    void newRound() {
        if (balance < 1) { balance = 1000; setMsg("Reloaded $1000 chips. Good luck!"); }
        playerHands.clear(); playerHands.add(new ArrayList<>());
        dealerHand.clear();
        bets.clear(); bets.add(currentBet);
        results.clear(); doubledHands.clear();
        currentHandIdx = 0; surrendered = false;
        insuranceBet = 0; insurancePaid = false; offeringInsurance = false;
        phase = Phase.BETTING;
        setMsg(currentBet == 0 ? "Place your bet to begin!" : "Betting $"+currentBet+" — Deal when ready!");
        updateUI();
    }

    void addChip(int v) {
        if (phase != Phase.BETTING) return;
        if (currentBet + v > balance) { setMsg("Not enough chips!"); return; }
        currentBet += v;
        setMsg("Bet: $"+currentBet+" — Add more or Deal!");
        updateUI();
    }

    void clearBet() {
        currentBet = 0;
        setMsg("Place your bet to begin!");
        updateUI();
    }

    void addHistory(String outcome, int bet, int net) {
        history.add(0, new HistoryEntry(outcome, bet, net));
        if (history.size() > 20) history.remove(history.size()-1);
    }

    void setMsg(String msg) { messageLabel.setText(msg); }

    void updateInsuranceCost() {
        // Find the cost label inside insurancePanel
        for (Component c : insurancePanel.getComponents()) {
            if (c instanceof JLabel lbl && "insuranceCost".equals(lbl.getName()))
                lbl.setText("Cost: $"+(currentBet/2)+" — Pays 2:1 if dealer has Blackjack");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  UI REFRESH
    // ═══════════════════════════════════════════════════════════════════════════
    void updateUI() {
        // Stats
        balanceLabel.setText("$"+String.format("%,d", balance));
        winsLabel.setText(String.valueOf(wins));
        lossesLabel.setText(String.valueOf(losses));
        pushesLabel.setText(String.valueOf(pushes));
        bjLabel.setText(String.valueOf(blackjacks));

        // Insurance modal
        insurancePanel.setVisible(offeringInsurance);

        // Dealer hand
        dealerPanel.removeAll();
        if (dealerHand.isEmpty()) {
            JLabel wait = label("Waiting…", new Color(255,255,255,51), 12, Font.PLAIN);
            dealerPanel.add(wait);
        } else {
            int dTot = phase != Phase.BETTING && phase != Phase.PLAYING
                    ? handTotal(dealerHand) : handTotalVisible(dealerHand);
            String dLabel = (phase != Phase.BETTING && phase != Phase.PLAYING && dTot > 0)
                    ? "DEALER — "+(dTot > 21 ? "BUST" : dTot) : "DEALER";
            dealerPanel.add(buildHandPanel(dealerHand, null, phase == Phase.DEALER_TURN, null, 0, false));
        }

        // Player hands
        playerPanel.removeAll();
        boolean small = playerHands.size() > 2;
        if (phase == Phase.BETTING || playerHands.stream().allMatch(List::isEmpty)) {
            JLabel wait = label("Your cards appear here", new Color(255,255,255,51), 12, Font.PLAIN);
            playerPanel.add(wait);
        } else {
            for (int i = 0; i < playerHands.size(); i++) {
                List<Card> hand = playerHands.get(i);
                if (hand.isEmpty()) continue;
                String lbl  = playerHands.size() > 1 ? "Hand "+(i+1) : null;
                boolean active = phase == Phase.PLAYING && currentHandIdx == i && !offeringInsurance;
                String res = (results.size() > i) ? results.get(i) : null;
                int bet = (bets.size() > i) ? bets.get(i) : 0;
                playerPanel.add(buildHandPanel(hand, lbl, active, res, bet, small));
            }
        }

        // Bet area chips
        for (Component c : chipPanel.getComponents()) {
            if (c instanceof ChipButton cb) cb.setDimmed(cb.value > balance);
        }

        // Area visibility
        bettingArea.setVisible(phase == Phase.BETTING);
        actionArea.setVisible(phase == Phase.PLAYING && !offeringInsurance);
        resultArea.setVisible(phase == Phase.RESULT);

        // Action button states
        boolean canDouble = phase == Phase.PLAYING && !offeringInsurance
                && !playerHands.get(currentHandIdx).isEmpty()
                && playerHands.get(currentHandIdx).size() == 2
                && bets.get(currentHandIdx) <= balance;
        boolean canSplit = canDouble
                && playerHands.get(currentHandIdx).get(0).rank.equals(playerHands.get(currentHandIdx).get(1).rank)
                && playerHands.size() < 4;
        boolean canSurrender = phase == Phase.PLAYING && !offeringInsurance
                && !playerHands.get(currentHandIdx).isEmpty()
                && playerHands.get(currentHandIdx).size() == 2
                && playerHands.size() == 1;

        doubleButton.setEnabled(canDouble);
        splitButton.setEnabled(canSplit);
        surrenderButton.setEnabled(canSurrender);

        // Deal button enabled
        dealButton.setEnabled(currentBet >= 1 && currentBet <= balance);

        // History
        if (showHistory) refreshHistory();

        tablePanel.revalidate();
        tablePanel.repaint();
        revalidate();
        repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  WIDGET HELPERS
    // ═══════════════════════════════════════════════════════════════════════════
    static JLabel label(String text, Color fg, int size, int style) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia", style, size));
        l.setForeground(fg);
        return l;
    }

    static JButton styledButton(String text, Color bg, Color hover, Color fg, int size) {
        JButton b = new JButton(text) {
            boolean hovered = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered=true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered=false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered && isEnabled() ? hover : isEnabled() ? bg : bg.darker().darker());
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        b.setFont(new Font("Georgia", Font.BOLD, size));
        b.setForeground(fg);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(130, 34));
        return b;
    }

    static JButton actionBtn(String text, Color bg, Color border) {
        JButton b = styledButton(text, bg, bg.brighter(), Color.WHITE, 12);
        b.setPreferredSize(new Dimension(120, 34));
        return b;
    }

    // ─── Chip button ──────────────────────────────────────────────────────────
    class ChipButton extends JButton {
        final int value;
        boolean dimmed = false;

        ChipButton(int value) {
            this.value = value;
            setPreferredSize(new Dimension(56, 56));
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                boolean hov = false;
                public void mouseEntered(MouseEvent e) { if (!dimmed) { hov=true;  repaint(); } }
                public void mouseExited (MouseEvent e) { hov=false; repaint(); }
            });
        }

        void setDimmed(boolean d) { this.dimmed = d; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color[] cc = CHIP_COLORS.get(value);
            Color bg = cc[0], border = cc[1], fg = cc[2];
            if (dimmed) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            // Body
            RadialGradientPaint rp = new RadialGradientPaint(new Point2D.Float(20,20), 28,
                    new float[]{0f,1f}, new Color[]{bg, border});
            g2.setPaint(rp);
            g2.fillOval(1,1,53,53);
            // Dashed border
            g2.setColor(border);
            float[] dash = {4f,4f};
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, dash, 0));
            g2.drawOval(2,2,51,51);
            // Value text
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, dimmed?0.4f:1f));
            g2.setColor(fg);
            g2.setFont(new Font("Georgia", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String txt = "$"+value;
            g2.drawString(txt, (55-fm.stringWidth(txt))/2, 33);
            g2.dispose();
        }
    }

    // ─── Bet display ──────────────────────────────────────────────────────────
    class BetDisplay extends JPanel {
        BetDisplay() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setMaximumSize(new Dimension(500, 80));
            setAlignmentX(CENTER_ALIGNMENT);
            refresh();
        }

        void refresh() {
            removeAll();
            if (currentBet == 0) {
                JLabel l = label("Place your bet above", new Color(255,255,255,76), 12, Font.ITALIC);
                l.setAlignmentX(CENTER_ALIGNMENT);
                add(l);
            } else {
                JLabel betLbl = label("BET: $"+currentBet, GOLD, 13, Font.BOLD);
                betLbl.setAlignmentX(CENTER_ALIGNMENT);
                add(betLbl);

                // Mini chip stack
                JPanel chips = new JPanel(null);
                chips.setOpaque(false);
                int rem = currentBet;
                List<Integer> stack = new ArrayList<>();
                int[] vals = {500,100,25,5,1};
                for (int v : vals) while (rem >= v) { stack.add(v); rem -= v; }
                int show = Math.min(stack.size(), 8);
                int cw = 36;
                int total = cw + (show-1)*(cw-22);
                chips.setPreferredSize(new Dimension(Math.max(total,40), 40));
                chips.setMaximumSize(chips.getPreferredSize());
                for (int i = 0; i < show; i++) {
                    Color[] cc = CHIP_COLORS.get(stack.get(i));
                    final int fi = i;
                    JPanel chip = new JPanel() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D)g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(cc[0]);
                            g2.fillOval(0,0,35,35);
                            float[] dash={3,3};
                            g2.setColor(cc[1]);
                            g2.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,0,dash,0));
                            g2.drawOval(1,1,33,33);
                            g2.setColor(cc[2]);
                            g2.setFont(new Font("Georgia",Font.BOLD,9));
                            String t = "$"+stack.get(fi);
                            FontMetrics fm = g2.getFontMetrics();
                            g2.drawString(t,(36-fm.stringWidth(t))/2, 22);
                            g2.dispose();
                        }
                    };
                    chip.setOpaque(false);
                    chip.setBounds(i*(cw-22), 2, 36, 36);
                    chips.add(chip);
                }
                chips.setAlignmentX(CENTER_ALIGNMENT);
                add(chips);
                if (stack.size() > 8) {
                    JLabel more = label("+"+(stack.size()-8)+" more", new Color(170,170,170), 10, Font.PLAIN);
                    more.setAlignmentX(CENTER_ALIGNMENT);
                    add(more);
                }

                JButton clear = styledButton("CLEAR", new Color(231,76,60,51), new Color(231,76,60,80), RED_LOSE, 10);
                clear.setPreferredSize(new Dimension(80, 26));
                clear.addActionListener(e -> clearBet());
                clear.setAlignmentX(CENTER_ALIGNMENT);
                add(Box.createVerticalStrut(4));
                add(clear);
            }
            revalidate(); repaint();
        }
    }

    // Update BetDisplay whenever bet changes
    @Override
    public void revalidate() {
        super.revalidate();
        // Refresh BetDisplay if present
        if (bettingArea != null) {
            for (Component c : bettingArea.getComponents()) {
                if (c instanceof BetDisplay bd) bd.refresh();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Blackjack::new);
    }
}
