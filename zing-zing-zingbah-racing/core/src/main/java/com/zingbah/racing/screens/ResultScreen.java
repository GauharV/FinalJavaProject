package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.zingbah.racing.ZingbahRacing;
import com.zingbah.racing.engine.RaceResult;

public class ResultScreen extends BaseScreen {

    private final RaceResult<Float> result;
    private float time = 0f;

    public ResultScreen(ZingbahRacing game, RaceResult<Float> result) {
        super(game);
        this.result = result;
    }

    @Override
    protected void onShow() {
        RaceResult.Entry<Float> playerEntry = result.getPlayerEntry();
        boolean won = playerEntry != null && playerEntry.position == 1;

        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(36);

        // ── Title ─────────────────────────────────────────────────────────────
        String headline = won ? "🏆  WINNER!  🏆" : "RACE OVER";
        Label title = new Label(headline, skin, "title");
        title.setFontScale(won ? 2.6f : 2.0f);
        title.setAlignment(Align.center);
        root.add(title).colspan(2).expandX().padBottom(28).row();

        // ── Standings table ───────────────────────────────────────────────────
        String[] medals = { "🥇", "🥈", "🥉", "  4." };
        for (RaceResult.Entry<Float> entry : result.getEntries()) {
            String medal = entry.position <= 3 ? medals[entry.position - 1]
                    : "  " + entry.position + ".";
            Label posLbl  = new Label(medal + "  " + entry.character.displayName
                    + (entry.isPlayer ? "  ← YOU" : ""), skin);
            posLbl.setFontScale(1.5f);
            posLbl.setColor(entry.isPlayer ? new Color(1f, 0.9f, 0.2f, 1f) : Color.WHITE);

            // Format time
            float t    = entry.value;
            int   mins = (int)(t / 60f);
            float secs = t - mins * 60f;
            Label timeLbl = new Label(String.format("%d:%05.2f", mins, secs), skin);
            timeLbl.setFontScale(1.3f);
            timeLbl.setColor(0.8f, 0.8f, 0.8f, 1f);

            root.add(posLbl).left().padLeft(60).padBottom(12);
            root.add(timeLbl).right().padRight(60).padBottom(12).row();
        }

        root.add().colspan(2).height(30).row();

        // ── Buttons ───────────────────────────────────────────────────────────
        TextButton playAgainBtn = new TextButton("  Race Again  ", skin, "accent");
        playAgainBtn.getLabel().setFontScale(1.6f);
        playAgainBtn.pad(12, 36, 12, 36);
        playAgainBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.getStateStack().push(new CharacterSelectScreen(game));
            }
        });
        root.add(playAgainBtn).colspan(2).width(280).padBottom(16).row();

        TextButton menuBtn = new TextButton("Main Menu", skin, "back");
        menuBtn.getLabel().setFontScale(1.3f);
        menuBtn.pad(10, 28, 10, 28);
        menuBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.getStateStack().replaceAll(new MenuScreen(game));
            }
        });
        root.add(menuBtn).colspan(2).width(220).padBottom(24).row();

        stage.addActor(root);
    }

    @Override
    protected void onRender(float delta) {
        time += delta;
        float pulse = 0.5f + 0.5f * MathUtils.sin(time * 0.6f);
        Gdx.gl.glClearColor(0.00f, 0.00f, 0.39f + 0.04f * pulse, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
