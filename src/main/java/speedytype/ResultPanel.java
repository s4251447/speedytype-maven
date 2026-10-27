package speedytype;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/** Shows the finished run: animated WPM, stat tiles, an area chart over time, and confetti if the goal was beaten. */
final class ResultPanel extends Ui.Screen {
    private final SpeedyType app;
    private double wpm, raw, acc, shown;                       // shown = count-up animation value
    private String note = "";
    private List<String> unlocked = new ArrayList<>();
    private List<Integer> samples = new ArrayList<>();
    private boolean beat;
    private final List<double[]> confetti = new ArrayList<>(); // {x, y, vx, vy, hue, size}
    private javax.swing.Timer goalTimer;                       // pending "set a new goal?" popup
    private final Ui.Glow again = new Ui.Glow("Play Again"), home = new Ui.Glow("Home");

    ResultPanel(SpeedyType app) {
        this.app = app;
        setLayout(null); add(again); add(home);
        again.addActionListener(e -> app.go("game"));
        home.addActionListener(e -> app.go("home"));
    }

    /** Called by TypingPanel when a run ends. */
    void show(double wpm, double raw, double acc, String note, List<Integer> samples, List<String> unlocked) {
        cancel(); this.unlocked = unlocked;
        this.wpm = wpm; this.raw = raw; this.acc = acc; this.note = note; this.samples = samples; shown = 0;
        beat = Theme.goal > 0 && wpm >= Theme.goal;
        if (!beat) return;
        for (int i = 0; i < 260; i++) confetti.add(new double[]{Sentences.RND.nextInt(app.width()), -Sentences.RND.nextInt(600),
            Sentences.RND.nextDouble() * 4 - 2, 2 + Sentences.RND.nextDouble() * 4, Sentences.RND.nextInt(360), 6 + Sentences.RND.nextInt(6)});
        goalTimer = new javax.swing.Timer(3000, e -> {
            if (JOptionPane.showConfirmDialog(app, "You beat your goal of " + Theme.goal + " wpm! Set a new goal?",
                    "Goal smashed!", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) app.go("goal");
        });
        goalTimer.setRepeats(false); goalTimer.start();
    }
    /** Stops confetti and the pending goal popup (called whenever we leave this screen). */
    void cancel() { confetti.clear(); if (goalTimer != null) goalTimer.stop(); }

    @Override public void doLayout() {
        again.setBounds(getWidth() / 2 - 285, getHeight() - 84, 270, 62);
        home.setBounds(getWidth() / 2 + 15, getHeight() - 84, 270, 62);
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = Ui.smooth(g0); int w = getWidth(), h = getHeight();

        // Big counting-up WPM number
        shown += (wpm - shown) * 0.08;
        String num = String.valueOf((int) Math.round(shown));
        g.setFont(Ui.font(Font.BOLD, 88)); int nw = g.getFontMetrics().stringWidth(num);
        g.setPaint(new GradientPaint((w - nw) / 2f, 0, Theme.accent(), (w + nw) / 2f, 0, Theme.accent2()));
        Ui.center(g, num, w / 2.0, 70);
        g.setFont(Ui.font(Font.PLAIN, 16)); g.setColor(Theme.dim()); Ui.center(g, "words per minute", w / 2.0, 122);
        g.setFont(Ui.font(Font.BOLD, 20)); g.setColor(note.startsWith("New") || beat ? Theme.accent() : Theme.fg());
        Ui.center(g, note + (Theme.goal > 0 ? "   |   Goal " + Theme.goal + (beat ? " reached!" : " not yet") : ""), w / 2.0, 155);
        if (!unlocked.isEmpty()) { g.setFont(Ui.font(Font.BOLD, 14)); g.setColor(Theme.accent2()); Ui.center(g, "Achievement unlocked: " + String.join(", ", unlocked), w / 2.0, 172); }

        // Stat tiles
        int tw = 190, gap = 20, tx = (w - (3 * tw + 2 * gap)) / 2;
        Ui.tile(g, tx, 180, tw, 74, "RAW", (int) raw + " wpm");
        Ui.tile(g, tx + tw + gap, 180, tw, 74, "ACCURACY", String.format("%.0f%%", acc));
        Ui.tile(g, tx + 2 * (tw + gap), 180, tw, 74, "MODE", Theme.mode);

        // Area chart of WPM over time
        int cx = 80, cy = 274, cw = w - 160, ch = h - 274 - 100;
        Ui.glass(g, cx, cy, cw, ch, 28);
        int px = cx + 30, pw = cw - 60, py = cy + 24, ph = ch - 48, n = samples.size(), max = 10;
        for (int s : samples) max = Math.max(max, s);
        if (Theme.goal > 0) max = Math.max(max, Theme.goal);
        max = (int) (max * 1.15);
        if (Theme.goal > 0) {                                        // dashed goal line
            int y = py + ph - ph * Theme.goal / max;
            g.setColor(new Color(255, 200, 60)); g.setStroke(new BasicStroke(1.5f, 0, 0, 1f, new float[]{7, 7}, 0)); g.drawLine(px, y, px + pw, y);
            g.setStroke(new BasicStroke(1f)); g.setFont(Ui.font(Font.BOLD, 12)); g.drawString("GOAL", px + pw - 34, y - 6);
        }
        double[] xs = new double[n], ys = new double[n];
        for (int i = 0; i < n; i++) { xs[i] = px + (n == 1 ? pw / 2.0 : pw * i / (double) (n - 1)); ys[i] = py + ph - ph * samples.get(i) / (double) max; }
        if (n > 1) {
            Path2D.Double area = new Path2D.Double(); area.moveTo(xs[0], py + ph);
            for (int i = 0; i < n; i++) area.lineTo(xs[i], ys[i]);
            area.lineTo(xs[n - 1], py + ph); area.closePath();
            g.setPaint(new GradientPaint(0, py, Ui.alpha(Theme.accent(), 110), 0, py + ph, Ui.alpha(Theme.accent(), 0))); g.fill(area);
            g.setPaint(new GradientPaint(px, 0, Theme.accent(), px + pw, 0, Theme.accent2())); g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 1; i < n; i++) g.draw(new Line2D.Double(xs[i - 1], ys[i - 1], xs[i], ys[i]));
        }
        g.setStroke(new BasicStroke(1f)); g.setColor(Theme.fg());
        for (int i = 0; i < n; i++) g.fillOval((int) xs[i] - 4, (int) ys[i] - 4, 8, 8);
        g.setFont(Ui.font(Font.PLAIN, 12)); g.setColor(Theme.dim()); g.drawString("WPM over time (seconds)", cx + 30, cy + ch - 8);

        // Confetti (moved a little every frame)
        for (double[] c : confetti) {
            c[0] += c[2]; c[1] += c[3]; if (c[1] > h + 20) { c[1] = -10; c[0] = Sentences.RND.nextInt(Math.max(w, 1)); }
            g.setColor(Color.getHSBColor((float) (c[4] / 360), 0.75f, 1f)); g.fillRect((int) c[0], (int) c[1], (int) c[5], (int) c[5] / 2 + 2);
        }
        g.dispose();
    }
}
