package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.zingbah.racing.ZingbahRacing;

public class MenuScreen extends BaseScreen {

    private float time = 0f;

    public MenuScreen(ZingbahRacing game) {
        super(game);
    }

    @Override
    protected void onShow() {
        Table root = new Table();
        root.setFillParent(true);

        // Spacer
        root.add().expandY();

        // Title
        Label title = new Label("ZING ZING ZINGBAH RACING", skin, "title");
        title.setFontScale(2.2f);
        root.add(title).padBottom(6).row();

        Label sub = new Label("A Boohbah Kart Experience", skin);
        sub.setFontScale(1.3f);
        root.add(sub).padBottom(60).row();

        // Play button
        TextButton playBtn = new TextButton("  RACE!  ", skin, "accent");
        playBtn.getLabel().setFontScale(1.8f);
        playBtn.pad(14, 40, 14, 40);
        playBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                game.getStateStack().push(new CharacterSelectScreen(game));
            }
        });
        root.add(playBtn).width(300).padBottom(24).row();

        // Quit button
        TextButton quitBtn = new TextButton("Quit", skin, "back");
        quitBtn.getLabel().setFontScale(1.2f);
        quitBtn.pad(8, 30, 8, 30);
        quitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        root.add(quitBtn).width(200).padBottom(20).row();

        // Controls hint
        Label controls = new Label(
                "WASD / Arrows = Drive   |   SPACE = Use Item", skin);
        controls.setFontScale(0.9f);
        root.add(controls).padBottom(20).row();

        root.add().expandY();
        stage.addActor(root);
    }

    @Override
    protected void onRender(float delta) {
        time += delta;
        // Animated sky-gradient background
        // NES-style dark navy background with subtle pulse
        float pulse = 0.5f + 0.5f * MathUtils.sin(time * 0.5f);
        Gdx.gl.glClearColor(0.00f, 0.00f, 0.39f + 0.04f * pulse, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
