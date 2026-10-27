package speedytype;

import java.util.*;

/** Saved results: history, bests and per-key accuracy (per profile), plus the shared leaderboard. */
final class Stats {
    /** Each row is {wpm, rawWpm, accuracy, mode, timestamp}. */
    static List<String[]> history() {
        List<String[]> rows = new ArrayList<>();
        for (String s : Theme.P.get("history", "").split("\n")) if (!s.trim().isEmpty()) rows.add(s.split("\\|"));
        return rows;
    }
    static double best(String mode) { return Theme.P.getDouble("best_" + mode, 0); }
    static int runs() { return Theme.P.getInt("runs", 0); }

    /** Records a run and returns the previous personal best for that mode (0 if none). */
    static double addRun(String mode, double wpm, double raw, double acc) {
        double prev = best(mode);
        if (wpm > prev) Theme.P.putDouble("best_" + mode, wpm);
        Theme.P.putInt("runs", runs() + 1);
        String h = Theme.P.get("history", "") + String.format(Locale.ROOT, "%.1f|%.1f|%.1f|%s|%d\n", wpm, raw, acc, mode, System.currentTimeMillis());
        String[] lines = h.split("\n");
        if (lines.length > 50) h = String.join("\n", Arrays.copyOfRange(lines, lines.length - 50, lines.length)) + "\n";
        Theme.P.put("history", h);
        addScore(mode, wpm, acc);
        return prev;
    }

    // ---- Per-key accuracy for the heatmap: letter -> {attempts, mistakes} ----
    static Map<Character, int[]> keys() {
        Map<Character, int[]> m = new TreeMap<>();
        for (String s : Theme.P.get("keys", "").split(";")) {
            String[] p = s.split(":");
            try { if (p.length == 3) m.put((char) Integer.parseInt(p[0]), new int[]{Integer.parseInt(p[1]), Integer.parseInt(p[2])}); }
            catch (NumberFormatException e) { /* skip damaged entry */ }
        }
        return m;
    }
    static void addKeys(Map<Character, int[]> run) {
        Map<Character, int[]> all = keys();
        for (Map.Entry<Character, int[]> e : run.entrySet()) {
            int[] a = all.computeIfAbsent(e.getKey(), k -> new int[2]); a[0] += e.getValue()[0]; a[1] += e.getValue()[1];
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, int[]> e : all.entrySet()) sb.append((int) e.getKey()).append(':').append(e.getValue()[0]).append(':').append(e.getValue()[1]).append(';');
        Theme.P.put("keys", sb.toString());
    }

    // ---- Leaderboard shared by all profiles on this computer (top 10 per mode) ----
    /** Rows are {profile, mode, wpm, accuracy, timestamp}, best first. */
    static List<String[]> board(String mode) {
        List<String[]> l = new ArrayList<>();
        for (String[] r : allScores()) if (r[1].equals(mode)) l.add(r);
        return l;
    }
    private static List<String[]> allScores() {
        List<String[]> l = new ArrayList<>();
        for (String s : Theme.G.get("board", "").split("\n")) if (!s.trim().isEmpty()) l.add(s.split("\\|"));
        return l;
    }
    private static void addScore(String mode, double wpm, double acc) {
        List<String[]> all = allScores();
        all.add(new String[]{Theme.profile, mode, String.format(Locale.ROOT, "%.1f", wpm), String.format(Locale.ROOT, "%.1f", acc), String.valueOf(System.currentTimeMillis())});
        all.sort((a, b) -> Double.compare(Double.parseDouble(b[2]), Double.parseDouble(a[2])));
        Map<String, Integer> count = new HashMap<>(); StringBuilder sb = new StringBuilder();
        for (String[] r : all) if (count.merge(r[1], 1, Integer::sum) <= 10) sb.append(String.join("|", r)).append('\n');
        Theme.G.put("board", sb.toString());
    }
}
