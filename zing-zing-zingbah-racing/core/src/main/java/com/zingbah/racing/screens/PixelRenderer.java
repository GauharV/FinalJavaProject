package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Low-resolution framebuffer.
 * Render all Mode 7 content between begin() and end().
 * end() upscales with nearest-neighbour to the full screen window.
 *
 * At 1280×720 each internal pixel becomes a 5×5 block.
 */
public class PixelRenderer implements Disposable {

    public static final int FB_W = 256;
    public static final int FB_H = 144;

    private final FrameBuffer        fb;
    private final SpriteBatch        blit;
    private final OrthographicCamera screenCam;

    public PixelRenderer() {
        // No depth buffer — Mode 7 doesn't do 3D depth testing
        fb = new FrameBuffer(Pixmap.Format.RGB888, FB_W, FB_H, false);
        fb.getColorBufferTexture().setFilter(
                Texture.TextureFilter.Nearest,
                Texture.TextureFilter.Nearest);

        blit      = new SpriteBatch();
        screenCam = new OrthographicCamera();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void resize(int w, int h) {
        screenCam.setToOrtho(false, w, h);
        screenCam.update();
        blit.setProjectionMatrix(screenCam.combined);
    }

    /** Bind the low-res framebuffer. Render Mode 7 content after this. */
    public void begin() {
        fb.begin();
        Gdx.gl.glViewport(0, 0, FB_W, FB_H);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /** Unbind framebuffer and upscale its contents to the screen. */
    public void end() {
        fb.end();
        int sw = Gdx.graphics.getWidth(), sh = Gdx.graphics.getHeight();
        Gdx.gl.glViewport(0, 0, sw, sh);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        blit.begin();
        // Negative height flips the framebuffer (GL stores it upside-down)
        blit.draw(fb.getColorBufferTexture(), 0, sh, sw, -sh);
        blit.end();
    }

    @Override
    public void dispose() {
        fb.dispose();
        blit.dispose();
    }
}
