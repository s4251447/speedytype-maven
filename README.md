# SpeedyType

A MonkeyType-style typing test written in Java Swing.

## Build and run (needs JDK 17+ and Maven)
    mvn clean package
    java -jar target/speedytype.jar

## Make a shareable installer (.exe / .msi / .dmg / .deb)
Run on the OS you are targeting (needs JDK 17+ with jpackage):

    jpackage --type app-image --name SpeedyType --input target --main-jar speedytype.jar --main-class speedytype.SpeedyType

(Use `--type exe`, `msi`, `dmg` or `deb` for an installer. On Windows `exe`/`msi` also need the WiX Toolset.)

## Where things live
- Built-in sentences: `src/main/resources/sentences.txt`
- Your own sentences: `~/.speedytype/sentences.txt` (one per line, `#` = comment)
- Profiles, stats and settings are stored with Java Preferences (per user account).

## Keys
Tab+Enter restart, Esc home, F11 fullscreen.
