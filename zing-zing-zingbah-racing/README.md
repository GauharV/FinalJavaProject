# 🏎️ Zing Zing Zingbah Racing

A Boohbah-themed kart racer built with LibGDX for Java 2.  
By **Samuel Taiwo & Gauhar Veeravalli** — WECIB Java 2 Project.

---

## Features

- **5 playable Boohbah characters** — each with unique speed / acceleration / handling stats
- **Third-person camera** with smooth lag / spring feel
- **3 laps** against 3 AI opponents
- **Power-up system** — Speed Boost, Star Shield, Shrink Ray (queued in a `LinkedList`)
- **Spinning item boxes** scattered around the track
- **Procedurally generated** elliptical track with curbs, center-line dashes, and a checkered finish line

## Java 2 Concepts Demonstrated

| Concept | Where |
|---|---|
| `Stack<Screen>` | `GameStateStack.java` — screen navigation |
| `Queue<PowerUp>` / `LinkedList` | `ItemQueue.java` — held items |
| Generics `<T extends Comparable<T>>` | `RaceResult.java` — race standings |
| `ArrayList` | `Track.java` — item box positions, model list |
| `LinkedList<Vector3>` | `Track.java` — waypoint path |

---

## Requirements

- **Java 11+** (Java 17 or 21 recommended)
- **No other installs needed** — Gradle downloads LibGDX automatically

---

## How to Run

### Option 1 — Gradle (easiest)

```bash
# Clone / unzip the project, then:
cd zing-zing-zingbah-racing

# macOS / Linux
./gradlew desktop:run

# Windows
gradlew.bat desktop:run
```

Gradle will download all dependencies on the first run (~30 seconds).

### Option 2 — Build a fat JAR

```bash
./gradlew desktop:jar
java -jar desktop/build/libs/zingbah-racing.jar
```

### Option 3 — IntelliJ IDEA

1. **File → Open** → select the project root folder
2. Wait for Gradle sync to finish
3. Open `DesktopLauncher.java` → right-click → **Run**

---

## Controls

| Key | Action |
|---|---|
| `W` / `↑` | Accelerate |
| `S` / `↓` | Brake / Reverse |
| `A` / `←` | Steer Left |
| `D` / `→` | Steer Right |
| `SPACE` | Use held item |
| `ESC` | Quit to menu |

---

## Project Structure

```
zing-zing-zingbah-racing/
├── core/src/main/java/com/zingbah/racing/
│   ├── ZingbahRacing.java          # Main Game class
│   ├── characters/
│   │   └── BoohbahCharacter.java   # Character enum with stats
│   ├── data/
│   │   ├── ItemQueue.java          # Generic Queue<T> (LinkedList)
│   │   ├── GameStateStack.java     # Stack<Screen> navigator
│   │   └── PowerUp.java            # Power-up definitions
│   ├── engine/
│   │   ├── RacingEngine.java       # Master race loop
│   │   ├── Kart.java               # Physics + 3D model
│   │   ├── Track.java              # Procedural track mesh
│   │   ├── AIController.java       # Waypoint-following AI
│   │   ├── ItemBox.java            # Spinning pick-up
│   │   └── RaceResult.java         # Generic standings list
│   └── screens/
│       ├── BaseScreen.java         # Shared Stage/Skin/font helpers
│       ├── MenuScreen.java         # Title screen
│       ├── CharacterSelectScreen.java # Live 3D kart preview
│       ├── RaceScreen.java         # Third-person race view + HUD
│       └── ResultScreen.java       # Podium / standings
└── desktop/src/main/java/com/zingbah/racing/desktop/
    └── DesktopLauncher.java        # LWJGL3 entry point
```

---

## Setting Up the GitHub Repo

```bash
cd zing-zing-zingbah-racing
git init
git add .
git commit -m "Initial commit: Zing Zing Zingbah Racing"
git remote add origin https://github.com/YOUR_USERNAME/zing-zing-zingbah-racing.git
git push -u origin main
```

---

## Notes

- The Gradle wrapper JAR (`gradle/wrapper/gradle-wrapper.jar`) is **not** included in this zip because it is a binary file.  
  Run `gradle wrapper --gradle-version 8.5` once in the project root to generate it, then `git add gradle/` and commit.  
  Alternatively, IntelliJ will generate it automatically on first open.
- All graphics are **procedurally generated** — no external asset files required.
