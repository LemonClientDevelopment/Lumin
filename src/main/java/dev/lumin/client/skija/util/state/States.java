package dev.lumin.client.skija.util.state;

import org.lwjgl.opengl.GL30;

import java.util.Stack;

import static org.lwjgl.opengl.GL30.GL_MAJOR_VERSION;
import static org.lwjgl.opengl.GL30.GL_MINOR_VERSION;

public final class States {

    public static final States INSTANCE = new States();
    private static final int glVersion;
    private static final Stack<State> states = new Stack<>();

    private States() {
    }

    public void push() {
        State currentState = new State(glVersion);
        currentState.push();
        states.add(currentState);
    }

    public void pop() {
        if (states.isEmpty()) {
            throw new IllegalStateException("No state to restore.");
        } else {
            State state = states.pop();
            state.pop();
        }
    }

    static {
        int[] major = new int[1];
        int[] minor = new int[1];
        GL30.glGetIntegerv(GL_MAJOR_VERSION, major);
        GL30.glGetIntegerv(GL_MINOR_VERSION, minor);
        glVersion = major[0] * 100 + minor[0] * 10;
    }
}
