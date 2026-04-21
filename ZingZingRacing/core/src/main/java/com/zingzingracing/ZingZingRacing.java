package com.zingzingracing;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.zingzingracing.screens.MenuScreen;

public class ZingZingRacing extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public BitmapFont bigFont;
    public BitmapFont smallFont;

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;
    public static final int TOTAL_LAPS = 3;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        bigFont = new BitmapFont();
        bigFont.getData().setScale(3f);
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.0f);
        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        bigFont.dispose();
        smallFont.dispose();
    }
}
