package speedytype;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Loads practice text and builds each round's text, honoring difficulty, punctuation and numbers settings. */
final class Sentences {
    static final List<String> LINES = new ArrayList<>(), WORDS = new ArrayList<>();
    static final Random RND = new Random();

    /** Built-in sentences come from the bundled sentences.txt; yours go in ~/.speedytype/sentences.txt. */
    static void load() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Sentences.class.getResourceAsStream("/sentences.txt"), StandardCharsets.UTF_8))) {
            r.lines().forEach(Sentences::add);
        } catch (Exception e) { /* bundled file missing: fine */ }
        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".speedytype"), f = dir.resolve("sentences.txt");
            Files.createDirectories(dir);
            if (!Files.exists(f)) Files.write(f, Collections.singletonList("# Add your own sentences below, one per line. Lines starting with # are ignored."));
            Files.readAllLines(f).forEach(Sentences::add);
        } catch (Exception e) { /* no home folder access: fine */ }
        if (LINES.isEmpty()) LINES.add("Type this sentence to get started.");
        Set<String> pool = new LinkedHashSet<>();
        for (String s : LINES) for (String w : s.split("\\s+")) { w = w.replaceAll("[^A-Za-z']", "").toLowerCase(); if (w.length() > 1) pool.add(w); }
        WORDS.addAll(pool);
    }
    private static void add(String l) { l = l.trim(); if (!l.isEmpty() && !l.startsWith("#") && !LINES.contains(l)) LINES.add(l); }

    private static boolean sentenceFits(String s) {
        int n = s.length();
        switch (Theme.level) { case "Easy": return n < 70; case "Hard": return n > 105; default: return n >= 70 && n <= 105; }
    }
    private static boolean wordFits(String w) {
        int n = w.length();
        switch (Theme.level) { case "Easy": return n <= 5; case "Hard": return n >= 6; default: return n >= 4 && n <= 7; }
    }

    /** A random sentence for the current difficulty (punctuation stripped / numbers added if the settings say so). */
    static String sentence() {
        List<String> pool = new ArrayList<>();
        for (String s : LINES) if (sentenceFits(s)) pool.add(s);
        if (pool.isEmpty()) pool = LINES;
        String s = pool.get(RND.nextInt(pool.size()));
        if (!Theme.punct) s = s.replaceAll("[^A-Za-z0-9' ]", "");
        if (Theme.numbers) { String[] w = s.split(" "); w[RND.nextInt(w.length)] += " " + (10 + RND.nextInt(990)); s = String.join(" ", w); }
        return s;
    }

    /** n random words for Words/Time modes, with optional numbers and punctuation. */
    static String words(int n) {
        List<String> pool = new ArrayList<>();
        for (String w : WORDS) if (wordFits(w)) pool.add(w);
        if (pool.isEmpty()) pool = WORDS;
        StringBuilder sb = new StringBuilder(); boolean capNext = false;
        for (int i = 0; i < n; i++) {
            String w = pool.get(RND.nextInt(pool.size()));
            if (Theme.numbers && RND.nextInt(8) == 0) w = String.valueOf(1 + RND.nextInt(999));
            else {
                if (capNext) w = Character.toUpperCase(w.charAt(0)) + w.substring(1);
                capNext = false;
                if (Theme.punct) {
                    int r = RND.nextInt(14);
                    if (r == 0) w += ","; else if (r == 1) { w += "."; capNext = true; } else if (r == 2) { w += "?"; capNext = true; }
                }
            }
            if (i > 0) sb.append(' ');
            sb.append(w);
        }
        return sb.toString();
    }
}
