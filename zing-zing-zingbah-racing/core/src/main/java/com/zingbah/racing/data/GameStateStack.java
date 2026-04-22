package com.zingbah.racing.data;

import com.badlogic.gdx.Screen;
import com.zingbah.racing.ZingbahRacing;

import java.util.Stack;

/**
 * Manages the game's screen history using a Stack.
 * Push a new screen to navigate forward; pop to go back.
 * Satisfies the Java 2 Stack requirement.
 */
public class GameStateStack {

    private final Stack<Screen> states = new Stack<>();
    private final ZingbahRacing game;

    public GameStateStack(ZingbahRacing game) {
        this.game = game;
    }

    /**
     * Navigate to a new screen, keeping this one on the stack.
     */
    public void push(Screen screen) {
        states.push(screen);
        game.setScreen(screen);
    }

    /**
     * Go back to the previous screen.
     * If there's only one screen left, stay on it.
     */
    public Screen pop() {
        if (states.size() <= 1) return states.isEmpty() ? null : states.peek();
        Screen popped = states.pop();
        if (!states.isEmpty()) {
            game.setScreen(states.peek());
        }
        return popped;
    }

    /** Replace all screens with a single new one (e.g., for race restart). */
    public void replaceAll(Screen screen) {
        states.clear();
        push(screen);
    }

    public Screen peek()    { return states.isEmpty() ? null : states.peek(); }
    public boolean isEmpty(){ return states.isEmpty(); }
    public int     size()   { return states.size();    }
}
