package speedytype;

import javax.sound.sampled.*;

/** Generated sound effects (no audio files): key click, mistake buzz, and a win chime. */
final class Sound {
    private static Clip click, error, win;

    static void init() {
        click = tone(new double[]{1200}, 14, 0.5);
        error = tone(new double[]{170}, 80, 0.7);
        win = tone(new double[]{523, 659, 784, 1047}, 120, 0.6);
    }
    /** Builds a clip that plays each frequency in turn for `ms` milliseconds, fading out. */
    private static Clip tone(double[] freqs, int ms, double vol) {
        try {
            AudioFormat f = new AudioFormat(44100, 8, 1, true, false);
            int n = 44100 * ms / 1000; byte[] b = new byte[n * freqs.length];
            for (int k = 0; k < freqs.length; k++)
                for (int i = 0; i < n; i++) b[k * n + i] = (byte) (Math.sin(2 * Math.PI * freqs[k] * i / 44100) * 100 * vol * (1 - (double) i / n));
            Clip c = AudioSystem.getClip(); c.open(f, b, 0, b.length); return c;
        } catch (Exception e) { return null; }
    }
    private static void play(Clip c) { if (Theme.sound && c != null) { c.stop(); c.setFramePosition(0); c.start(); } }
    static void click() { play(click); }
    static void error() { play(error); }
    static void win()   { play(win); }
}
