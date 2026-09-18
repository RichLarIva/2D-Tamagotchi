package se.zodiakengine.richard.GameInnards;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import se.zodiakengine.richard.Utils.Time;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static Window window = null;
    private static Scene currentScene;
    private final float a;
    private float r;
    private float g;
    private float b;
    private int width, height;
    private String title;
    private long glfwWindow;
    private long vg;
    private double lastTime = System.currentTimeMillis();
    private int fps = 0;
    private int frames = 0;

    private Window() {
        this.width = 2560;
        this.height = 1440;
        this.title = "Best Tamogotchi 2D";
        r = 0.1f;
        g = 0.5f;
        b = 0.95f;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                break;
            case 1:
                currentScene = new LevelScene();
                break;
            default:
                assert false : "Unknown scene \"" + newScene + "\"";
        }
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

        init();
        loop();

        // Free the memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        // Force X11 backend to avoid Wayland/libdecor crashes
        glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW!");


        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        glfwWindowHint(GLFW_STENCIL_BITS, 8);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL)
            throw new IllegalStateException("Failed to create the GLFW window.");

        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(0);
        glfwShowWindow(glfwWindow);

        GL.createCapabilities();

        vg = NanoVGGL3.nvgCreate(
                NanoVGGL3.NVG_ANTIALIAS |
                        NanoVGGL3.NVG_STENCIL_STROKES
        );

        if (vg == NULL)
            throw new RuntimeException("Failed to create NanoVG context");

        // Load font
        int font = NanoVG.nvgCreateFont(vg, "mono", "/mnt/DATA/2D Tamagotchi/src/main/resources/fonts/Comic Sans MS.ttf");
        if (font == -1)
            throw new RuntimeException("Failed to load font");


        nvgEndFrame(vg);

        glfwSwapBuffers(glfwWindow);

    }


    public void loop() {
        float beginTime = Time.getTime();
        float endTime = Time.getTime();

        while (!glfwWindowShouldClose(glfwWindow)) {
            // poll events
            glfwPollEvents();

            //rgbScreen();

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT);

            double currentTime = System.currentTimeMillis();
            frames++;

            if (currentTime - lastTime >= 1000) {
                fps = frames;
                frames = 0;
                lastTime = currentTime;

            }

            nvgBeginFrame(vg, width, height, 1);

            nvgFontSize(vg, 48f);
            nvgFontFace(vg, "mono");

            NVGColor color = NVGColor.create();
            color.r(1f).g(1f).b(1f).a(1f);

            nvgFillColor(vg, color);

            nvgText(vg, 0, 100, "FPS: " + fps);

            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE))
                nvgText(vg, 100, 100, "SPACE KEY PRESSED");

            nvgEndFrame(vg);


            glfwSwapBuffers(glfwWindow);
            endTime = Time.getTime();

            float deltaTime = endTime - beginTime;
            beginTime = endTime;
        }
    }

    private void rgbScreen() {
        r = MouseListener.getX() / width;
        g = MouseListener.getY() / height;
        b = 1.0f - r;
    }
}
