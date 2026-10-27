package speedytype;

import java.util.*;

/** Achievement definitions and the check that runs after every finished test. */
final class Achievements {
    /** {id, title, description} */
    static final String[][] ALL = {
        {"first", "First Steps", "Finish your first test"},
        {"wpm30", "Warming Up", "Reach 30 WPM"},
        {"wpm50", "Getting Fast", "Reach 50 WPM"},
        {"wpm80", "Speed Demon", "Reach 80 WPM"},
        {"wpm100", "Centurion", "Reach 100 WPM"},
        {"perfect", "Flawless", "Finish with 100% accuracy"},
        {"runs10", "Regular", "Complete 10 tests"},
        {"runs50", "Dedicated", "Complete 50 tests"},
        {"goal", "Goal Getter", "Beat your goal"},
        {"marathon", "Marathon", "Finish a 60 second test"},
        {"hard", "Bring It On", "Reach 40 WPM on Hard"}
    };

    static Set<String> unlocked() {
        Set<String> s = new LinkedHashSet<>();
        for (String x : Theme.P.get("ach", "").split(",")) if (!x.isEmpty()) s.add(x);
        return s;
    }
    /** Call after the run has been saved. Returns the titles of achievements unlocked just now. */
    static List<String> check(double wpm, double acc, String mode, boolean beatGoal) {
        Set<String> have = unlocked(); List<String> fresh = new ArrayList<>(); int runs = Stats.runs();
        grant(have, fresh, "first", true);
        grant(have, fresh, "wpm30", wpm >= 30);   grant(have, fresh, "wpm50", wpm >= 50);
        grant(have, fresh, "wpm80", wpm >= 80);   grant(have, fresh, "wpm100", wpm >= 100);
        grant(have, fresh, "perfect", acc >= 100); grant(have, fresh, "runs10", runs >= 10);
        grant(have, fresh, "runs50", runs >= 50); grant(have, fresh, "goal", beatGoal);
        grant(have, fresh, "marathon", mode.equals("Time 60"));
        grant(have, fresh, "hard", Theme.level.equals("Hard") && wpm >= 40);
        Theme.P.put("ach", String.join(",", have));
        return fresh;
    }
    private static void grant(Set<String> have, List<String> fresh, String id, boolean condition) {
        if (condition && have.add(id)) for (String[] a : ALL) if (a[0].equals(id)) fresh.add(a[1]);
    }
}
