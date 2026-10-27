package speedytype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Entry point. Owns the window, the screens, the fade transition, fullscreen and the animation clock. */
public class SpeedyType extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);            // holds all screens, one visible at a time
    final HomePanel home = new HomePanel(this);
    final TypingPanel game = new TypingPanel(this);
    final ResultPanel result = new ResultPanel(this);
    final GoalPanel goalScreen = new GoalPanel(this);
    final StatsPanel statsScreen = new StatsPanel(this);
    final SettingsPanel settings = new SettingsPanel(this);
    final ProfilePanel profiles = new ProfilePanel(this);
    final LeaderboardPanel board = new LeaderboardPanel(this);
    final AchievementsPanel trophies = new AchievementsPanel(this);
    final HeatmapPanel heat = new HeatmapPanel(this);

    private float fade = 0;                                    // 1 = fully covered, fades to 0 after each screen change
    private boolean fullscreen = false;
    /** Transparent overlay above everything that draws the fade. Mouse clicks pass straight through it. */
    private final JPanel glass = new JPanel() {
        @Override protected void paintComponent(Graphics g) {
            if (fade > 0.01f) { g.setColor(Ui.alpha(Theme.bg1(), (int) (255 * fade))); g.fillRect(0, 0, getWidth(), getHeight()); }
        }
        @Override public boolean contains(int x, int y) { return false; }
    };

    SpeedyType() {
        super("SpeedyType");
        root.add(home, "home"); root.add(goalScreen, "goal"); root.add(settings, "settings"); root.add(statsScreen, "stats");
        root.add(game, "game"); root.add(result, "result"); root.add(profiles, "profile");
        root.add(board, "board"); root.add(trophies, "trophies"); root.add(heat, "heatmap");
        add(root);
        glass.setOpaque(false); setGlassPane(glass); glass.setVisible(true);

        setIconImages(AppIcon.images());
        try { Taskbar.getTaskbar().setIconImage(AppIcon.images().get(4)); } catch (Exception e) { /* not supported here */ }
        setSize(1000, 700); setMinimumSize(new Dimension(820, 620)); setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // F11 toggles fullscreen from any screen
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F11"), "fullscreen");
        getRootPane().getActionMap().put("fullscreen", new AbstractAction() { public void actionPerformed(ActionEvent e) { toggleFullscreen(); } });

        // Animation clock: ~30 frames per second drives the backdrop, hover effects, caret, confetti and the fade
        home.refresh();
        new javax.swing.Timer(33, e -> {
            Ui.t += 0.033; root.repaint();
            if (fade > 0) { fade = Math.max(0, fade - 0.09f); glass.repaint(); }
        }).start();
    }

    /** Switches screen: home, goal, settings, stats, game, result, profile, board, trophies or heatmap. */
    void go(String name) {
        result.cancel();                                       // stop confetti / pending goal popup
        switch (name) {                                        // screens that show saved data reload it first
            case "home": home.refresh(); break;
            case "goal": goalScreen.refresh(); break;
            case "stats": statsScreen.refresh(); break;
            case "settings": settings.refresh(); break;
            case "profile": profiles.refresh(); break;
            case "board": board.refresh(); break;
            case "trophies": trophies.refresh(); break;
            case "heatmap": heat.refresh(); break;
            default: break;
        }
        cards.show(root, name);
        fade = 0.9f;
        if (name.equals("game")) game.reset();                 // fresh text every time
    }
    int width() { return Math.max(root.getWidth(), 960); }

    void toggleFullscreen() {
        fullscreen = !fullscreen;
        GraphicsDevice d = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        dispose(); setUndecorated(fullscreen); setVisible(true);
        if (d.isFullScreenSupported()) d.setFullScreenWindow(fullscreen ? this : null);
        else setExtendedState(fullscreen ? MAXIMIZED_BOTH : NORMAL);
    }

    public static void main(String[] args) {
        Sentences.load(); Sound.init();
        SwingUtilities.invokeLater(() -> new SpeedyType().setVisible(true));
    }
}
