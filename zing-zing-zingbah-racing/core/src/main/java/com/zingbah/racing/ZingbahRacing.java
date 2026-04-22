package com.zingbah.racing;

import com.badlogic.gdx.Game;
import com.zingbah.racing.data.GameStateStack;
import com.zingbah.racing.screens.MenuScreen;

/**
 * Application entry point.
 * Owns the GameStateStack so every screen can navigate freely.
 */
public class ZingbahRacing extends Game {

    private GameStateStack stateStack;

    @Override
    public void create() {
        stateStack = new GameStateStack(this);
        stateStack.push(new MenuScreen(this));
    }

    public GameStateStack getStateStack() {
        return stateStack;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
