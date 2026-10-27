package speedytype;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/** The app icon (a gradient keycap with an "S"), drawn in code at several sizes. */
final class AppIcon {
    static List<Image> images() {
        List<Image> l = new ArrayList<>();
        for (int s : new int[]{16, 32, 64, 128, 256}) l.add(draw(s));
        return l;
    }
    private static BufferedImage draw(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = Ui.smooth(img.getGraphics());
        int arc = (int) (s * 0.24), m = (int) (s * 0.2);
        g.setPaint(new GradientPaint(0, 0, new Color(64, 156, 255), s, s, new Color(170, 110, 255)));
        g.fillRoundRect(0, 0, s, s, arc, arc);                                              // rounded background
        g.setColor(new Color(255, 255, 255, 235));
        g.fillRoundRect(m, m, s - 2 * m, (int) (s * 0.56), (int) (s * 0.14), (int) (s * 0.14)); // keycap
        g.setColor(new Color(40, 50, 110)); g.setFont(new Font("SansSerif", Font.BOLD, (int) (s * 0.42)));
        Ui.center(g, "S", s / 2.0, m + s * 0.28);
        g.setColor(new Color(255, 255, 255, 220));
        g.fillRoundRect((int) (s * 0.3), (int) (s * 0.83), (int) (s * 0.4), Math.max(2, (int) (s * 0.06)), 4, 4);   // caret bar
        g.dispose();
        return img;
    }
}
