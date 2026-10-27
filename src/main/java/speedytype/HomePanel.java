package speedytype;

import javax.swing.*;
import java.awt.*;

/** Front page: title, Start button, and a grid of shortcuts to the other screens. */
final class HomePanel extends Ui.Screen {
    private final Ui.Label info = new Ui.Label("", 16, Font.PLAIN, false).dim();

    HomePanel(SpeedyType app) {
        put(new Ui.Label("SpeedyType", 84, Font.BOLD, true));
        put(new Ui.Label("Type fast. Type clean. Beat your goal.", 18, Font.PLAIN, false).dim(), 0);
        put(info, 4);

        Ui.Glow start = new Ui.Glow("Start", 272);
        start.addActionListener(e -> {
            // No goal yet? Offer to set one before playing.
            if (Theme.goal == 0 && JOptionPane.showConfirmDialog(app, "You haven't set a goal yet. Set one before playing?",
                    "Set a goal?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { app.go("goal"); return; }
            app.go("game");
        });
        put(start, 24);
        put(Ui.row(nav("Goal", "goal", app), nav("Stats", "stats", app)), 0);
        put(Ui.row(nav("Ranks", "board", app), nav("Trophies", "trophies", app)), 0);
        put(Ui.row(nav("Profile", "profile", app), nav("Settings", "settings", app)), 0);
    }
    private Ui.Glow nav(String text, String screen, SpeedyType app) {
        Ui.Glow b = new Ui.Glow(text, 136); b.addActionListener(e -> app.go(screen)); return b;
    }
    /** Called each time the home screen opens. */
    void refresh() {
        info.setText("Playing as " + Theme.profile + (Theme.goal > 0 ? "   |   Goal " + Theme.goal + " wpm" : ""));
    }
}
