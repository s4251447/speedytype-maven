package speedytype;

import javax.swing.*;
import java.awt.*;

/** Create or switch profiles. Each profile keeps its own settings, stats, bests, heatmap and achievements. */
final class ProfilePanel extends Ui.Screen {
    private final SpeedyType app;
    private final JTextField field = new JTextField(14);
    private final Ui.Label current = new Ui.Label("", 18, Font.PLAIN, false).dim();
    private final JPanel list = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));

    ProfilePanel(SpeedyType app) {
        this.app = app;
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Profiles", 40, Font.BOLD, true));
        card.put(current);
        list.setOpaque(false); list.setPreferredSize(new Dimension(560, 96));
        card.put(list);
        card.put(new Ui.Label("Type a new name to start fresh, or an existing name to bring its data back", 14, Font.PLAIN, false).dim());
        field.setFont(Ui.font(Font.PLAIN, 20)); field.setHorizontalAlignment(JTextField.CENTER); field.setPreferredSize(new Dimension(260, 46));
        field.addActionListener(e -> submit());
        Ui.Glow go = new Ui.Glow("Create / Switch", 300), back = new Ui.Glow("Back");
        go.addActionListener(e -> submit()); back.addActionListener(e -> app.go("home"));
        card.put(field); card.put(go); card.put(back);
        put(card);
    }

    private void submit() {
        if (field.getText().trim().isEmpty()) return;
        Theme.load(field.getText()); field.setText(""); app.go("home");        // same name = old data returns, new name = fresh start
    }

    /** Rebuilds the list of existing profiles. */
    void refresh() {
        current.setText("Playing as " + Theme.profile);
        list.removeAll(); ButtonGroup g = new ButtonGroup();
        for (String name : Theme.profiles()) {
            Ui.Pill p = new Ui.Pill(name); p.setSelected(name.equals(Theme.profile));
            p.addActionListener(e -> { Theme.load(name); app.go("home"); }); g.add(p); list.add(p);
        }
        field.setBackground(Theme.dark ? new Color(28, 30, 52) : Color.WHITE); field.setForeground(Theme.fg()); field.setCaretColor(Theme.accent());
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.accent(), 2, true), BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        list.revalidate(); list.repaint();
    }
}
