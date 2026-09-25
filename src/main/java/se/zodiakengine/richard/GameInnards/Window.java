package se.zodiakengine.richard.GameInnards;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;
import se.zodiakengine.richard.Tamagotchi.Tamagotchi;
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
    private Tamagotchi playerTamagotchi;
    private int dvdCornerHits = 0;
    private boolean wasInCorner = false;

    private Window() {
        this.width = 1920;
        this.height = 1200;
        this.title = "Best Tamogotchi 2D";
        r = 0.0f;
        g = 0.0f;
        b = 0.0f;
        a = 1;
    }

    public static void changeScene(int newScene) {
        switch (newScene)
        {
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
        {
            Window.window = new Window();
        }

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

        // Let GLFW automatically select the correct platform
        // (Windows on Windows, X11/Wayland on Linux).
        if (!glfwInit())
        {
            throw new IllegalStateException("Unable to initialize GLFW!");
        }

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
        {
            throw new IllegalStateException("Failed to create the GLFW window.");
        }


        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        glfwMakeContextCurrent(glfwWindow);
        // vsync
        glfwSwapInterval(0);
        glfwShowWindow(glfwWindow);

        GL.createCapabilities();

        vg = NanoVGGL3.nvgCreate(
                NanoVGGL3.NVG_ANTIALIAS |
                        NanoVGGL3.NVG_STENCIL_STROKES
        );

        if (vg == NULL)
        {
            throw new RuntimeException("Failed to create NanoVG context");
        }

        // Load font
        try (var inputStream = getClass().getResourceAsStream("/fonts/Comic Sans MS.ttf"))
        {

            if (inputStream == null)
            {
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

            if (font == -1)
            {
                throw new RuntimeException("Failed to load font");
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        nvgEndFrame(vg);
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
        playerTamagotchi = new Tamagotchi("Felix");

    }

    private void checkCornerHit(float x, float y) {
        boolean inCorner =
                (x <= 0 && y <= 0) || // top-left
                        (x + 100 >= width && y <= 0) || // top-right
                        (x <= 0 && y + 100 >= height) || // bottom-left
                        (x + 100 >= width && y + 100 >= height); // bottom-right

        if (inCorner && !wasInCorner)
        {
            dvdCornerHits++;
            System.out.println("DVD corner hits: " + dvdCornerHits);
        }

        wasInCorner = inCorner;
    }

    public void loop() {
        float beginTime = Time.getTime();
        float endTime = Time.getTime();
        String test = "";
        int previousKey = -1;
        int switchVsync = 0;
        NVGColor color = NVGColor.create();
        color.r(0f).g(1f).b(0f).a(1f);
        float tamaX = width / 2;
        float tamaY = height / 2;
        int bc = 5;
        float x2 = width / 2;
        float y2 = height / 2;
        float dx = 260, dy = 260;

        while (!glfwWindowShouldClose(glfwWindow))
        {
            // poll events
            glfwPollEvents();


            //rgbScreen();

            glClearColor(r, g, b, a);
            glClear(GL_COLOR_BUFFER_BIT);

            double currentTime = System.currentTimeMillis();
            frames++;

            if (currentTime - lastTime >= 1000)
            {
                fps = frames;
                frames = 0;
                lastTime = currentTime;

            }


            nvgBeginFrame(vg, width, height, 1);

            nvgFontSize(vg, 48f);
            nvgFontFace(vg, "mono");


            nvgFillColor(vg, color);

            nvgText(vg, 0, 100, "FPS: " + fps);
            nvgText(vg, 0, 1000, "WIDTH: " + width + "Height:" + height);
            nvgText(vg, width / 2, 200, "Corner Hits: " + dvdCornerHits);

            if (KeyListener.isKeyPressed(GLFW_KEY_SPACE))
            {
                nvgText(vg, 100, 100, "SPACE KEY PRESSED");
            }

            if (KeyListener.isKeyPressed(GLFW_KEY_F5))
            {
                if (switchVsync == 0)
                {
                    glfwSwapInterval(1);
                    switchVsync = 1;
                    nvgText(vg, 800, 100, "VSYNC ON");
                }

            }
            else if (KeyListener.isKeyPressed(GLFW_KEY_F6))
            {

                if (switchVsync == 1)
                {
                    glfwSwapInterval(0);
                    switchVsync = 0;
                    nvgText(vg, 800, 100, "VSYNC OFF");
                }
            }


            drawDVDLOGO(x2, y2);


            bc += bc << 5;
            nvgText(vg, 600, 900, "" + bc);


//            int key = KeyListener.getKeyPressed();
//
//            if(key != -1 && key != previousKey)
//            {
//                test += (char) key;
//            }
//
//            previousKey = key;
            if (playerTamagotchi.getFullLevel() > 0 && playerTamagotchi.getFunLevel() > -5)
            {

                nvgTextBox(vg, 250, 600, width - 2 * 20, playerTamagotchi.toString() + "\nHäst");
                switch (KeyListener.getKeyPressed())
                {
                    case GLFW_KEY_1:
                        playerTamagotchi.increaseFun();
                        break;
                    case GLFW_KEY_2:
                        playerTamagotchi.increaseFullness();
                        break;
                }
            }
            else
            {
                color.r(1f).g(0f).b(0f).a(1f);
                if (playerTamagotchi.getFunLevel() <= -5)
                {
                    nvgText(vg, 600, 600, playerTamagotchi.getName() + " SUICIDED");
                }
                else
                {
                    nvgText(vg, 600, 600, playerTamagotchi.getName() + " DIED");
                }
            }

            if (KeyListener.isKeyPressed(GLFW_KEY_UP))
            {
                tamaY -= 0.5f;
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_DOWN))
            {
                tamaY += 0.5f;
            }

            if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT))
            {
                tamaX += 0.5f;
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_LEFT))
            {
                tamaX -= 0.5f;
            }
            tamaX = Math.clamp(tamaX, 0, width - 200f);
            tamaY = Math.clamp(tamaY, 0, height - 200f);

            x2 = Math.clamp(x2, 0, width - 100f);
            y2 = Math.clamp(y2, 0, height - 100f);

            drawTamagotchi(tamaX, tamaY);

            nvgText(vg, 0, 900, test);
            if (KeyListener.isKeyPressed(GLFW_KEY_F2))
            {
                test = "";
            }

            nvgText(vg, 900, 50, "X:" + tamaX + " Y:" + tamaY);

            nvgEndFrame(vg);


            glfwSwapBuffers(glfwWindow);
            endTime = Time.getTime();

            float deltaTime = endTime - beginTime;


            x2 += dx * deltaTime;
            y2 += dy * deltaTime;

            if (x2 <= 0)
            {
                dx *= -1;
                x2 = 0;
            }
            else if (x2 + 100f >= width)
            {
                dx *= -1;
                x2 = width - 100f;
            }
            if (y2 <= 0)
            {
                dy *= -1;
                y2 = 0;
            }
            else if (y2 + 100f >= height)
            {
                y2 = height - 100f;
                dy *= -1f;
            }

            checkCornerHit(x2, y2);


            boolean isColliding = (x2 < tamaX + 200 && x2 + 100 > tamaX) &&
                    (y2 < tamaY + 200 && y2 + 100 > tamaY);

            if (isColliding)
            {
                float overlapLeft = (x2 + 100) - tamaX;
                float overlapRight = (tamaX + 200) - x2;
                float overlapTop = (y2 + 100) - tamaY;
                float overlapBottom = (tamaY + 200) - y2;

                float minOverlap = Math.min(Math.min(overlapLeft, overlapRight), Math.min(overlapTop, overlapBottom));

                if (minOverlap == overlapLeft)
                {
                    x2 = tamaX - 100;
                    dx = -Math.abs(dx);
                }
                else if (minOverlap == overlapRight)
                {
                    x2 = tamaX + 200; // Push out of collision
                    dx = Math.abs(dx);      // Bounce right
                }
                else if (minOverlap == overlapTop)
                {
                    y2 = tamaY - 100;
                    dy = -Math.abs(dy);
                }
                else if (minOverlap == overlapBottom)
                {
                    y2 = tamaY + 200;
                    dy = Math.abs(dy);
                }
            }

            if (tamaY != 929)
            {
                tamaY -= -9.81f * deltaTime * 20;
            }
            beginTime = endTime;
            KeyListener.endFrame();
            if (KeyListener.isKeyPressed(GLFW_KEY_5))
            {
                glfwWindowShouldClose(glfwWindow);
                glfwDestroyWindow(glfwWindow);
            }
        }
    }

    private void rgbScreen() {
        r = MouseListener.getX() / width;
        g = MouseListener.getY() / height;
        b = 1.0f - r;
    }

    private void drawTamagotchi(float x, float y) {
        nvgBeginPath(vg);

        nvgRect(vg, x, y, 200, 200);

        NVGColor color = NVGColor.create()
                .r(1.0f)
                .g(0.5f)
                .b(0.2f)
                .a(1.0f);

        nvgFillColor(vg, color);
        nvgFill(vg);
    }

    private void drawDVDLOGO(float x, float y) {
        nvgBeginPath(vg);
        nvgRect(vg, x, y, 100, 100);

        NVGColor color = NVGColor.create().r(0.0f).g(1).b(0).a(0.95f);

        nvgFillColor(vg, color);
        nvgFill(vg);
    }
}
