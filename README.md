# SpeedyType

A typing speed test for your desktop, written in Java Swing. Type a sentence, see your speed and accuracy, chase a goal, unlock trophies, and find out which keys you keep getting wrong.

<!-- Add a screenshot: save it as docs/screenshot.png, then delete this line's comment markers.
![SpeedyType screenshot](docs/screenshot.png)
-->

## About this project

The idea, feature design, and direction are mine. The code was written with AI assistance (Claude) through "vibe coding": I described what I wanted, tested it, and kept asking for changes until it felt right.

It's inspired by [Monkeytype](https://monkeytype.com/) but is an independent project and isn't affiliated with it.

## Features

**Typing tests**
- Five modes: Sentence, Words 25, Time 15, Time 30, and Time 60
- Three difficulty levels: Easy, Medium, Hard
- Optional punctuation and numbers
- A "ghost" caret that paces you against your target speed
- Live WPM, accuracy, and a speed graph on the results screen

**Progress and motivation**
- Set a personal WPM goal and try to beat it
- Stats page with your history and personal bests
- **Keyboard heatmap:** keys you miss most glow red, and your weakest keys are listed
- Local leaderboard: top 10 scores per mode across every profile on the computer
- 11 achievements to unlock, like *First Steps*, *Speed Demon*, *Flawless*, and *Marathon*
- Multiple profiles, each with its own settings, stats, and trophies

**Customisation**
- 7 colour themes plus a custom colour, with dark mode
- Font choice, text size, and caret style (line, block, underline)
- Keyboard click, mistake, and win sounds, all generated in code, so there are no audio files
- Focus mode, which hides everything while you type

## Requirements

- [JDK 17](https://adoptium.net/) or newer
- [Maven](https://maven.apache.org/)

## Build and run

```bash
git clone https://github.com/YOUR-USERNAME/speedytype.git
cd speedytype

mvn clean package
java -jar target/speedytype.jar
```

## Keyboard shortcuts

| Key | Action |
|---|---|
| `Tab` + `Enter` | Restart the test |
| `Esc` | Back to the home screen |
| `F11` | Toggle fullscreen |

## Adding your own sentences

The built-in sentences are in `src/main/resources/sentences.txt`.

To add your own without changing the project, create `~/.speedytype/sentences.txt` with one sentence per line. Lines starting with `#` are treated as comments.

## Where your data is stored

Profiles, settings, stats, and the leaderboard are saved on your computer with Java Preferences, per user account. Nothing is sent over the internet.

## Make a shareable installer (optional)

Run this on the operating system you're targeting. It needs a JDK 17+ that includes `jpackage`.

```bash
jpackage --type app-image --name SpeedyType --input target --main-jar speedytype.jar --main-class speedytype.SpeedyType
```

Use `--type exe`, `msi`, `dmg`, or `deb` for a full installer. On Windows, `exe` and `msi` also need the [WiX Toolset](https://wixtoolset.org/).

## Project structure

| File | What it does |
|---|---|
| `SpeedyType.java` | App entry point and screen switching |
| `TypingPanel.java` | The typing test itself: input, timing, accuracy |
| `ResultPanel.java` | Results screen and speed graph |
| `Stats.java` | Saves history, personal bests, per-key accuracy, and the leaderboard |
| `Achievements.java` | Trophy definitions and unlock checks |
| `HeatmapPanel.java` | The keyboard heatmap |
| `Theme.java` | Settings, profiles, and colours |
| `Sound.java` | Generated sound effects |

## Ideas for the future

- More sentence packs or a quote mode
- Export your stats to a file
- Online leaderboard

## License

MIT. See the `LICENSE` file.
