# 🌈 Zing Zing Zingbah Racing

A Boohbah-themed Mario Kart-style racing game built in Java with LibGDX.
**By Samuel Taiwo & Gauhar Veeravalli**

---

## 🚀 How to Run

### In IntelliJ IDEA (recommended)
1. Open the project folder in IntelliJ
2. Let Gradle sync automatically
3. Run the `desktop` module: right-click `DesktopLauncher.java` → Run

### From terminal
```bash
# Windows
gradlew.bat desktop:run

# Mac / Linux
./gradlew desktop:run
```

### Build a runnable JAR
```bash
gradlew.bat desktop:jar          # Windows
./gradlew desktop:jar            # Mac/Linux

# Then run:
java -jar desktop/build/libs/ZingZingRacing.jar
```

---

## 🎮 Controls

| Key | Action |
|-----|--------|
| ↑ / W | Accelerate |
| ↓ / S | Brake / Reverse |
| ← / A | Turn Left |
| → / D | Turn Right |
| ESC | Return to Menu |
| ENTER / SPACE | Confirm |

---

## 🏎️ Characters

| Character | Strength |
|-----------|----------|
| Zumbah (Purple) | Balanced |
| Zing Zing (Red) | Top Speed |
| Jumbah (Orange) | Acceleration |
| Humbah (Green) | Handling |
| Tombliboo (Pink) | Speed |

---

## 📚 Java 2 Concepts Used

- **ArrayList<Racer>** — all racers stored and iterated
- **Queue<Racer>** (LinkedList) — finish order tracking
- **ArrayDeque<Float>** (Stack) — AI steering angle history
- **Generics** — typed collections throughout
- **Inheritance** — PlayerRacer and AIRacer extend Racer
- **Encapsulation** — private fields with getters

---

## 🗂️ Structure

```
zing-zing-zingbah-racing/
├── assets/                          ← Game assets folder
├── core/src/main/java/com/zingzingracing/
│   ├── ZingZingRacing.java
│   ├── characters/BoohbahCharacter.java
│   ├── racing/  Racer, PlayerRacer, AIRacer, RaceManager
│   ├── screens/ MenuScreen, CharacterSelectScreen, RaceScreen
│   └── track/   Track, TrackWaypoint
├── desktop/src/main/java/com/zingbah/racing/desktop/
│   └── DesktopLauncher.java         ← Entry point
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
└── README.md
```
