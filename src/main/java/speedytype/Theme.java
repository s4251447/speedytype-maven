package speedytype;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.*;
import java.util.prefs.Preferences;

/** All user settings (saved per profile) plus the color palette derived from them. */
final class Theme {
    static final Preferences G = Preferences.userNodeForPackage(Theme.class);   // global data: profile list, last profile, leaderboard
    static Preferences P;                                                        // the CURRENT profile's saved data
    static String profile;
    static final String[] MODES = {"Sentence", "Words 25", "Time 15", "Time 30", "Time 60"};
    static final String[] LEVELS = {"Easy", "Medium", "Hard"};
    static final String[] CARETS = {"Line", "Block", "Underline"};
    static final Map<String, Color> ACCENTS = new LinkedHashMap<>();
    static {
        ACCENTS.put("Ocean", new Color(64, 156, 255));  ACCENTS.put("Forest", new Color(60, 200, 110));
        ACCENTS.put("Sunset", new Color(255, 130, 70)); ACCENTS.put("Grape", new Color(170, 110, 255));
        ACCENTS.put("Rose", new Color(255, 90, 150));   ACCENTS.put("Mint", new Color(50, 215, 190));
        ACCENTS.put("Gold", new Color(240, 190, 50));
    }

    // Settings of the current profile (filled in by load)
    static boolean dark, sound, punct, numbers, ghost, focus;
    static String name, mode, level, caret, font;
    static int goal, textSize;

    static { load(G.get("last", "Player")); }

    /**
     * Switches to a profile. A new name creates a fresh profile; an existing name
     * (any capitalization) brings back everything that profile saved before.
     */
    static String load(String wanted) {
        String n = wanted.trim().replaceAll("[^A-Za-z0-9 _-]", "");
        if (n.length() > 20) n = n.substring(0, 20).trim();
        if (n.isEmpty()) n = "Player";
        List<String> all = profiles();
        for (String p : all) if (p.equalsIgnoreCase(n)) n = p;
        if (!all.contains(n)) { all.add(n); G.put("profiles", String.join(",", all)); }
        G.put("last", n); profile = n; P = G.node("p_" + n);

        dark = P.getBoolean("dark", true);    sound = P.getBoolean("sound", true);
        punct = P.getBoolean("punct", true);  numbers = P.getBoolean("numbers", false);
        ghost = P.getBoolean("ghost", true);  focus = P.getBoolean("focus", false);
        name = P.get("theme", "Ocean");       mode = P.get("mode", "Sentence");
        level = P.get("level", "Medium");     caret = P.get("caret", "Line");
        font = P.get("font", "Monospaced");   goal = P.getInt("goal", 0);
        textSize = P.getInt("size", 30);
        return n;
    }
    static List<String> profiles() {
        List<String> l = new ArrayList<>();
        for (String s : G.get("profiles", "").split(",")) if (!s.isEmpty()) l.add(s);
        return l;
    }
    /** Writes the current profile's settings to disk. Call after changing any field. */
    static void save() {
        P.putBoolean("dark", dark); P.putBoolean("sound", sound); P.putBoolean("punct", punct); P.putBoolean("numbers", numbers);
        P.putBoolean("ghost", ghost); P.putBoolean("focus", focus); P.put("theme", name); P.put("mode", mode);
        P.put("level", level); P.put("caret", caret); P.put("font", font); P.putInt("goal", goal); P.putInt("size", textSize);
    }
    static void setCustom(Color c) { P.putInt("custom", c.getRGB()); name = "Custom"; save(); }

    /** Monospaced fonts that are actually installed on this computer. */
    static List<String> fonts() {
        Set<String> have = new HashSet<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        List<String> out = new ArrayList<>(); out.add("Monospaced");
        for (String f : new String[]{"JetBrains Mono", "Fira Code", "Cascadia Code", "Source Code Pro", "Consolas", "Menlo", "Monaco", "Courier New", "Lucida Console", "DejaVu Sans Mono"})
            if (have.contains(f)) out.add(f);
        return out;
    }

    // ---- Palette ----
    static Color accent() { return name.equals("Custom") ? new Color(P.getInt("custom", 0x409CFF)) : ACCENTS.getOrDefault(name, ACCENTS.get("Ocean")); }
    /** Second gradient color: the accent with its hue shifted a little. */
    static Color accent2() {
        Color a = accent(); float[] h = Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), null);
        return Color.getHSBColor((h[0] + 0.12f) % 1f, h[1], h[2]);
    }
    static Color bg1()  { return dark ? new Color(12, 14, 28) : new Color(236, 240, 255); }
    static Color bg2()  { return dark ? new Color(34, 18, 60) : new Color(255, 236, 246); }
    static Color fg()   { return dark ? new Color(238, 240, 250) : new Color(35, 38, 55); }
    static Color dim()  { return dark ? new Color(125, 130, 160) : new Color(140, 145, 170); }
    static Color card() { return dark ? new Color(255, 255, 255, 16) : new Color(255, 255, 255, 150); }
    static Color line() { return dark ? new Color(255, 255, 255, 40) : new Color(0, 0, 0, 30); }
}
