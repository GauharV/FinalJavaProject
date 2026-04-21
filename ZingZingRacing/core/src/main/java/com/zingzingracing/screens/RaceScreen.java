package com.zingzingracing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.zingzingracing.ZingZingRacing;
import com.zingzingracing.characters.BoohbahCharacter;
import com.zingzingracing.racing.*;
import com.zingzingracing.track.Track;
import java.util.ArrayList;

/**
 * The main race screen. Renders track, racers, HUD.
 */
public class RaceScreen implements Screen {

    private final ZingZingRacing game;
    private final ShapeRenderer sr;
    private final Track track;
    private final RaceManager raceManager;
    private final PlayerRacer player;

    private float time = 0;
    private boolean showingResults = false;
    private float resultsTimer = 0;

    // Finish banner flash
    private float finishFlash = 0;

    public RaceScreen(ZingZingRacing game, BoohbahCharacter chosen) {
        this.game = game;
        this.sr = new ShapeRenderer();
        this.track = new Track();

        // Create player
        player = new PlayerRacer(chosen, track.getStartPosition(0).x, track.getStartPosition(0).y);

        // Create 4 AI opponents from remaining characters
        ArrayList<BoohbahCharacter> allChars = BoohbahCharacter.createAllCharacters();
        ArrayList<AIRacer> aiRacers = new ArrayList<>();
        int lane = 1;
        float[] difficulties = {0.6f, 0.75f, 0.85f, 0.95f};
        int di = 0;
        for (BoohbahCharacter ch : allChars) {
            if (!ch.getName().equals(chosen.getName())) {
                float[] pos = getLanePos(lane);
                aiRacers.add(new AIRacer(ch, pos[0], pos[1], difficulties[di++]));
                lane++;
                if (di >= difficulties.length) break;
            }
        }

        raceManager = new RaceManager(player, aiRacers, track, ZingZingRacing.TOTAL_LAPS);
    }

    private float[] getLanePos(int lane) {
        float baseX = Track.TRACK_CENTER_X;
        float baseY = Track.TRACK_CENTER_Y + Track.INNER_RY + 30f;
        float[][] offsets = new float[][]{{-35, 0}, {35, 0}, {-70, -35}, {70, -35}};
        if (lane <= 0 || lane > 4) return new float[]{baseX, baseY};
        float[] off = offsets[lane - 1];
        return new float[]{baseX + off[0], baseY + off[1]};
    }

    @Override
    public void render(float delta) {
        time += delta;

        if (!showingResults) {
            raceManager.update(delta);
        }

        if (raceManager.isRaceOver() && !showingResults) {
            showingResults = true;
            finishFlash = 1.5f;
        }
        if (showingResults) {
            resultsTimer += delta;
            finishFlash -= delta;
        }

        // ESC to quit
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        // After results, press ENTER to restart
        if (showingResults && resultsTimer > 1.5f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                game.setScreen(new MenuScreen(game));
                dispose();
                return;
            }
        }

        // === RENDER ===
        Gdx.gl.glClearColor(0.2f, 0.55f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        track.renderBackground(sr);

        // Track
        track.render(sr);

        // All racers
        for (Racer r : raceManager.getRacers()) {
            r.render(sr);
        }

        sr.end();

        // HUD
        renderHUD(delta);

        // Countdown overlay
        renderCountdown();

        // Results overlay
        if (showingResults) {
            renderResults();
        }
    }

    private void renderHUD(float delta) {
        game.batch.begin();

        // Position badge
        int pos = player.getPosition();
        String posStr = pos + getOrdinal(pos);
        game.bigFont.setColor(pos == 1 ? Color.GOLD : Color.WHITE);
        game.bigFont.draw(game.batch, posStr, 20, 590);

        // Lap counter
        int lap = Math.min(player.getCurrentLap(), ZingZingRacing.TOTAL_LAPS);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "LAP " + lap + "/" + ZingZingRacing.TOTAL_LAPS, 600, 590);

        // Speed
        int spd = (int) Math.abs(player.getSpeed());
        game.font.setColor(Color.CYAN);
        game.font.draw(game.batch, "SPD: " + spd, 650, 555);

        // Character name
        game.smallFont.setColor(Color.WHITE);
        game.smallFont.draw(game.batch, player.getName(), 20, 555);

        // Controls reminder
        game.smallFont.setColor(0.7f, 0.7f, 0.7f, 1f);
        game.smallFont.draw(game.batch, "ESC=Menu", 700, 30);

        // Mini race standings
        game.smallFont.setColor(Color.WHITE);
        game.smallFont.draw(game.batch, "STANDINGS:", 10, 200);
        ArrayList<Racer> racers = raceManager.getRacers();
        racers.sort((r1, r2) -> Float.compare(r2.getRaceProgress(), r1.getRaceProgress()));
        for (int i = 0; i < racers.size(); i++) {
            Racer r = racers.get(i);
            boolean isPlayer = r == player;
            game.smallFont.setColor(isPlayer ? Color.YELLOW : Color.WHITE);
            game.smallFont.draw(game.batch, (i+1) + ". " + r.getName(), 10, 180 - i * 18);
        }

        game.batch.end();
    }

    private void renderCountdown() {
        float cd = raceManager.getCountdown();
        if (cd <= 0 && raceManager.isRaceStarted()) return;

        // Dim overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.45f);
        sr.rect(0, 0, 800, 600);
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();
        if (cd > 0) {
            int countNum = (int) Math.ceil(cd);
            float pulse = (float)(Math.sin(time * 8) * 0.15 + 1.0);
            game.bigFont.getData().setScale(5f * pulse);
            game.bigFont.setColor(countNum == 1 ? Color.RED : countNum == 2 ? Color.YELLOW : Color.GREEN);
            game.bigFont.draw(game.batch, String.valueOf(countNum), 370, 340);
            game.bigFont.getData().setScale(3f);
        } else {
            game.bigFont.setColor(Color.GREEN);
            game.bigFont.draw(game.batch, "GO!", 330, 340);
        }
        game.batch.end();
    }

    private void renderResults() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0, 0, 0, 0.75f);
        sr.rect(150, 130, 500, 340);

        // Winner color flash
        if (finishFlash > 0) {
            sr.setColor(1f, 0.9f, 0.1f, finishFlash * 0.3f);
            sr.rect(150, 130, 500, 340);
        }
        sr.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();

        int playerPos = player.getPosition();
        String[] msgs = {"", "YOU WIN!!", "2nd Place!", "3rd Place", "4th Place", "5th Place"};
        String headline = playerPos >= 1 && playerPos < msgs.length ? msgs[playerPos] : "Finished!";

        game.bigFont.setColor(playerPos == 1 ? Color.GOLD : Color.WHITE);
        game.bigFont.draw(game.batch, headline, 270, 435);

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "RACE RESULTS", 285, 395);

        // Show finish order
        ArrayList<Racer> racers = raceManager.getRacers();
        racers.sort((r1, r2) -> Float.compare(r2.getRaceProgress(), r1.getRaceProgress()));
        for (int i = 0; i < racers.size(); i++) {
            Racer r = racers.get(i);
            boolean isPlayer = r == player;
            game.font.setColor(isPlayer ? Color.YELLOW : Color.WHITE);
            String timeStr = String.format("%.1fs", r.getRaceTime());
            game.font.draw(game.batch, (i+1) + ". " + r.getName() + "   " + timeStr, 230, 360 - i * 35);
        }

        if (resultsTimer > 1.5f) {
            float blink = (float)(Math.sin(time * 4) * 0.5 + 0.5);
            game.font.setColor(1f, blink, blink, 1f);
            game.font.draw(game.batch, "ENTER / SPACE = Menu", 255, 165);
        }

        game.batch.end();
    }

    private String getOrdinal(int n) {
        if (n == 1) return "st";
        if (n == 2) return "nd";
        if (n == 3) return "rd";
        return "th";
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { sr.dispose(); }
}
