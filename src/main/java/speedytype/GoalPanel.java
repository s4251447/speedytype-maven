package speedytype;

import java.awt.*;

/** Lets the user pick a target speed (words per minute) with - / + buttons. */
final class GoalPanel extends Ui.Screen {
    private int value = 40;
    private final Ui.Label number = new Ui.Label("", 96, Font.BOLD, true);

    GoalPanel(SpeedyType app) {
        Ui.Card card = new Ui.Card();
        card.put(new Ui.Label("Set Your Goal", 40, Font.BOLD, true));
        card.put(new Ui.Label("Target speed in words per minute", 17, Font.PLAIN, false).dim());
        Ui.Glow minus = new Ui.Glow("-5", 90), plus = new Ui.Glow("+5", 90);
        minus.addActionListener(e -> change(-5));
        plus.addActionListener(e -> change(5));
        number.setPreferredSize(new Dimension(200, 110));
        card.put(Ui.row(minus, number, plus));

        Ui.Glow save = new Ui.Glow("Save"), back = new Ui.Glow("Back");
        save.addActionListener(e -> { Theme.goal = value; Theme.save(); app.go("home"); });
        back.addActionListener(e -> app.go("home"));
        card.put(save); card.put(back);
        put(card);
    }
    private void change(int d) { value = Math.max(5, Math.min(300, value + d)); number.setText(String.valueOf(value)); }
    /** Called each time the screen opens so it always shows the saved goal. */
    void refresh() { value = Theme.goal > 0 ? Theme.goal : 40; number.setText(String.valueOf(value)); }
}
