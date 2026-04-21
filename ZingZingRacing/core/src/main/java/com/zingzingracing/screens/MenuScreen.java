package com.zingzingracing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.zingzingracing.ZingZingRacing;

/**
 * Main menu screen with animated Boohbah circles.
 */
public class MenuScreen implements Screen {

    private final ZingZingRacing game;
    private final ShapeRenderer sr;
    private float time = 0;

    // Bouncing Boohbah decorations
    private final float[][] boohbahs = {
        {100, 400, 0.0f,   155, 89,  182},
        {700, 200, 1.2f,   231, 76,  60},
        {150, 150, 2.4f,   243, 156, 18},
        {650, 450, 0.8f,   39,  174, 96},
        {400, 500, 1.8f,   233, 30,  99},
    };

    public MenuScreen(ZingZingRacing game) {
        this.game = game;
        this.sr = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        time += delta;

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.3f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        // Draw bouncing Boohbah decorations
        for (float[] bh : boohbahs) {
            float bx = bh[0] + (float)(Math.sin(time * 1.2f + bh[2]) * 20);
            float by = bh[1] + (float)(Math.cos(time * 0.9f + bh[2]) * 15);
            sr.setColor(bh[3]/255f, bh[4]/255f, bh[5]/255f, 1f);
            sr.circle(bx, by, 35);
            sr.setColor(Math.min(bh[3]/255f+0.3f,1f), Math.min(bh[4]/255f+0.3f,1f), Math.min(bh[5]/255f+0.3f,1f), 1f);
            sr.circle(bx, by - 5, 18);
            sr.setColor(Color.WHITE);
            sr.circle(bx - 8, by + 8, 5);
            sr.circle(bx + 8, by + 8, 5);
            sr.setColor(Color.BLACK);
            sr.circle(bx - 7, by + 9, 2.5f);
            sr.circle(bx + 9, by + 9, 2.5f);
        }

        // Title background bar
        sr.setColor(0.05f, 0.05f, 0.2f, 0.85f);
        sr.rect(60, 290, 680, 120);

        sr.end();

        // Text
        game.batch.begin();

        // Rainbow title effect
        float pulse = (float)(Math.sin(time * 3) * 0.5 + 0.5);
        game.bigFont.setColor(1f, pulse * 0.5f + 0.5f, pulse, 1f);
        game.bigFont.draw(game.batch, "ZING ZING ZINGBAH", 95, 395);
        game.bigFont.setColor(0.9f, 0.8f, 0.1f, 1f);
        game.bigFont.draw(game.batch, "RACING!", 270, 350);

        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Press ENTER or SPACE to Start", 230, 260);

        game.smallFont.setColor(0.8f, 0.8f, 0.8f, 1f);
        game.smallFont.draw(game.batch, "Arrow Keys / WASD to drive", 270, 220);
        game.smallFont.draw(game.batch, "Race 3 laps around the circuit!", 255, 195);
        game.smallFont.draw(game.batch, "Samuel Taiwo & Gauhar Veeravalli", 245, 40);

        game.batch.end();

        // Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new CharacterSelectScreen(game));
            dispose();
        }
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { sr.dispose(); }
}
