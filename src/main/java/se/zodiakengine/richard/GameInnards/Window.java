package se.zodiakengine.richard.GameInnards;

import org.lwjgl.PointerBuffer;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import se.zodiakengine.richard.GameInnards.Scenes.LevelEditorScene;
import se.zodiakengine.richard.GameInnards.Scenes.LevelScene;
import se.zodiakengine.richard.GameInnards.Scenes.Scene;
import se.zodiakengine.richard.GameInnards.Scenes.TamagotchiScene;
import se.zodiakengine.richard.Tamagotchi.Tamagotchi;
import se.zodiakengine.richard.Utils.Time;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static final int SCENES_MAX_INTEGER = 3;
    private static final int DEFAULT_MONITOR = 0;
    private static Window window = null;
    private static Scene currentScene;
    private final float a;
    private final double lastTime = System.currentTimeMillis();
    private final int fps = 0;
    private final int frames = 0;
    private final int dvdCornerHits = 0;
    private final boolean wasInCorner = false;
    public float r;
    public float g;
    public float b;
    private int width, height;
    private String title;
    private long glfwWindow;
    private long vg;
    private Tamagotchi playerTamagotchi;

    private Window() {
        this.title = "Best Tamogotchi 2D";
        r = 0.0f;
        g = 0.0f;
        b = 0.0f;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch (newScene) {
            case 0:
                currentScene = new LevelEditorScene();
                currentScene.init();
                break;
            case 1:
                currentScene = new LevelScene();
                currentScene.init();
                break;
            case 2:
                currentScene = new TamagotchiScene();
                currentScene.init();
                break;
            default:
                assert false : "Unknown scene \"" + newScene + "\"";
        }
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }

        return Window.window;
    }

    private GLFWVidMode getSelectedMonitorMode() {
        PointerBuffer monitors = glfwGetMonitors();

        if (monitors == null || monitors.remaining() == 0) {
            throw new IllegalStateException("No monitors detected.");
        }

        if (DEFAULT_MONITOR < 0 || DEFAULT_MONITOR >= monitors.remaining()) {
            throw new IllegalStateException(
                    "Invalid monitor index: " + DEFAULT_MONITOR +
                            ". Available monitors: " + monitors.remaining()
            );
        }

        long monitor = monitors.get(DEFAULT_MONITOR);

        String name = glfwGetMonitorName(monitor);
        GLFWVidMode mode = glfwGetVideoMode(monitor);

        IO.println(
                "Starting on monitor " + DEFAULT_MONITOR +
                        ": " + name +
                        " (" + mode.width() + "x" + mode.height() + ")"
        );

        return mode;
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

        // Let GLFW automatically select the correct platform
        // (Windows on Windows, X11/Wayland on Linux).

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
        }
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW!");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        glfwWindowHint(GLFW_STENCIL_BITS, 8);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);


        GLFWVidMode videoMode = getSelectedMonitorMode();

        width = videoMode.width();
        height = videoMode.height();

        PointerBuffer monitors = glfwGetMonitors();

        if (monitors == null || monitors.remaining() == 0) {
            throw new IllegalStateException("No monitors detected");
        }

        long monitor = monitors.get(DEFAULT_MONITOR);

        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);

        if (glfwWindow == NULL) {
            throw new IllegalStateException("Failed to create the GLFW window.");
        }

        int[] monitorX = new int[1];
        int[] monitorY = new int[1];

        glfwGetMonitorPos(monitor, monitorX, monitorY);

        int windowX = monitorX[0] + (videoMode.width() - width) / 2;
        int windowY = monitorY[0] + (videoMode.height() - height) / 2;

        glfwSetWindowPos(glfwWindow, windowX, windowY);


        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        glfwMakeContextCurrent(glfwWindow);
        // vsync
        glfwSwapInterval(0);
        glfwShowWindow(glfwWindow);
        glfwMaximizeWindow(glfwWindow);

        GL.createCapabilities();

        vg = NanoVGGL3.nvgCreate(
                NanoVGGL3.NVG_ANTIALIAS |
                        NanoVGGL3.NVG_STENCIL_STROKES
        );

        if (vg == NULL) {
            throw new RuntimeException("Failed to create NanoVG context");
        }

        // Load font
        try (var inputStream = getClass().getResourceAsStream("/fonts/Comic Sans MS.ttf")) {

            if (inputStream == null) {
                throw new RuntimeException("Font not found: /fonts/Comic Sans MS.ttf");
            }

            var tempFont = java.nio.file.Files.createTempFile("font-", ".ttf");

            java.nio.file.Files.copy(
                    inputStream,
                    tempFont,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            int font = NanoVG.nvgCreateFont(
                    vg,
                    "mono",
                    tempFont.toAbsolutePath().toString()
            );

            if (font == -1) {
                throw new RuntimeException("Failed to load font");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        int[] windowWidth = new int[1];
        int[] windowHeight = new int[1];

        glfwGetFramebufferSize(glfwWindow, windowWidth, windowHeight);

        width = windowWidth[0];
        height = windowHeight[0];

        glfwSetFramebufferSizeCallback(glfwWindow, (window, newWidth, newHeight) -> {
            width = newWidth;
            height = newHeight;
        });

        glfwSwapBuffers(glfwWindow);
        Window.changeScene(0);
    }


    public void loop() {
        float beginTime = Time.getTime();
        float endTime;
        float deltaTime = -1.0f;
        int sceneInt = 0;


        while (!glfwWindowShouldClose(glfwWindow)) {

            glfwPollEvents();

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (deltaTime >= 0) {
                currentScene.update(deltaTime);
            }

            glfwSwapBuffers(glfwWindow);

            endTime = Time.getTime();
            deltaTime = endTime - beginTime;
            beginTime = endTime;
            if (KeyListener.isKeyJustPressed(GLFW_KEY_6)) {
                sceneInt++;
                if (sceneInt >= SCENES_MAX_INTEGER)
                    sceneInt = 0;
                Window.changeScene(sceneInt);
            }
            KeyListener.endFrame();
            if (KeyListener.isKeyPressed(GLFW_KEY_5)) {
                glfwSetWindowShouldClose(glfwWindow, true);
            }

        }
    }

    private void rgbScreen() {
        r = MouseListener.getX() / width;
        g = MouseListener.getY() / height;
        b = 1.0f - r;
    }

    public long getVG() {
        return vg;
    }
}
