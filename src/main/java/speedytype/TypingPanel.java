package speedytype;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/** The typing test: shows the text, reads keystrokes, tracks speed/accuracy, then hands off to ResultPanel. */
final class TypingPanel extends Ui.Screen {
    private final SpeedyType app;

    // ---- Test state ----
    private String target = "";                        // the text the user must type
    private final StringBuilder typed = new StringBuilder();
    private long start = 0, end = 0;                   // ms timestamps (0 = not started yet)
    private int limit = 0;                             // seconds for Time modes, 0 otherwise
    private int keys = 0, mistakes = 0;                // all keystrokes / wrong keystrokes (even if later corrected)
    private boolean tabPressed = false, finished = false;
    private double ghostWpm = 0;                       // pace of the ghost caret (0 = no ghost)
    private final Map<Character, int[]> runKeys = new HashMap<>();   // letter -> {attempts, mistakes} for the heatmap
    private final List<Integer> samples = new ArrayList<>();         // WPM once per second, for the result graph
    private final javax.swing.Timer sampler = new javax.swing.Timer(1000, e -> sample());

    // ---- Visual state ----
    private double caretX, caretY, shownWpm;
    private boolean snapCaret = true;
    private final List<double[]> sparks = new ArrayList<>();         // {x, y, vx, vy, life}

    TypingPanel(SpeedyType app) {
        this.app = app;
        setFocusable(true); setFocusTraversalKeysEnabled(false);    // so Tab reaches us instead of moving focus
        addMouseListener(new MouseAdapter() { @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); } });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_TAB) { tabPressed = true; return; }
                if (k == KeyEvent.VK_ENTER && tabPressed) { reset(); return; }        // Tab, then Enter = restart
                if (k == KeyEvent.VK_ESCAPE) { sampler.stop(); app.go("home"); return; }
                tabPressed = false;
            }
            @Override public void keyTyped(KeyEvent e) { onChar(e.getKeyChar()); }
        });
    }

    /** Starts a fresh test using the current settings. */
    void reset() {
        sampler.stop(); typed.setLength(0); start = end = 0; keys = mistakes = 0; finished = false; limit = 0;
        samples.clear(); sparks.clear(); runKeys.clear(); shownWpm = 0; snapCaret = true;
        double best = Stats.best(Theme.mode);                        // ghost runs at your personal best, or your goal
        ghostWpm = !Theme.ghost ? 0 : best > 0 ? best : Theme.goal;
        String m = Theme.mode;
        if (m.startsWith("Time")) { limit = Integer.parseInt(m.substring(5)); target = Sentences.words(50); }
        else if (m.startsWith("Words")) target = Sentences.words(25);
        else target = Sentences.sentence();
        requestFocusInWindow(); repaint();
    }

    /** Handles one typed character (or Backspace). */
    private void onChar(char ch) {
        if (finished) return;
        if (ch == '\b') { if (typed.length() > 0) typed.setLength(typed.length() - 1); return; }
        if (ch < 32 || typed.length() >= target.length()) return;
        if (start == 0) { start = System.currentTimeMillis(); sampler.start(); }      // timer starts on the first key
        char want = target.charAt(typed.length()); boolean ok = ch == want;
        keys++; if (!ok) mistakes++;
        if (Character.isLetter(want)) {                                                // remember per-key accuracy
            int[] a = runKeys.computeIfAbsent(Character.toLowerCase(want), k -> new int[2]); a[0]++; if (!ok) a[1]++;
        }
        typed.append(ch);
        if (ok) Sound.click(); else Sound.error();
        if (ok) for (int i = 0; i < 4; i++) sparks.add(new double[]{caretX, caretY - 10, (Sentences.RND.nextDouble() - 0.5) * 4, -1 - Sentences.RND.nextDouble() * 3, 1});
        if (limit > 0 && typed.length() > target.length() - 60) target += " " + Sentences.words(20);   // endless text in Time modes
        if (limit == 0 && typed.length() == target.length()) finish();
    }

    // ---- Scoring ----
    private long elapsed() { return start == 0 ? 0 : (end > 0 ? end : System.currentTimeMillis()) - start; }
    private int correct() { int n = 0; for (int i = 0; i < typed.length(); i++) if (typed.charAt(i) == target.charAt(i)) n++; return n; }
    private double minutes() { return Math.max(elapsed(), 1000) / 60000.0; }
    private double wpm() { return start == 0 ? 0 : correct() / 5.0 / minutes(); }        // "net" speed: only correct characters
    private double rawWpm() { return start == 0 ? 0 : typed.length() / 5.0 / minutes(); }

    private void sample() {
        samples.add((int) wpm());
        if (limit > 0 && elapsed() >= limit * 1000L - 50) finish();
    }

    /** Ends the run: saves stats, keys, leaderboard entry and achievements, then shows the results. */
    private void finish() {
        if (finished) return;
        finished = true; sampler.stop();
        end = limit > 0 ? start + limit * 1000L : System.currentTimeMillis();
        double w = wpm(), raw = rawWpm(), acc = keys == 0 ? 100 : 100.0 * (keys - mistakes) / keys;
        samples.add((int) w);
        boolean beat = Theme.goal > 0 && w >= Theme.goal;
        double prev = Stats.addRun(Theme.mode, w, raw, acc);
        Stats.addKeys(runKeys);
        List<String> unlocked = Achievements.check(w, acc, Theme.mode, beat);
        String note = w > prev ? (prev > 0 ? "New personal best!" : "First run recorded!") : "Personal best: " + (int) prev + " wpm";
        if (beat || w > prev || !unlocked.isEmpty()) Sound.win();
        app.result.show(w, raw, acc, note, new ArrayList<>(samples), unlocked);
        app.go("result");
    }

    // ---- Drawing ----
    private void header(Graphics2D g, int w) {
        g.setFont(Ui.font(Font.BOLD, 56));
        g.setPaint(new GradientPaint(60, 0, Theme.accent(), 200, 0, Theme.accent2()));
        g.drawString(String.valueOf((int) shownWpm), 60, 82);
        g.setFont(Ui.font(Font.PLAIN, 16)); g.setColor(Theme.dim()); g.drawString("wpm", 62, 106);
        String right = limit > 0 ? Math.max(0, limit - (int) (elapsed() / 1000)) + "s" : Theme.mode;
        g.setFont(Ui.font(Font.BOLD, 30)); g.setColor(Theme.fg());
        g.drawString(right, w - 60 - g.getFontMetrics().stringWidth(right), 78);
        String info = (Theme.goal > 0 ? "Goal " + Theme.goal + " wpm" : "") + (ghostWpm > 0 ? "   Ghost " + (int) ghostWpm : "");
        g.setFont(Ui.font(Font.PLAIN, 16)); g.setColor(Theme.dim());
        g.drawString(info, w - 60 - g.getFontMetrics().stringWidth(info), 106);
    }

    /** Draws a caret at (x, baseline y) in the user's chosen style. The color's alpha controls blinking/fading. */
    private void drawCaret(Graphics2D g, double x, double y, FontMetrics fm, Color c) {
        int cw = fm.charWidth('m'), top = (int) y - fm.getAscent();
        switch (Theme.caret) {
            case "Block": g.setColor(Ui.alpha(c, c.getAlpha() / 3)); g.fillRoundRect((int) x, top, cw, fm.getHeight(), 8, 8); break;
            case "Underline": g.setColor(c); g.fillRoundRect((int) x, (int) y + 5, cw, 4, 3, 3); break;
            default:
                g.setColor(Ui.alpha(c, c.getAlpha() / 4)); g.fillRoundRect((int) x - 4, top, 10, fm.getHeight(), 8, 8);
                g.setColor(c); g.fillRoundRect((int) x - 1, top + 2, 3, fm.getHeight() - 4, 3, 3);
        }
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);                                    // animated backdrop
        Graphics2D g = Ui.smooth(g0); int w = getWidth(), h = getHeight();
        boolean quiet = Theme.focus && start != 0 && !finished;      // focus mode: hide everything except the text while typing
        if (quiet && Theme.dark) { g.setColor(new Color(0, 0, 0, 130)); g.fillRect(0, 0, w, h); }
        shownWpm += (wpm() - shownWpm) * 0.15;
        if (!quiet) header(g, w);

        int cx = 60, cy = 130, cw = w - 120, ch = h - 130 - 90;
        Ui.glass(g, cx, cy, cw, ch, 34);
        int n = target.length(); if (n == 0) { g.dispose(); return; }
        g.setFont(new Font(Theme.font, Font.PLAIN, Theme.textSize)); FontMetrics fm = g.getFontMetrics();

        // Lay out characters, wrapping at word boundaries
        int[] px = new int[n], line = new int[n]; int x = 0, ln = 0, i = 0, maxW = cw - 80, lh = fm.getHeight() + 14;
        for (String word : target.split("(?<= )")) {
            if (x + fm.stringWidth(word) > maxW && x > 0) { x = 0; ln++; }
            for (char c : word.toCharArray()) { px[i] = x; line[i] = ln; x += fm.charWidth(c); i++; }
        }
        int caretIdx = Math.min(typed.length(), n - 1);
        int first = Math.max(0, line[caretIdx] - 1), rows = Math.max(1, (ch - 80) / lh);       // scroll so the caret stays in view
        int left = cx + 40, top = cy + 30 + fm.getAscent();

        for (i = 0; i < n; i++) {
            if (line[i] < first || line[i] >= first + rows) continue;
            char c = target.charAt(i); int y = top + (line[i] - first) * lh, cwid = fm.charWidth(c);
            if (i < typed.length()) {
                boolean ok = typed.charAt(i) == c;
                if (!ok) { g.setColor(new Color(255, 90, 110, 70)); g.fillRoundRect(left + px[i], y - fm.getAscent(), cwid, fm.getHeight(), 6, 6); }
                g.setColor(ok ? Theme.fg() : new Color(255, 100, 120));
            } else g.setColor(Theme.dim());
            g.drawString(String.valueOf(c), left + px[i], y);
        }

        // Ghost caret: runs at your personal-best pace so you can race it
        if (ghostWpm > 0 && start != 0 && !finished) {
            int gi = Math.min(n - 1, (int) (ghostWpm * 5 * elapsed() / 60000.0));
            if (line[gi] >= first && line[gi] < first + rows) drawCaret(g, left + px[gi], top + (line[gi] - first) * lh, fm, Ui.alpha(Theme.accent2(), 170));
        }

        // Your caret glides smoothly (and blinks while waiting for the first key)
        double tx = left + (typed.length() < n ? px[caretIdx] : px[n - 1] + fm.charWidth(target.charAt(n - 1)));
        double ty = top + (line[caretIdx] - first) * lh;
        if (snapCaret) { caretX = tx; caretY = ty; snapCaret = false; }
        caretX += (tx - caretX) * 0.4; caretY += (ty - caretY) * 0.4;
        drawCaret(g, caretX, caretY, fm, Ui.alpha(Theme.accent(), start == 0 ? (int) (110 + 145 * Math.abs(Math.sin(Ui.t * 4))) : 255));

        // Sparks fly off the caret on correct keys
        for (Iterator<double[]> it = sparks.iterator(); it.hasNext(); ) {
            double[] s = it.next(); s[0] += s[2]; s[1] += s[3]; s[3] += 0.25; s[4] -= 0.06;
            if (s[4] <= 0) { it.remove(); continue; }
            g.setColor(Ui.alpha(Theme.accent2(), (int) (255 * s[4]))); g.fillOval((int) s[0], (int) s[1], 5, 5);
        }

        if (!quiet) {
            // Progress bar (time elapsed in Time modes, characters typed otherwise)
            double frac = limit > 0 ? Math.min(1, elapsed() / (limit * 1000.0)) : typed.length() / (double) n;
            int bx = cx + 40, by = cy + ch - 26, bw = cw - 80;
            g.setColor(Theme.line()); g.fillRoundRect(bx, by, bw, 6, 6, 6);
            g.setPaint(new GradientPaint(bx, 0, Theme.accent(), bx + bw, 0, Theme.accent2())); g.fillRoundRect(bx, by, (int) (bw * frac), 6, 6, 6);
            g.setFont(Ui.font(Font.PLAIN, 14)); g.setColor(Theme.dim());
            Ui.center(g, "TAB + ENTER  restart        ESC  home        F11  fullscreen", w / 2.0, h - 42);
        }

        // Lost focus? Dim the card and say how to get it back.
        if (!hasFocus()) {
            g.setColor(new Color(0, 0, 0, Theme.dark ? 150 : 90)); g.fillRoundRect(cx, cy, cw, ch, 34, 34);
            g.setFont(Ui.font(Font.BOLD, 26)); g.setColor(Color.WHITE); Ui.center(g, "Click here to focus", w / 2.0, cy + ch / 2.0);
        }
        g.dispose();
    }
}
