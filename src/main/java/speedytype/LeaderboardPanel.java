package speedytype;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/** Local high-score board: the top 10 runs from every profile on this computer, per mode. */
final class LeaderboardPanel extends Ui.Screen {
    private String mode = Theme.MODES[0];
    private final JPanel pillRow = Ui.row();
    private final Table table = new Table();

    LeaderboardPanel(SpeedyType app) {
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Leaderboard", 40, Font.BOLD, true));
        card.put(pillRow); card.put(table);
        Ui.Glow back = new Ui.Glow("Back"); back.addActionListener(e -> app.go("home"));
        card.put(back); put(card);
    }
    void refresh() {
        mode = Theme.mode; pillRow.removeAll(); ButtonGroup g = new ButtonGroup();
        for (String m : Theme.MODES) {
            Ui.Pill p = new Ui.Pill(m); p.setSelected(m.equals(mode));
            p.addActionListener(e -> { mode = m; table.repaint(); }); g.add(p); pillRow.add(p);
        }
        pillRow.revalidate(); table.repaint();
    }

    private class Table extends JComponent {
        Table() { setPreferredSize(new Dimension(660, 340)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.smooth(g0); List<String[]> rows = Stats.board(mode);
            if (rows.isEmpty()) { g.setFont(Ui.font(Font.PLAIN, 18)); g.setColor(Theme.dim()); Ui.center(g, "No scores yet for " + mode + " - be the first!", getWidth() / 2.0, 150); }
            Color[] medals = {new Color(255, 205, 60), new Color(200, 205, 215), new Color(215, 140, 80)};
            SimpleDateFormat fmt = new SimpleDateFormat("d MMM yyyy");
            for (int i = 0; i < rows.size(); i++) {
                String[] r = rows.get(i); int y = i * 33;
                if (r[0].equals(Theme.profile)) { g.setColor(Ui.alpha(Theme.accent(), 45)); g.fillRoundRect(0, y, getWidth(), 30, 14, 14); }
                g.setFont(Ui.font(Font.BOLD, 17)); g.setColor(i < 3 ? medals[i] : Theme.dim()); g.drawString("#" + (i + 1), 16, y + 21);
                g.setColor(Theme.fg()); g.drawString(r[0], 80, y + 21);
                g.setColor(Theme.accent()); g.drawString((int) Double.parseDouble(r[2]) + " wpm", 330, y + 21);
                g.setFont(Ui.font(Font.PLAIN, 15)); g.setColor(Theme.dim());
                g.drawString((int) Double.parseDouble(r[3]) + "% acc", 450, y + 21);
                g.drawString(fmt.format(new Date(Long.parseLong(r[4]))), 540, y + 21);
            }
            g.dispose();
        }
    }
}
