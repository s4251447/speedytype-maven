package speedytype;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/** Grid of all achievements; unlocked ones glow in the accent color. */
final class AchievementsPanel extends Ui.Screen {
    private final Ui.Label count = new Ui.Label("", 17, Font.PLAIN, false).dim();
    private final JComponent grid = new JComponent() {
        { setPreferredSize(new Dimension(680, 336)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.smooth(g0); Set<String> have = Achievements.unlocked();
            int cw = (getWidth() - 12) / 2;
            for (int i = 0; i < Achievements.ALL.length; i++) {
                String[] a = Achievements.ALL[i]; boolean on = have.contains(a[0]);
                int x = (i % 2) * (cw + 12), y = (i / 2) * 56;
                Ui.glass(g, x, y, cw, 48, 18);
                if (on) { g.setColor(Ui.alpha(Theme.accent(), 45)); g.fillRoundRect(x, y, cw, 48, 18, 18); }
                g.setColor(on ? Theme.accent() : Theme.line()); g.fillOval(x + 12, y + 12, 24, 24);           // status dot
                if (on) { g.setColor(Color.WHITE); g.setStroke(new BasicStroke(2.5f)); g.drawLine(x + 18, y + 24, x + 22, y + 28); g.drawLine(x + 22, y + 28, x + 30, y + 19); g.setStroke(new BasicStroke(1f)); }
                g.setFont(Ui.font(Font.BOLD, 15)); g.setColor(on ? Theme.fg() : Theme.dim()); g.drawString(a[1], x + 48, y + 21);
                g.setFont(Ui.font(Font.PLAIN, 12)); g.setColor(Theme.dim()); g.drawString(a[2], x + 48, y + 38);
            }
            g.dispose();
        }
    };

    AchievementsPanel(SpeedyType app) {
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Trophies", 40, Font.BOLD, true)); card.put(count); card.put(grid);
        Ui.Glow back = new Ui.Glow("Back"); back.addActionListener(e -> app.go("home"));
        card.put(back); put(card);
    }
    void refresh() { count.setText(Achievements.unlocked().size() + " / " + Achievements.ALL.length + " unlocked"); grid.repaint(); }
}
