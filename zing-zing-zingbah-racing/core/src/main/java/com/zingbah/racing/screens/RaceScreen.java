package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.zingbah.racing.ZingbahRacing;
import com.zingbah.racing.characters.BoohbahCharacter;
import com.zingbah.racing.engine.Kart;
import com.zingbah.racing.engine.RacingEngine;

/**
 * Race screen with a low-resolution faux-3D road pass and full-resolution HUD.
 */
public class RaceScreen extends BaseScreen {

    private final RacingEngine engine;
    private PixelRenderer pixels;
    private Mode7Renderer mode7;

    private float camYaw = 0f;
    private static final float YAW_LAG = 6f;

    private Label posLabel, lapLabel, speedLabel, itemLabel, countdownLabel, msgLabel;
    private float msgTimer = 0f;
    private boolean transitioned = false;

    public RaceScreen(ZingbahRacing game, BoohbahCharacter playerChar) {
        super(game);
        engine = new RacingEngine(playerChar);
    }

    @Override
    protected void onShow() {
        Gdx.input.setInputProcessor(null);
        pixels = new PixelRenderer();
        mode7 = new Mode7Renderer(PixelRenderer.FB_W, PixelRenderer.FB_H);
        camYaw = engine.getPlayerKart().heading;
        buildHUD();
    }

    private void buildHUD() {
        Table tl = new Table();
        tl.setFillParent(true);
        tl.top().left().pad(14);
        posLabel = new Label("P1", skin, "title");
        posLabel.setFontScale(2.4f);
        lapLabel = new Label("LAP 1/" + RacingEngine.TOTAL_LAPS, skin);
        lapLabel.setFontScale(1.2f);
        tl.add(posLabel).left().row();
        tl.add(lapLabel).left().padTop(2).row();
        stage.addActor(tl);

        Table tr = new Table();
        tr.setFillParent(true);
        tr.top().right().pad(14);
        speedLabel = new Label("0 km/h", skin);
        speedLabel.setFontScale(1.2f);
        tr.add(speedLabel).right().row();
        stage.addActor(tr);

        Table bl = new Table();
        bl.setFillParent(true);
        bl.bottom().left().pad(14);
        itemLabel = new Label("None", skin);
        itemLabel.setFontScale(1.1f);
        bl.add(new Label("ITEM: ", skin)).padRight(4);
        bl.add(itemLabel);
        stage.addActor(bl);

        Table cc = new Table();
        cc.setFillParent(true);
        cc.top().padTop(16);
        countdownLabel = new Label("", skin, "title");
        countdownLabel.setFontScale(5f);
        countdownLabel.setAlignment(Align.center);
        msgLabel = new Label("", skin, "title");
        msgLabel.setFontScale(2f);
        msgLabel.setAlignment(Align.center);
        cc.add(countdownLabel).expandX().row();
        cc.add(msgLabel).expandX().row();
        stage.addActor(cc);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            game.getStateStack().replaceAll(new MenuScreen(game));
            return;
        }

        engine.update(delta);

        Kart player = engine.getPlayerKart();
        float diff = player.heading - camYaw;
        while (diff > 180f) {
            diff -= 360f;
        }
        while (diff < -180f) {
            diff += 360f;
        }
        camYaw += diff * YAW_LAG * delta;

        refreshHUD();
        checkTransition();

        final float CAM_BACK = 4.2f;
        final float CAM_LOOK_AHEAD = 1.4f;
        float camRad = camYaw * com.badlogic.gdx.math.MathUtils.degreesToRadians;
        float dirX = com.badlogic.gdx.math.MathUtils.cos(camRad);
        float dirZ = com.badlogic.gdx.math.MathUtils.sin(camRad);
        float camX = player.position.x - dirX * CAM_BACK + dirX * CAM_LOOK_AHEAD;
        float camZ = player.position.z - dirZ * CAM_BACK + dirZ * CAM_LOOK_AHEAD;

        pixels.begin();
        mode7.render(
                engine.getTrack(),
                engine.getAllKarts(),
                engine.getItemBoxes(),
                camX,
                camZ,
                camYaw,
                delta
        );
        pixels.end();

        stage.act(delta);
        stage.draw();

        if (msgTimer > 0 && (msgTimer -= delta) <= 0) {
            msgLabel.setText("");
        }
    }

    private void refreshHUD() {
        Kart p = engine.getPlayerKart();

        countdownLabel.setText(engine.phase == RacingEngine.Phase.COUNTDOWN
                ? engine.getCountdownText() : "");

        int pos = engine.getPlayerPosition();
        posLabel.setText(pos + ordinal(pos));
        lapLabel.setText("LAP " + Math.min(p.laps + 1, RacingEngine.TOTAL_LAPS)
                + "/" + RacingEngine.TOTAL_LAPS);
        speedLabel.setText((int) (Math.abs(p.speed) * 5.4f) + " km/h");

        if (p.activePowerUp != null) {
            itemLabel.setText("[ON] " + p.activePowerUp.displayName);
        } else if (!p.itemQueue.isEmpty()) {
            itemLabel.setText(p.itemQueue.peek().displayName + " (SPACE)");
        } else {
            itemLabel.setText("None");
        }

        if (p.isShrunk && msgTimer <= 0) {
            showMsg("You shrunk!", 2f);
        }
    }

    private void showMsg(String t, float d) {
        msgLabel.setText(t);
        msgTimer = d;
    }

    private void checkTransition() {
        if (!transitioned && engine.isFinished()) {
            transitioned = true;
            Kart p = engine.getPlayerKart();
            showMsg(p.finishPosition == 1 ? "YOU WIN!"
                    : "Finished " + p.finishPosition + ordinal(p.finishPosition) + "!", 10f);
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    Gdx.app.postRunnable(() ->
                            game.getStateStack().push(new ResultScreen(game, engine.getResult())));
                }
            }, 2.5f);
        }
    }

    private String ordinal(int n) {
        if (n == 1) {
            return "st";
        }
        if (n == 2) {
            return "nd";
        }
        if (n == 3) {
            return "rd";
        }
        return "th";
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        if (pixels != null) {
            pixels.resize(w, h);
        }
    }

    @Override
    protected void onDispose() {
        engine.dispose();
        if (pixels != null) {
            pixels.dispose();
        }
        if (mode7 != null) {
            mode7.dispose();
        }
    }
}
