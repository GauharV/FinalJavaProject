# 🏁 Zing Zing Zingbah Racing

**Samuel Taiwo & Gauhar Veeravalli** — Java 2 Final Project

A Boohbah-themed kart racing game built with JavaFX, inspired by Mario Kart.

---

## Screenshots & Gameplay

- **Character Select** — Choose from 5 unique Boohbah racers, each with different Speed, Acceleration, and Handling stats.
- **Race** — Compete against 4 AI karts on an oval track across 3 laps.
- **Results** — See your final position and race standings.

---

## Controls

| Key | Action |
|-----|--------|
| `↑` / `W` | Accelerate |
| `↓` / `S` | Brake / Reverse |
| `←` / `A` | Turn Left |
| `→` / `D` | Turn Right |
| `ESC` | Return to character select |

---

## Characters

| Name | Strength | Stats |
|------|----------|-------|
| Zingbah 🔴 | Balanced | SPD ●●●●○ ACC ●●●○○ HND ●●●○○ |
| Jingbah 🔵 | Speed Demon | SPD ●●●●● ACC ●●○○○ HND ●●○○○ |
| Zing Zing 🟡 | Quick Start | SPD ●●●○○ ACC ●●●●● HND ●●●○○ |
| Wahbah 🟢 | Precision | SPD ●●●○○ ACC ●●●○○ HND ●●●●● |
| Narabah 🟣 | Powerhouse | SPD ●●●●● ACC ●●○○○ HND ●●●○○ |

---

## Java 2 Concepts Demonstrated

| Concept | Where Used |
|---------|-----------|
| `ArrayList<Kart>` | `RacingEngine` — stores all racers |
| `Queue<RaceEvent<?>>` (via `LinkedList`) | `RacingEngine.eventQueue` — FIFO race event processing |
| `Deque<String>` as Stack (via `ArrayDeque`) | `RacingEngine.raceLog` — most-recent-first event log |
| **Generics** — `RaceEvent<T>` | `RaceEvent.java` — typed event payloads (lap #, messages, etc.) |
| `Comparator` lambda | `RacingEngine.updatePositions()` — sorts karts by race progress |
| **Inheritance / Composition** | `Kart` composes `Character`; `CharacterSelectScreen` extends `Pane` |
| **Encapsulation** | All model classes use private fields + public getters/setters |

---

## Project Structure

```
ZingZingZingbahRacing/
├── pom.xml                          Maven build file (JavaFX 17)
├── README.md
└── src/main/java/
    ├── module-info.java             JPMS module descriptor
    └── zingzingzingbah/
        ├── Main.java                Entry point
        ├── GameApp.java             JavaFX Application + scene manager
        ├── model/
        │   ├── Character.java       Racer data model (stats, color)
        │   ├── Kart.java            Physics simulation + AI + drawing
        │   └── RaceEvent.java       Generic<T> race event class
        ├── track/
        │   └── Track.java           Oval track geometry + rendering
        ├── engine/
        │   └── RacingEngine.java    Game logic: laps, positions, events
        └── screen/
            ├── CharacterSelectScreen.java   Selection UI (Canvas-drawn)
            └── RaceScreen.java              Race loop, HUD, results
```

---

## How to Build & Run

### Prerequisites
- Java 17 or later
- Maven 3.6+

### Run with Maven
```bash
git clone https://github.com/YOUR_USERNAME/ZingZingZingbahRacing.git
cd ZingZingZingbahRacing
mvn javafx:run
```

### Build a JAR
```bash
mvn package
```

---

## MVP

Our MVP is the **racing engine** — without it, there is no game.  
Character selection is built on top of that foundation.

## Stretch Goals
- [ ] Animations (bouncing Boohbahs, explosion effects)
- [ ] Sound effects & music
- [ ] Item boxes (shells, banana peels)
- [ ] Multiple track layouts
- [ ] Local multiplayer

---

## AI Use

We used AI to assist with code structure, JavaFX drawing API syntax, and debugging physics logic. All game design decisions, character concepts, and overall architecture were designed by the team.
