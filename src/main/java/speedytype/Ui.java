package speedytype;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/** Shared drawing helpers and the custom widgets (buttons, cards, switches...) used by every screen. */
final class Ui {
    /** Animation clock in seconds, advanced by the main window ~30 times per second. */
    static double t = 0;

    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }
    static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a))); }
    static Font font(int style, int size) { return new Font("SansSerif", style, size); }
    /** Draws text centered on (cx, cy) using the graphics' current font. */
    static void center(Graphics2D g, String s, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0));
    }
    /** A rounded translucent "glass" rectangle. */
    static void glass(Graphics2D g, int x, int y, int w, int h, int arc) {
        g.setColor(Theme.card()); g.fillRoundRect(x, y, w, h, arc, arc);
        g.setColor(Theme.line()); g.drawRoundRect(x, y, w, h, arc, arc);
    }
    /** A small stat box: dim caption on top, big value underneath. */
    static void tile(Graphics2D g, int x, int y, int w, int h, String label, String value) {
        glass(g, x, y, w, h, 20);
        g.setFont(font(Font.BOLD, 12)); g.setColor(Theme.dim()); center(g, label, x + w / 2.0, y + h * 0.28);
        g.setFont(font(Font.BOLD, 26)); g.setColor(Theme.fg()); center(g, value, x + w / 2.0, y + h * 0.66);
    }
    /** Animated gradient background with slowly drifting glow orbs. */
    static void backdrop(Graphics g0, int w, int h) {
        Graphics2D g = smooth(g0);
        g.setPaint(new GradientPaint(0, 0, Theme.bg1(), w, h, Theme.bg2())); g.fillRect(0, 0, w, h);
        Color[] cols = {Theme.accent(), Theme.accent2(), Theme.accent()};
        for (int i = 0; i < 3; i++) {
            double x = w * (0.5 + 0.4 * Math.sin(t * 0.25 + i * 2.1)), y = h * (0.5 + 0.4 * Math.cos(t * 0.2 + i * 1.7));
            float r = Math.max(w, h) * 0.45f;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), r, new float[]{0, 1},
                new Color[]{alpha(cols[i], Theme.dark ? 60 : 50), alpha(cols[i], 0)}));
            g.fill(new Rectangle2D.Double(x - r, y - r, 2 * r, 2 * r));
        }
        g.dispose();
    }
    static GridBagConstraints gbc(int top) {
        GridBagConstraints k = new GridBagConstraints(); k.gridx = 0; k.insets = new Insets(top, 0, 8, 0); return k;
    }
    /** A transparent horizontal strip of components. */
    static JPanel row(Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0)); p.setOpaque(false);
        for (Component c : cs) p.add(c);
        return p;
    }
    private static void plain(AbstractButton b) {
        b.setRolloverEnabled(true); b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false); b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /** Base class of every screen: paints the animated backdrop and centers its content. */
    static class Screen extends JPanel {
        Screen() { super(new GridBagLayout()); }
        void put(Component c) { put(c, 8); }
        void put(Component c, int top) { add(c, gbc(top)); }
        @Override protected void paintComponent(Graphics g) { backdrop(g, getWidth(), getHeight()); }
    }

    /** A glass panel that stacks its children vertically. */
    static class Card extends JPanel {
        Card() { super(new GridBagLayout()); setOpaque(false); setBorder(BorderFactory.createEmptyBorder(28, 48, 28, 48)); }
        void put(Component c) { add(c, gbc(8)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = smooth(g0); glass(g, 0, 0, getWidth() - 1, getHeight() - 1, 34); g.dispose();
        }
    }

    /** Text label. gradient = accent-colored title, dim() = muted subtitle. */
    static class Label extends JLabel {
        private final boolean gradient; private boolean dim;
        Label(String text, int size, int style, boolean gradient) { super(text, CENTER); this.gradient = gradient; setFont(font(style, size)); }
        Label dim() { dim = true; return this; }
        @Override public Color getForeground() { return dim ? Theme.dim() : Theme.fg(); }
        @Override protected void paintComponent(Graphics g0) {
            if (!gradient) { super.paintComponent(g0); return; }
            Graphics2D g = smooth(g0); g.setFont(getFont()); FontMetrics fm = g.getFontMetrics(); int w = fm.stringWidth(getText());
            g.setPaint(new GradientPaint((getWidth() - w) / 2f, 0, Theme.accent(), (getWidth() + w) / 2f, 0, Theme.accent2()));
            center(g, getText(), getWidth() / 2.0, getHeight() / 2.0); g.dispose();
        }
    }

    /** Main button: gradient body, soft glow and a little lift on hover. */
    static class Glow extends JButton {
        private float hover = 0;
        Glow(String text) { this(text, 270); }
        Glow(String text, int width) { this(text, width, 62); }
        Glow(String text, int width, int height) {
            super(text); setFont(font(Font.BOLD, 20)); plain(this); setPreferredSize(new Dimension(width, height));
        }
        @Override protected void paintComponent(Graphics g0) {
            hover += ((getModel().isRollover() ? 1f : 0f) - hover) * 0.25f;
            Graphics2D g = smooth(g0); int w = getWidth(), h = getHeight(), p = 6;
            for (int i = 3; i > 0; i--) {                       // glow halo
                g.setColor(alpha(Theme.accent(), (int) (10 + 22 * hover)));
                g.fillRoundRect(p - i * 2, p - i * 2, w - 2 * p + i * 4, h - 2 * p + i * 4, 30 + i * 4, 30 + i * 4);
            }
            RoundRectangle2D body = new RoundRectangle2D.Double(p, p - hover * 2, w - 2 * p, h - 2 * p, 26, 26);
            g.setPaint(new GradientPaint(0, 0, Theme.accent(), w, 0, Theme.accent2())); g.fill(body);
            g.setColor(alpha(Color.WHITE, (int) (40 * hover))); g.fill(body);
            g.setColor(Color.WHITE); g.setFont(getFont()); center(g, getText(), w / 2.0, h / 2.0 - hover * 2); g.dispose();
        }
    }

    /** Selectable pill (used for the mode picker). */
    static class Pill extends JToggleButton {
        Pill(String text) {
            super(text); setFont(font(Font.BOLD, 15)); plain(this);
            setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(text) + 40, 38));
        }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = smooth(g0); int w = getWidth(), h = getHeight();
            if (isSelected()) { g.setPaint(new GradientPaint(0, 0, Theme.accent(), w, 0, Theme.accent2())); g.fillRoundRect(0, 0, w, h, h, h); g.setColor(Color.WHITE); }
            else { glass(g, 0, 0, w - 1, h - 1, h); g.setColor(Theme.fg()); }
            g.setFont(getFont()); center(g, getText(), w / 2.0, h / 2.0); g.dispose();
        }
    }

    /** Round color swatch for the theme picker. A null color makes the "+ custom" swatch. */
    static class Swatch extends JToggleButton {
        private final Color color;
        Swatch(Color color) { this.color = color; plain(this); setPreferredSize(new Dimension(36, 36)); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = smooth(g0);
            g.setColor(color != null ? color : (isSelected() ? Theme.accent() : Theme.dim())); g.fillOval(4, 4, 28, 28);
            if (color == null) { g.setColor(Color.WHITE); g.setFont(font(Font.BOLD, 20)); center(g, "+", 18, 18); }
            if (isSelected()) { g.setColor(Theme.fg()); g.setStroke(new BasicStroke(2.5f)); g.drawOval(1, 1, 33, 33); }
            g.dispose();
        }
    }

    /** Animated on/off switch. */
    static class Switch extends JToggleButton {
        private float pos;
        Switch(boolean on) { setSelected(on); pos = on ? 1 : 0; plain(this); setPreferredSize(new Dimension(58, 32)); }
        @Override protected void paintComponent(Graphics g0) {
            pos += ((isSelected() ? 1f : 0f) - pos) * 0.3f;
            Graphics2D g = smooth(g0); int w = getWidth(), h = getHeight();
            if (isSelected()) { g.setPaint(new GradientPaint(0, 0, Theme.accent(), w, 0, Theme.accent2())); g.fillRoundRect(0, 0, w, h, h, h); }
            else glass(g, 0, 0, w - 1, h - 1, h);
            g.setColor(Color.WHITE); g.fillOval(3 + (int) (pos * (w - h)), 3, h - 6, h - 6); g.dispose();
        }
    }
}
