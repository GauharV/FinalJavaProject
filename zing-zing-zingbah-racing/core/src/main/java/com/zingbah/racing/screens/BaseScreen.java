package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.zingbah.racing.ZingbahRacing;

/**
 * Shared base for all screens. Provides a programmatic Skin, Stage, and
 * common UI helpers so no external atlas files are required.
 */
public abstract class BaseScreen implements Screen {

    protected final ZingbahRacing game;
    protected       Stage         stage;
    protected       Skin          skin;
    protected       SpriteBatch   batch;
    protected       BitmapFont    font;
    protected       BitmapFont    bigFont;
    protected       GlyphLayout   layout;

    private Texture whiteTexture;

    public BaseScreen(ZingbahRacing game) {
        this.game = game;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void show() {
        batch   = new SpriteBatch();
        font    = new BitmapFont();
        bigFont = new BitmapFont();
        bigFont.getData().setScale(2.5f);
        layout  = new GlyphLayout();
        stage   = new Stage(new ScreenViewport());
        skin    = buildSkin();
        Gdx.input.setInputProcessor(stage);
        onShow();
    }

    /** Subclasses set up their UI here. */
    protected abstract void onShow();

    @Override
    public void render(float delta) {
        onRender(delta);
        stage.act(delta);
        stage.draw();
    }

    /** Extra render logic after the stage is drawn (e.g., SpriteBatch overlays). */
    protected void onRender(float delta) {}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        batch.dispose();
        font.dispose();
        bigFont.dispose();
        if (whiteTexture != null) whiteTexture.dispose();
        onDispose();
    }

    protected void onDispose() {}

    // ── Skin builder ──────────────────────────────────────────────────────────

    private Skin buildSkin() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        whiteTexture = new Texture(pm);
        pm.dispose();

        Skin s = new Skin();
        s.add("white", whiteTexture);
        s.add("default-font", font);

        TextureRegionDrawable white = new TextureRegionDrawable(whiteTexture);

        // ── Button style ──────────────────────────────────────────────────────
        TextButton.TextButtonStyle btn = new TextButton.TextButtonStyle();
        btn.font      = font;
        btn.fontColor = Color.WHITE;
        btn.up        = white.tint(new Color(0.25f, 0.25f, 0.75f, 0.95f));
        btn.over      = white.tint(new Color(0.40f, 0.40f, 0.95f, 0.95f));
        btn.down      = white.tint(new Color(0.15f, 0.15f, 0.55f, 1.00f));
        s.add("default", btn);

        // Accent button (GO / race start)
        TextButton.TextButtonStyle accentBtn = new TextButton.TextButtonStyle();
        accentBtn.font      = font;
        accentBtn.fontColor = Color.WHITE;
        accentBtn.up        = white.tint(new Color(0.10f, 0.60f, 0.10f, 0.95f));
        accentBtn.over      = white.tint(new Color(0.20f, 0.80f, 0.20f, 0.95f));
        accentBtn.down      = white.tint(new Color(0.05f, 0.40f, 0.05f, 1.00f));
        s.add("accent", accentBtn);

        // Back button (muted red)
        TextButton.TextButtonStyle backBtn = new TextButton.TextButtonStyle();
        backBtn.font      = font;
        backBtn.fontColor = Color.WHITE;
        backBtn.up        = white.tint(new Color(0.55f, 0.10f, 0.10f, 0.95f));
        backBtn.over      = white.tint(new Color(0.75f, 0.20f, 0.20f, 0.95f));
        backBtn.down      = white.tint(new Color(0.35f, 0.05f, 0.05f, 1.00f));
        s.add("back", backBtn);

        // ── Label styles ──────────────────────────────────────────────────────
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        s.add("default", labelStyle);

        Label.LabelStyle titleStyle = new Label.LabelStyle(font, new Color(1f, 0.85f, 0.1f, 1f));
        s.add("title", titleStyle);

        return s;
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    protected void drawCenteredText(BitmapFont f, String text, float y, Color color) {
        layout.setText(f, text);
        f.setColor(color);
        f.draw(batch, text,
                (Gdx.graphics.getWidth() - layout.width) / 2f, y);
    }

    protected void fillRect(float x, float y, float w, float h, Color color) {
        batch.setColor(color);
        batch.draw(whiteTexture, x, y, w, h);
        batch.setColor(Color.WHITE);
    }
}
