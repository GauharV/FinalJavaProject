package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.zingbah.racing.ZingbahRacing;
import com.zingbah.racing.characters.BoohbahCharacter;

/**
 * Character selection — entirely 2D.
 * The kart "preview" is a few coloured rectangles drawn with SpriteBatch.
 * No LibGDX 3D anywhere.
 */
public class CharacterSelectScreen extends BaseScreen {

    private final BoohbahCharacter[] CHARS = BoohbahCharacter.values();
    private int selectedIndex = 0;

    // 2D preview
    private SpriteBatch previewBatch;
    private Texture     white;
    private float       previewAngle = 0f;

    // Changing labels
    private Label   nameLabel, taglineLabel;
    private Label[] statBars = new Label[3];

    public CharacterSelectScreen(ZingbahRacing game) {
        super(game);
    }

    @Override
    protected void onShow() {
        // 1×1 white texture for tinted rectangles
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        white       = new Texture(pm); pm.dispose();
        previewBatch = new SpriteBatch();

        buildUI();
        refreshCharacter();
    }

    // ── UI ────────────────────────────────────────────────────────────────────

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(20);

        // Title
        Label title = new Label("SELECT YOUR BOOHBAH", skin, "title");
        title.setFontScale(1.8f);
        root.add(title).colspan(3).padBottom(16).row();

        // Arrow ← Name → row
        TextButton left = btn("  <  ");
        left.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                selectedIndex = (selectedIndex - 1 + CHARS.length) % CHARS.length;
                refreshCharacter();
            }
        });
        TextButton right = btn("  >  ");
        right.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                selectedIndex = (selectedIndex + 1) % CHARS.length;
                refreshCharacter();
            }
        });

        nameLabel = new Label("", skin, "title");
        nameLabel.setFontScale(2.0f);

        root.add(left).width(70).padRight(16);
        root.add(nameLabel).expandX();
        root.add(right).width(70).padLeft(16).row();

        // Tagline
        taglineLabel = new Label("", skin);
        taglineLabel.setFontScale(1.0f);
        root.add(taglineLabel).colspan(3).padBottom(10).row();

        // Stats
        Table stats = new Table();
        String[] names = { "TOP SPEED", "ACCEL", "HANDLING" };
        for (int i = 0; i < 3; i++) {
            stats.add(new Label(names[i] + "  ", skin)).right().padRight(6);
            statBars[i] = new Label("", skin);
            stats.add(statBars[i]).left().padBottom(4).row();
        }
        root.add(stats).colspan(3).padBottom(16).row();

        // RACE button
        TextButton race = new TextButton("  RACE!  ", skin, "accent");
        race.getLabel().setFontScale(1.8f);
        race.pad(12, 36, 12, 36);
        race.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.getStateStack().push(new RaceScreen(game, CHARS[selectedIndex]));
            }
        });
        root.add(race).colspan(3).width(260).padBottom(12).row();

        // Back button
        TextButton back = new TextButton("Back", skin, "back");
        back.pad(8, 24, 8, 24);
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                game.getStateStack().pop();
            }
        });
        root.add(back).colspan(3).width(160).padBottom(12).row();

        // Reserve bottom 35% for the 2D kart preview
        root.add().colspan(3).expandY();

        stage.addActor(root);
    }

    private TextButton btn(String text) {
        TextButton b = new TextButton(text, skin);
        b.getLabel().setFontScale(1.8f);
        return b;
    }

    // ── Refresh on character change ───────────────────────────────────────────

    private void refreshCharacter() {
        BoohbahCharacter c = CHARS[selectedIndex];
        nameLabel.setText(c.displayName);
        taglineLabel.setText(c.tagline);
        int[] ratings = { c.topSpeed, c.acceleration, c.handling };
        for (int i = 0; i < 3; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 5; j++) sb.append(j < ratings[i] ? "█ " : "░ ");
            statBars[i].setText(sb.toString());
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    protected void onRender(float delta) {
        previewAngle += 60f * delta;

        Gdx.gl.glClearColor(0.00f, 0.00f, 0.39f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw 2D kart preview in the bottom portion of the screen
        int   W  = Gdx.graphics.getWidth();
        int   H  = Gdx.graphics.getHeight();
        float cx = W * 0.5f;
        float cy = H * 0.18f;

        BoohbahCharacter c = CHARS[selectedIndex];
        Color col = c.color;

        // Bounce the kart slightly
        float bounce = MathUtils.sin(previewAngle * MathUtils.degreesToRadians * 2f) * 4f;

        float bw = 140f, bh = 56f;  // body width/height

        previewBatch.begin();

        // Shadow
        previewBatch.setColor(0f, 0f, 0f, 0.25f);
        previewBatch.draw(white, cx - bw/2f + 8, cy - 12 + bounce, bw - 16, 8);

        // Body
        previewBatch.setColor(col.r, col.g, col.b, 1f);
        previewBatch.draw(white, cx - bw/2f, cy + bounce, bw, bh * 0.55f);

        // Cab
        previewBatch.setColor(col.r * 0.55f, col.g * 0.55f, col.b * 0.55f, 1f);
        previewBatch.draw(white, cx - bw * 0.32f, cy + bh * 0.55f + bounce, bw * 0.64f, bh * 0.45f);

        // Wheels
        previewBatch.setColor(0.1f, 0.1f, 0.1f, 1f);
        float ww = bw * 0.18f, wh = bh * 0.38f;
        previewBatch.draw(white, cx - bw/2f - 6,      cy + bounce,          ww, wh);
        previewBatch.draw(white, cx + bw/2f + 6 - ww, cy + bounce,          ww, wh);
        previewBatch.draw(white, cx - bw/2f - 6,      cy + wh + 4 + bounce, ww, wh * 0.8f);
        previewBatch.draw(white, cx + bw/2f + 6 - ww, cy + wh + 4 + bounce, ww, wh * 0.8f);

        previewBatch.end();
    }

    // ── Dispose ───────────────────────────────────────────────────────────────

    @Override
    protected void onDispose() {
        previewBatch.dispose();
        white.dispose();
    }
}
