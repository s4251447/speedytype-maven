package speedytype;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Shows totals, averages, personal bests and a bar chart of the last 20 runs. */
final class StatsPanel extends Ui.Screen {
    private final Board board = new Board();

    StatsPanel(SpeedyType app) {
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Stats", 40, Font.BOLD, true));
        card.put(board);
        Ui.Glow heat = new Ui.Glow("Heatmap", 200), back = new Ui.Glow("Back", 200);
        heat.addActionListener(e -> app.go("heatmap")); back.addActionListener(e -> app.go("home"));
        card.put(Ui.row(heat, back));
        put(card);
    }
    void refresh() { board.load(); }

    /** Custom-painted area that draws the tiles and the bar chart. */
    private static class Board extends JComponent {
        private List<String[]> runs = new ArrayList<>();
        Board() { setPreferredSize(new Dimension(640, 320)); }
        void load() { runs = Stats.history(); repaint(); }

        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.smooth(g0); int w = getWidth();
            double sum = 0, best = 0;
            for (String[] r : runs) { double v = Double.parseDouble(r[0]); sum += v; best = Math.max(best, v); }
            int tw = (w - 40) / 3;
            Ui.tile(g, 0, 0, tw, 84, "RUNS", String.valueOf(runs.size()));
            Ui.tile(g, tw + 20, 0, tw, 84, "AVERAGE", (runs.isEmpty() ? 0 : (int) (sum / runs.size())) + " wpm");
            Ui.tile(g, 2 * (tw + 20), 0, tw, 84, "BEST", (int) best + " wpm");

            int n = Math.min(20, runs.size()), top = 110, bh = 140;
            if (n == 0) {
                g.setFont(Ui.font(Font.PLAIN, 18)); g.setColor(Theme.dim()); Ui.center(g, "No runs yet - go type something!", w / 2.0, top + bh / 2.0);
            } else {
                double max = 1; for (int i = runs.size() - n; i < runs.size(); i++) max = Math.max(max, Double.parseDouble(runs.get(i)[0]));
                int barW = Math.min(40, (w - (n - 1) * 8) / n);
                for (int i = 0; i < n; i++) {
                    int h = (int) (bh * Double.parseDouble(runs.get(runs.size() - n + i)[0]) / max), x = i * (barW + 8);
                    g.setPaint(new GradientPaint(0, top + bh - h, Theme.accent(), 0, top + bh, Theme.accent2()));
                    g.fillRoundRect(x, top + bh - h, barW, h, 8, 8);
                }
            }
            g.setFont(Ui.font(Font.PLAIN, 14)); g.setColor(Theme.dim());
            Ui.center(g, "Last 20 runs (WPM)", w / 2.0, top + bh + 22);
            StringBuilder s = new StringBuilder("Best per mode:  ");
            for (String m : Theme.MODES) s.append(m).append(" ").append((int) Stats.best(m)).append("   ");
            Ui.center(g, s.toString(), w / 2.0, top + bh + 52);
            g.dispose();
        }
    }
}
