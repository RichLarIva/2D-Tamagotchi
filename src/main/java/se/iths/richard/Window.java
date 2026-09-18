package se.iths.richard;

import org.lwjgl.Version;

public class Window {
    private static Window window = null;
    private int width, height;
    private String title;

    private Window() {
        this.width = 1920;
        this.height = 1080;
        this.title = "Best Tamogotchi 2D";
    }

    public static Window get() {
        if (Window.window == null)
            Window.window = new Window();

        return Window.window;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void run() {
        IO.println("Hello LWJGL" + Version.getVersion() + "!");
    }
}
