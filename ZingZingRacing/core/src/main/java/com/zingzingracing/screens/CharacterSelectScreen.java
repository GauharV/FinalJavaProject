package com.zingzingracing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.zingzingracing.ZingZingRacing;
import com.zingzingracing.characters.BoohbahCharacter;
import java.util.ArrayList;

/**
 * Character selection screen. Player scrolls through 5 Boohbahs.
 */
public class CharacterSelectScreen implements Screen {

    private final ZingZingRacing game;
    private final ShapeRenderer sr;
    private final ArrayList<BoohbahCharacter> characters;
    private int selectedIndex = 0;
    private float time = 0;
    private float inputCooldown = 0;

    public CharacterSelectScreen(ZingZingRacing game) {
        this.game = game;
        this.sr = new ShapeRenderer();
        this.characters = BoohbahCharacter.createAllCharacters();
    }

    @Override
    public void render(float delta) {
        time += delta;
        inputCooldown -= delta;

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.25f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Handle input
        if (inputCooldown <= 0) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                selectedIndex = (selectedIndex - 1 + characters.size()) % characters.size();
                inputCooldown = 0.2f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                selectedIndex = (selectedIndex + 1) % characters.size();
                inputCooldown = 0.2f;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new RaceScreen(game, characters.get(selectedIndex)));
            dispose();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Draw all 5 character cards
        int numChars = characters.size();
        float cardW = 120f, cardH = 180f;
        float spacing = 140f;
        float startX = 400f - (numChars - 1) * spacing / 2f;

        for (int i = 0; i < numChars; i++) {
            BoohbahCharacter ch = characters.get(i);
            float cx = startX + i * spacing;
            float cy = 290f;

            boolean selected = (i == selectedIndex);
            float scale = selected ? 1.2f + (float)(Math.sin(time * 4) * 0.05) : 1.0f;
            float cw = cardW * scale;
            float ch2 = cardH * scale;

            // Card background
            if (selected) {
                sr.setColor(0.9f, 0.8f, 0.1f, 1f);
                sr.rect(cx - cw/2 - 4, cy - ch2/2 - 4, cw + 8, ch2 + 8);
            }
            sr.setColor(0.15f, 0.15f, 0.4f, 1f);
            sr.rect(cx - cw/2, cy - ch2/2, cw, ch2);

            // Boohbah body
            float r = ch.getColorR() / 255f;
            float g2 = ch.getColorG() / 255f;
            float b = ch.getColorB() / 255f;

            float bouncY = selected ? (float)(Math.sin(time * 5) * 6) : 0;
            float bodyR = 38f * scale;

            sr.setColor(r, g2, b, 1f);
            sr.circle(cx, cy + 10 + bouncY, bodyR);
            sr.setColor(Math.min(r+0.3f,1f), Math.min(g2+0.3f,1f), Math.min(b+0.3f,1f), 1f);
            sr.circle(cx, cy + 5 + bouncY, bodyR * 0.55f);
            sr.setColor(Color.WHITE);
            sr.circle(cx - 10, cy + 22 + bouncY, 6f * scale);
            sr.circle(cx + 10, cy + 22 + bouncY, 6f * scale);
            sr.setColor(Color.BLACK);
            sr.circle(cx - 9, cy + 23 + bouncY, 3f * scale);
            sr.circle(cx + 11, cy + 23 + bouncY, 3f * scale);

            // Stat bars
            float barX = cx - 40 * scale;
            float barY = cy - ch2/2 + 15;
            float barW = 80f * scale;
            float barH = 8f * scale;

            // Speed bar
            sr.setColor(0.3f, 0.3f, 0.3f, 1f);
            sr.rect(barX, barY + barH * 2.5f, barW, barH);
            sr.setColor(0.2f, 0.8f, 0.2f, 1f);
            sr.rect(barX, barY + barH * 2.5f, barW * (ch.getTopSpeed() / 220f), barH);

            // Accel bar
            sr.setColor(0.3f, 0.3f, 0.3f, 1f);
            sr.rect(barX, barY + barH * 1.0f, barW, barH);
            sr.setColor(0.2f, 0.6f, 1.0f, 1f);
            sr.rect(barX, barY + barH * 1.0f, barW * ch.getAcceleration(), barH);
        }

        sr.end();

        // Text
        game.batch.begin();
        game.bigFont.setColor(1f, 0.9f, 0.1f, 1f);
        game.bigFont.draw(game.batch, "PICK YOUR BOOHBAH!", 135, 560);

        // Character name & desc
        BoohbahCharacter sel = characters.get(selectedIndex);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, sel.getName(), 340, 165);
        game.smallFont.setColor(0.9f, 0.9f, 0.9f, 1f);
        game.smallFont.draw(game.batch, sel.getDescription(), 320, 145);

        // Stat labels
        float startX2 = 400f - (characters.size() - 1) * 140f / 2f + selectedIndex * 140f;
        game.smallFont.setColor(0.2f, 1f, 0.2f, 1f);
        game.smallFont.draw(game.batch, "SPD", startX2 - 55, 220);
        game.smallFont.setColor(0.2f, 0.6f, 1f, 1f);
        game.smallFont.draw(game.batch, "ACC", startX2 - 55, 205);

        game.font.setColor(0.8f, 0.8f, 0.8f, 1f);
        game.font.draw(game.batch, "< LEFT / RIGHT >   ENTER to race!", 155, 100);
        game.smallFont.setColor(0.6f, 0.6f, 0.6f, 1f);
        game.smallFont.draw(game.batch, "ESC = Back", 355, 60);

        game.batch.end();
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { sr.dispose(); }
}
