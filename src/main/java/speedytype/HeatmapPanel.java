package speedytype;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/** Keyboard heatmap: keys you miss more often glow redder. Built from every test you've finished. */
final class HeatmapPanel extends Ui.Screen {
    private static final int MIN_ATTEMPTS = 5;                     // ignore keys with too little data
    private final Ui.Label weakest = new Ui.Label("", 17, Font.PLAIN, false);
    private Map<Character, int[]> data = new TreeMap<>();
    private final JComponent board = new JComponent() {
        { setPreferredSize(new Dimension(680, 190)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = Ui.smooth(g0); String[] rows = {"qwertyuiop", "asdfghjkl", "zxcvbnm"}; int[] offset = {0, 30, 90};
            int x0 = (getWidth() - 10 * 62) / 2;
            for (int r = 0; r < rows.length; r++)
                for (int c = 0; c < rows[r].length(); c++) {
                    char k = rows[r].charAt(c); int x = x0 + offset[r] + c * 62, y = r * 62; int[] a = data.get(k);
                    Ui.glass(g, x, y, 54, 54, 14);
                    if (a != null && a[0] >= MIN_ATTEMPTS) {
                        double rate = a[1] / (double) a[0];
                        g.setColor(Ui.alpha(Color.getHSBColor((float) (0.33 * (1 - Math.min(1, rate * 4))), 0.75f, 0.95f), 190));   // green -> red
                        g.fillRoundRect(x, y, 54, 54, 14, 14);
                        g.setFont(Ui.font(Font.PLAIN, 10)); g.setColor(Color.WHITE); Ui.center(g, (int) Math.round(rate * 100) + "%", x + 27, y + 43);
                    }
                    g.setFont(Ui.font(Font.BOLD, 20)); g.setColor(a != null && a[0] >= MIN_ATTEMPTS ? Color.WHITE : Theme.fg());
                    Ui.center(g, String.valueOf(Character.toUpperCase(k)), x + 27, y + 22);
                }
            g.dispose();
        }
    };

    HeatmapPanel(SpeedyType app) {
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Keyboard Heatmap", 40, Font.BOLD, true));
        card.put(new Ui.Label("Miss rate per key across all your tests (green = clean, red = trouble)", 15, Font.PLAIN, false).dim());
        card.put(board); card.put(weakest);
        Ui.Glow back = new Ui.Glow("Back"); back.addActionListener(e -> app.go("stats"));
        card.put(back); put(card);
    }

    void refresh() {
        data = Stats.keys();
        List<Map.Entry<Character, int[]>> list = new ArrayList<>();
        for (Map.Entry<Character, int[]> e : data.entrySet()) if (e.getValue()[0] >= MIN_ATTEMPTS && e.getValue()[1] > 0) list.add(e);
        list.sort((a, b) -> Double.compare(b.getValue()[1] / (double) b.getValue()[0], a.getValue()[1] / (double) a.getValue()[0]));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            Map.Entry<Character, int[]> e = list.get(i);
            sb.append(i > 0 ? "    " : "").append(Character.toUpperCase(e.getKey())).append(" ").append(Math.round(100.0 * e.getValue()[1] / e.getValue()[0])).append("%");
        }
        weakest.setText(sb.length() == 0 ? "Finish a few tests to see your weakest keys" : "Weakest keys:   " + sb);
        board.repaint();
    }
}
