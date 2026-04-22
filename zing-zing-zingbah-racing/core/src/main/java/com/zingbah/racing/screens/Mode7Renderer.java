package com.zingbah.racing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.zingbah.racing.data.PowerUp;
import com.zingbah.racing.engine.ItemBox;
import com.zingbah.racing.engine.Kart;
import com.zingbah.racing.engine.Track;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Mode7Renderer implements Disposable {

    public static final float HORIZON = 0.58f;
    public static final float FOV = 0.72f;

    private static final float SKY_R = 0.26f, SKY_G = 0.45f, SKY_B = 0.94f;
    private static final float KART_W = 1.6f;
    private static final float KART_H = 1.0f;
    private static final float ITEM_W = 1.2f;
    private static final float GUIDE_W = 3.2f;
    private static final float GUIDE_H = 2.0f;
    private static final float BARRIER_W = 1.7f;
    private static final float BARRIER_H = 2.9f;

    private static final float PLAYER_SCREEN_Y = 0.15f;
    private static final float PLAYER_LATERAL_FOLLOW = 0.32f;
    private static final float PLAYER_VISUAL_FWD = 2.8f;

    private static final String VERT =
            "attribute vec2 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" +
                    "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
                    "varying vec2 v_uv;\n" +
                    "void main(){\n" +
                    "  v_uv = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +
                    "  gl_Position = vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ",0.0,1.0);\n" +
                    "}\n";

    private static final String FRAG =
            "#ifdef GL_ES\nprecision mediump float;\n#endif\n" +
                    "uniform sampler2D u_track;\n" +
                    "uniform float u_camX,u_camZ,u_cosA,u_sinA,u_horizon,u_fov,u_wh;\n" +
                    "varying vec2 v_uv;\n" +
                    "void main(){\n" +
                    "  if(v_uv.y>=u_horizon){\n" +
                    "    float t=(v_uv.y-u_horizon)/(1.0-u_horizon);\n" +
                    "    gl_FragColor=vec4(mix(vec3(" + SKY_R + "," + SKY_G + "," + SKY_B + "),vec3(0.10,0.18,0.55),t),1.0);\n" +
                    "    return;\n" +
                    "  }\n" +
                    "  float dh=u_horizon-v_uv.y;\n" +
                    "  float sc=u_fov/max(dh,0.0001);\n" +
                    "  float dx=v_uv.x-0.5;\n" +
                    "  float wx=u_camX+(u_cosA+u_sinA*dx)*sc;\n" +
                    "  float wz=u_camZ+(u_sinA-u_cosA*dx)*sc;\n" +
                    "  float wf=u_wh*2.0;\n" +
                    "  vec2 uv=vec2((wx+u_wh)/wf,(u_wh-wz)/wf);\n" +
                    "  if(uv.x<0.0||uv.x>1.0||uv.y<0.0||uv.y>1.0){\n" +
                    "    gl_FragColor=vec4(0.0,0.47,0.0,1.0);\n" +
                    "    return;\n" +
                    "  }\n" +
                    "  gl_FragColor=texture2D(u_track,uv);\n" +
                    "}\n";

    private final ShaderProgram shader;
    private final Mesh mesh;
    private final SpriteBatch spriteBatch;
    private final OrthographicCamera spriteCam;
    private final Texture white;

    private final int fbW;
    private final int fbH;
    private float time = 0f;

    public Mode7Renderer(int fbW, int fbH) {
        this.fbW = fbW;
        this.fbH = fbH;

        ShaderProgram.pedantic = false;
        shader = new ShaderProgram(VERT, FRAG);
        if (!shader.isCompiled()) {
            throw new RuntimeException("Mode7 shader:\n" + shader.getLog());
        }

        mesh = new Mesh(true, 4, 6,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));
        mesh.setVertices(new float[]{-1, -1, 0, 0, 1, -1, 1, 0, 1, 1, 1, 1, -1, 1, 0, 1});
        mesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});

        spriteCam = new OrthographicCamera();
        spriteCam.setToOrtho(false, fbW, fbH);
        spriteCam.update();
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(spriteCam.combined);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        white = new Texture(pm);
        pm.dispose();
    }

    public void render(Track track, List<Kart> karts, List<ItemBox> itemBoxes,
                       float camX, float camZ, float camAngle, float delta) {
        time += delta;
        float cosA = MathUtils.cosDeg(camAngle);
        float sinA = MathUtils.sinDeg(camAngle);

        renderFloor(track, camX, camZ, cosA, sinA);
        renderSprites(track, karts, itemBoxes, camX, camZ, cosA, sinA);

        Kart player = karts.stream().filter(k -> k.isPlayer).findFirst().orElse(null);
        if (player != null && player.activePowerUp != null) {
            renderPowerUpEffect(player.activePowerUp, player.powerUpTimer);
        }
    }

    private void renderFloor(Track track, float camX, float camZ, float cosA, float sinA) {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        track.getTexture().bind(0);
        shader.begin();
        shader.setUniformi("u_track", 0);
        shader.setUniformf("u_camX", camX);
        shader.setUniformf("u_camZ", camZ);
        shader.setUniformf("u_cosA", cosA);
        shader.setUniformf("u_sinA", sinA);
        shader.setUniformf("u_horizon", HORIZON);
        shader.setUniformf("u_fov", FOV);
        shader.setUniformf("u_wh", Track.WORLD_HALF);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    private void renderSprites(Track track, List<Kart> karts, List<ItemBox> itemBoxes,
                               float camX, float camZ, float cosA, float sinA) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        spriteBatch.begin();

        renderBarrierMarkers(track, camX, camZ, cosA, sinA);
        renderGuideMarkers(track, camX, camZ, cosA, sinA);

        for (ItemBox box : itemBoxes) {
            if (!box.active) {
                continue;
            }
            float[] p = project(box.position.x, box.position.z, camX, camZ, cosA, sinA);
            if (p == null) {
                continue;
            }
            float sz = (FOV / p[2]) * ITEM_W * fbH;
            float pulse = 0.75f + 0.25f * MathUtils.sin(time * 8f);
            spriteBatch.setColor(pulse, pulse * 0.78f, 0.05f, 1f);
            spriteBatch.draw(white, p[0] - sz / 2f, p[1], sz, sz);
            spriteBatch.setColor(1f, 1f, 1f, 0.6f * pulse);
            spriteBatch.draw(white, p[0] - sz * 0.2f, p[1] + sz * 0.25f, sz * 0.4f, sz * 0.5f);
        }

        Kart player = null;
        List<Kart> sorted = new ArrayList<>();
        for (Kart kart : karts) {
            if (kart.isPlayer) {
                player = kart;
            } else {
                sorted.add(kart);
            }
        }
        sorted.sort(Comparator.comparingDouble(k -> -fwdDist(k, camX, camZ, cosA, sinA)));

        for (Kart kart : sorted) {
            float[] p = project(kart.position.x, kart.position.z, camX, camZ, cosA, sinA);
            if (p == null) {
                continue;
            }
            drawKartSprite(kart, p[0], p[1], p[2]);
        }

        if (player != null) {
            float[] projected = project(player.position.x, player.position.z, camX, camZ, cosA, sinA);
            float playerX = fbW * 0.5f;
            if (projected != null) {
                playerX = MathUtils.lerp(fbW * 0.5f, projected[0], PLAYER_LATERAL_FOLLOW);
            }
            drawKartSprite(player, playerX, fbH * PLAYER_SCREEN_Y, PLAYER_VISUAL_FWD);
        }

        spriteBatch.end();
    }

    private void renderGuideMarkers(Track track, float camX, float camZ, float cosA, float sinA) {
        for (Track.GuideMarker marker : track.getGuideMarkers()) {
            float[] p = project(marker.position.x, marker.position.z, camX, camZ, cosA, sinA);
            if (p == null) {
                continue;
            }
            drawGuideChevron(p[0], p[1], p[2], marker.pointRight);
        }
    }

    private void renderBarrierMarkers(Track track, float camX, float camZ, float cosA, float sinA) {
        for (com.badlogic.gdx.math.Vector3 marker : track.getBarrierMarkers()) {
            float[] p = project(marker.x, marker.z, camX, camZ, cosA, sinA);
            if (p == null) {
                continue;
            }
            drawBarrierPost(p[0], p[1], p[2]);
        }
    }

    private void drawBarrierPost(float sx, float sy, float fwd) {
        float sw = (FOV / fwd) * BARRIER_W * fbW;
        float sh = (FOV / fwd) * BARRIER_H * fbH;
        if (sw < 2f || sh < 3f) {
            return;
        }

        spriteBatch.setColor(0f, 0f, 0f, 0.22f);
        spriteBatch.draw(white, sx - sw * 0.40f, sy - sh * 0.08f, sw * 0.80f, sh * 0.12f);

        spriteBatch.setColor(0.92f, 0.92f, 0.95f, 1f);
        spriteBatch.draw(white, sx - sw / 2f, sy, sw, sh);

        spriteBatch.setColor(0.85f, 0.08f, 0.08f, 1f);
        spriteBatch.draw(white, sx - sw / 2f, sy + sh * 0.08f, sw, sh * 0.18f);
        spriteBatch.draw(white, sx - sw / 2f, sy + sh * 0.42f, sw, sh * 0.18f);
        spriteBatch.draw(white, sx - sw / 2f, sy + sh * 0.76f, sw, sh * 0.18f);
    }

    private void drawGuideChevron(float sx, float sy, float fwd, boolean pointRight) {
        float sw = (FOV / fwd) * GUIDE_W * fbW;
        float sh = (FOV / fwd) * GUIDE_H * fbH;
        if (sw < 3f || sh < 2f) {
            return;
        }

        float spacing = sw * 0.24f;
        float start = sx - spacing;
        for (int i = 0; i < 3; i++) {
            float cx = start + i * spacing;
            drawSingleChevron(cx, sy + sh * 0.18f, sw * 0.34f, sh * 0.62f, pointRight);
        }
    }

    private void drawSingleChevron(float cx, float cy, float w, float h, boolean pointRight) {
        float t = Math.max(1f, w * 0.22f);
        float left = cx - w / 2f;
        float right = cx + w / 2f;
        float innerLeft = left + t;
        float innerRight = right - t;
        float top = cy + h / 2f;
        float mid = cy;
        float bottom = cy - h / 2f;
        float tip = pointRight ? right : left;
        float innerTip = pointRight ? innerRight : innerLeft;
        float tail = pointRight ? left : right;
        float innerTail = pointRight ? innerLeft : innerRight;

        spriteBatch.setColor(0.18f, 0.95f, 0.98f, 0.28f);
        spriteBatch.draw(white, tail - t, bottom - t, w + t * 2f, h + t * 2f);

        spriteBatch.setColor(0.22f, 0.96f, 0.98f, 0.88f);
        drawLineSegment(tail, top, tip, mid, t);
        drawLineSegment(tail, bottom, tip, mid, t);

        spriteBatch.setColor(1f, 1f, 1f, 0.92f);
        drawLineSegment(innerTail, top - t * 0.3f, innerTip, mid, Math.max(1f, t * 0.45f));
        drawLineSegment(innerTail, bottom + t * 0.3f, innerTip, mid, Math.max(1f, t * 0.45f));
    }

    private void drawLineSegment(float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) {
            return;
        }
        float angle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
        spriteBatch.draw(white, x1, y1 - thickness / 2f, 0f, thickness / 2f, len, thickness, 1f, 1f, angle,
                0, 0, 1, 1, false, false);
    }

    private void drawKartSprite(Kart k, float sx, float sy, float fwd) {
        float sw = (FOV / fwd) * KART_W * fbW;
        float sh = (FOV / fwd) * KART_H * fbH;
        if (sw < 2f || sh < 1f) {
            return;
        }

        Color col = k.character.color;

        spriteBatch.setColor(0f, 0f, 0f, 0.22f);
        spriteBatch.draw(white, sx - sw * 0.44f, sy - sh * 0.10f, sw * 0.88f, sh * 0.13f);

        spriteBatch.setColor(0.08f, 0.08f, 0.08f, 1f);
        float rww = sw * 0.18f;
        float rwh = sh * 0.30f;
        spriteBatch.draw(white, sx - sw / 2f - 1f, sy, rww, rwh);
        spriteBatch.draw(white, sx + sw / 2f - rww + 1f, sy, rww, rwh);

        spriteBatch.setColor(col.r, col.g, col.b, 1f);
        spriteBatch.draw(white, sx - sw / 2f, sy, sw, sh * 0.52f);

        spriteBatch.setColor(col.r * 0.50f, col.g * 0.50f, col.b * 0.50f, 1f);
        spriteBatch.draw(white, sx - sw * 0.28f, sy + sh * 0.52f, sw * 0.56f, sh * 0.48f);

        spriteBatch.setColor(0.08f, 0.08f, 0.08f, 1f);
        float fww = sw * 0.15f;
        float fwh = sh * 0.22f;
        spriteBatch.draw(white, sx - sw / 2f, sy + rwh + 1f, fww, fwh);
        spriteBatch.draw(white, sx + sw / 2f - fww, sy + rwh + 1f, fww, fwh);

        if (sw > 10f) {
            spriteBatch.setColor(0.8f, 0.95f, 1f, 0.55f);
            spriteBatch.draw(white, sx - sw * 0.22f, sy + sh * 0.52f + sh * 0.30f, sw * 0.44f, Math.max(1f, sh * 0.08f));
        }

        if (k.activePowerUp != null) {
            float[] rgb = powerUpColor(k.activePowerUp);
            spriteBatch.setColor(rgb[0], rgb[1], rgb[2], 0.9f);
            float hs = Math.max(4f, sw * 0.28f);
            spriteBatch.draw(white, sx - hs / 2f, sy + sh + 2f, hs, hs * 0.55f);
        }
    }

    private void renderPowerUpEffect(PowerUp pu, float timeLeft) {
        spriteBatch.begin();
        float pulse = 0.5f + 0.5f * MathUtils.sin(time * 10f);
        float alpha = Math.min(timeLeft, 1f) * 0.45f;

        switch (pu.effect) {
            case ORANGE_TRAIL:
                spriteBatch.setColor(1f, 0.45f, 0f, alpha * pulse);
                spriteBatch.draw(white, 0, 0, fbW * 0.12f, fbH);
                spriteBatch.draw(white, fbW * 0.88f, 0, fbW * 0.12f, fbH);
                break;
            case RED_FLASH:
                spriteBatch.setColor(1f, 0.05f, 0.05f, alpha * (0.4f + 0.6f * pulse));
                drawBorder(6f);
                break;
            case GOLD_SHIMMER:
                spriteBatch.setColor(1f, 0.85f, 0f, alpha * pulse);
                drawBorder(5f);
                break;
            case PURPLE_TINT:
                spriteBatch.setColor(0.5f, 0f, 0.8f, 0.20f);
                spriteBatch.draw(white, 0, 0, fbW, fbH);
                break;
            case YELLOW_SPIN:
                spriteBatch.setColor(1f, 0.9f, 0f, alpha * pulse * 0.5f);
                spriteBatch.draw(white, 0, 0, fbW, fbH);
                break;
            case BLUE_FREEZE:
                spriteBatch.setColor(0.1f, 0.4f, 1f, 0.25f);
                spriteBatch.draw(white, 0, 0, fbW, fbH);
                spriteBatch.setColor(0.0f, 0.6f, 1f, alpha);
                drawBorder(7f);
                break;
            case WHITE_GLOW:
                spriteBatch.setColor(1f, 1f, 1f, alpha * pulse);
                drawBorder(6f);
                spriteBatch.setColor(1f, 1f, 1f, 0.08f);
                spriteBatch.draw(white, 0, 0, fbW, fbH);
                break;
            default:
                break;
        }

        spriteBatch.end();
    }

    private void drawBorder(float t) {
        spriteBatch.draw(white, 0, 0, fbW, t);
        spriteBatch.draw(white, 0, fbH - t, fbW, t);
        spriteBatch.draw(white, 0, 0, t, fbH);
        spriteBatch.draw(white, fbW - t, 0, t, fbH);
    }

    private float[] project(float wx, float wz, float camX, float camZ, float cosA, float sinA) {
        float dx = wx - camX;
        float dz = wz - camZ;
        float fwd = dx * cosA + dz * sinA;
        float rgt = dx * sinA - dz * cosA;
        if (fwd < 0.5f) {
            return null;
        }

        float u = 0.5f + rgt / fwd;
        float v = HORIZON - FOV / fwd;
        if (u < -0.5f || u > 1.5f) {
            return null;
        }
        if (v < 0f || v > HORIZON) {
            return null;
        }

        return new float[]{u * fbW, v * fbH, fwd};
    }

    private double fwdDist(Kart k, float camX, float camZ, float cosA, float sinA) {
        return (k.position.x - camX) * cosA + (k.position.z - camZ) * sinA;
    }

    private float[] powerUpColor(PowerUp pu) {
        switch (pu) {
            case SPEED_BOOST: return new float[]{1f, 0.45f, 0f};
            case MEGA_TURBO: return new float[]{1f, 0.05f, 0.05f};
            case STAR_SHIELD: return new float[]{1f, 0.85f, 0f};
            case SHRINK_RAY: return new float[]{0.6f, 0f, 0.9f};
            case BANANA_PEEL: return new float[]{1f, 0.9f, 0f};
            case ICE_BLAST: return new float[]{0.1f, 0.6f, 1f};
            case GHOST: return new float[]{0.9f, 0.9f, 0.9f};
            default: return new float[]{1f, 1f, 1f};
        }
    }

    @Override
    public void dispose() {
        shader.dispose();
        mesh.dispose();
        spriteBatch.dispose();
        white.dispose();
    }
}
