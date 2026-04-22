package com.zingbah.racing.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.zingbah.racing.ZingbahRacing;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Zing Zing Zingbah Racing");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.setResizable(true);
        // Starting position: centered on primary monitor
        config.setWindowPosition(-1, -1);
        new Lwjgl3Application(new ZingbahRacing(), config);
    }
}
