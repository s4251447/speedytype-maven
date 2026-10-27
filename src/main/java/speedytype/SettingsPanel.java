package speedytype;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Settings in three tabs (Gameplay / Look / More). Every change is saved immediately. */
final class SettingsPanel extends Ui.Screen {
    private final SpeedyType app;
    SettingsPanel(SpeedyType app) { this.app = app; build(); }
    /** Rebuilds the controls so they match the current profile's saved settings. */
    void refresh() { removeAll(); build(); revalidate(); repaint(); }

    private void build() {
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Settings", 40, Font.BOLD, true));
        CardLayout tabLayout = new CardLayout(); JPanel body = new JPanel(tabLayout);
        body.setOpaque(false); body.setPreferredSize(new Dimension(680, 270));
        body.add(gameplay(), "Gameplay"); body.add(look(), "Look"); body.add(more(), "More");

        JPanel tabs = Ui.row(); ButtonGroup group = new ButtonGroup();
        for (String t : new String[]{"Gameplay", "Look", "More"}) {
            Ui.Pill p = new Ui.Pill(t); p.setSelected(t.equals("Gameplay"));
            p.addActionListener(e -> tabLayout.show(body, t)); group.add(p); tabs.add(p);
        }
        card.put(tabs); card.put(body);
        Ui.Glow back = new Ui.Glow("Back"); back.addActionListener(e -> app.go("home"));
        card.put(back); put(card);
    }

    // ---- Tabs ----
    private JPanel gameplay() {
        JPanel p = column();
        p.add(Ui.row(label("Mode"), pills(Theme.MODES, Theme.mode, v -> Theme.mode = v)), Ui.gbc(10));
        p.add(Ui.row(label("Difficulty"), pills(Theme.LEVELS, Theme.level, v -> Theme.level = v)), Ui.gbc(10));
        p.add(Ui.row(label("Punctuation"), sw(Theme.punct, v -> Theme.punct = v), label("Numbers"), sw(Theme.numbers, v -> Theme.numbers = v),
                     label("Ghost"), sw(Theme.ghost, v -> Theme.ghost = v)), Ui.gbc(18));
        return p;
    }
    private JPanel look() {
        JPanel p = column();
        p.add(Ui.row(label("Theme"), themeSwatches()), Ui.gbc(6));
        p.add(fontPicker(), Ui.gbc(6));
        String[] sizes = {"Small", "Medium", "Large"}; int[] px = {24, 30, 38};
        int cur = 1; for (int i = 0; i < px.length; i++) if (px[i] == Theme.textSize) cur = i;
        p.add(Ui.row(label("Text size"), pills(sizes, sizes[cur], v -> { for (int i = 0; i < sizes.length; i++) if (sizes[i].equals(v)) Theme.textSize = px[i]; })), Ui.gbc(6));
        p.add(Ui.row(label("Caret"), pills(Theme.CARETS, Theme.caret, v -> Theme.caret = v)), Ui.gbc(6));
        p.add(Ui.row(label("Dark mode"), sw(Theme.dark, v -> Theme.dark = v)), Ui.gbc(6));
        return p;
    }
    private JPanel more() {
        JPanel p = column();
        p.add(Ui.row(label("Keyboard sounds"), sw(Theme.sound, v -> Theme.sound = v)), Ui.gbc(10));
        p.add(Ui.row(label("Focus mode (hide everything while typing)"), sw(Theme.focus, v -> Theme.focus = v)), Ui.gbc(10));
        Ui.Glow fs = new Ui.Glow("Toggle Fullscreen (F11)", 340); fs.addActionListener(e -> app.toggleFullscreen());
        p.add(fs, Ui.gbc(20));
        return p;
    }

    // ---- Small builders ----
    private JPanel column() { JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); return p; }
    private Ui.Label label(String t) { return new Ui.Label(t, 16, Font.PLAIN, false).dim(); }

    /** A row of selectable pills; picking one runs onPick and saves. */
    private JPanel pills(String[] options, String current, Consumer<String> onPick) {
        JPanel row = Ui.row(); ButtonGroup g = new ButtonGroup();
        for (String o : options) {
            Ui.Pill p = new Ui.Pill(o); p.setSelected(o.equals(current));
            p.addActionListener(e -> { onPick.accept(o); Theme.save(); }); g.add(p); row.add(p);
        }
        return row;
    }
    /** An on/off switch. */
    private Ui.Switch sw(boolean on, Consumer<Boolean> onChange) {
        Ui.Switch s = new Ui.Switch(on); s.addActionListener(e -> { onChange.accept(s.isSelected()); Theme.save(); }); return s;
    }
    private JPanel themeSwatches() {
        JPanel row = Ui.row(); ButtonGroup g = new ButtonGroup(); Map<String, Ui.Swatch> byName = new HashMap<>();
        for (Map.Entry<String, Color> en : Theme.ACCENTS.entrySet()) {
            Ui.Swatch s = new Ui.Swatch(en.getValue()); s.setSelected(en.getKey().equals(Theme.name));
            s.addActionListener(e -> { Theme.name = en.getKey(); Theme.save(); });
            g.add(s); row.add(s); byName.put(en.getKey(), s);
        }
        Ui.Swatch custom = new Ui.Swatch(null); custom.setSelected(Theme.name.equals("Custom"));
        custom.addActionListener(e -> {
            Color c = JColorChooser.showDialog(app, "Pick a color", Theme.accent());
            if (c != null) Theme.setCustom(c);
            else if (byName.containsKey(Theme.name)) byName.get(Theme.name).setSelected(true);   // cancelled: restore old pick
        });
        g.add(custom); row.add(custom);
        return row;
    }
    /** "< Font name >" selector that previews each installed monospaced font. */
    private JPanel fontPicker() {
        List<String> fonts = Theme.fonts();
        Ui.Label name = new Ui.Label(Theme.font, 20, Font.PLAIN, false);
        name.setPreferredSize(new Dimension(220, 40)); name.setFont(new Font(Theme.font, Font.PLAIN, 20));
        Ui.Glow prev = new Ui.Glow("<", 64, 46), next = new Ui.Glow(">", 64, 46);
        IntConsumer step = d -> {
            int i = Math.max(0, fonts.indexOf(Theme.font));
            Theme.font = fonts.get((i + d + fonts.size()) % fonts.size()); Theme.save();
            name.setText(Theme.font); name.setFont(new Font(Theme.font, Font.PLAIN, 20));
        };
        prev.addActionListener(e -> step.accept(-1)); next.addActionListener(e -> step.accept(1));
        return Ui.row(label("Font"), prev, name, next);
    }
}
