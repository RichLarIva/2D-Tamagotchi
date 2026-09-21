package se.zodiakengine.richard.GameInnards;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {

    private static KeyListener instance;

    private final boolean[] keyPressed = new boolean[350];
    private final boolean[] keyJustPressed = new boolean[350];

    private KeyListener() {
    }

    public static KeyListener get() {
        if (KeyListener.instance == null)
        {
            KeyListener.instance = new KeyListener();
        }

        return KeyListener.instance;
    }

    public static void keyCallback(long window, int key, int scanCode, int action, int mods) {
        if (key < 0 || key >= get().keyPressed.length)
        {
            return;
        }

        if (action == GLFW_PRESS)
        {

            // Only true on the initial press
            get().keyPressed[key] = true;
            get().keyJustPressed[key] = true;

        }
        else if (action == GLFW_RELEASE)
        {

            get().keyPressed[key] = false;
        }
    }

    /**
     * True while the key is being held.
     */
    public static boolean isKeyPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= get().keyPressed.length)
        {
            return false;
        }

        return get().keyPressed[keyCode];
    }

    /**
     * True only once when the key is initially pressed.
     */
    public static boolean isKeyJustPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= get().keyJustPressed.length)
        {
            return false;
        }

        return get().keyJustPressed[keyCode];
    }

    /**
     * Call this at the end of every game frame.
     */
    public static void endFrame() {
        for (int i = 0; i < get().keyJustPressed.length; i++)
        {
            get().keyJustPressed[i] = false;
        }
    }

    /**
     * Returns the first key that was just pressed this frame.
     * Returns -1 if no key was pressed.
     */
    public static int getKeyPressed() {
        for (int key = 0; key < get().keyJustPressed.length; key++)
        {
            if (get().keyJustPressed[key])
            {
                return key;
            }
        }

        return -1;
    }
}
