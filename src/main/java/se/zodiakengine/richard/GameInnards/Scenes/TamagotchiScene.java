package se.zodiakengine.richard.GameInnards.Scenes;

import org.lwjgl.nanovg.NVGColor;
import se.zodiakengine.richard.GameInnards.KeyListener;
import se.zodiakengine.richard.GameInnards.Window;
import se.zodiakengine.richard.Tamagotchi.JobStuff.Job;
import se.zodiakengine.richard.Tamagotchi.Tamagotchi;

import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;

public class TamagotchiScene extends Scene {

    private double lastTime = System.nanoTime();
    private float dx = 260.0f;
    private float dy = 260.0f;
    private long vg;
    private Tamagotchi playerTamagotchi;
    private float tamaX;
    private float tamaY;
    private float dvdX;
    private float dvdY;
    private int dvdCornerHits;
    private boolean wasInCorner;
    private int fps;
    private int frames;
    private NVGColor color;

    @Override
    public void init() {
        vg = Window.get().getVG();

        playerTamagotchi = new Tamagotchi("Felix");

        tamaX = Window.get().getWidth() / 2.0f;
        tamaY = Window.get().getHeight() / 2.0f;

        dvdX = Window.get().getWidth() / 2.0f;
        dvdY = Window.get().getHeight() / 2.0f;

        color = NVGColor.create()
                .r(0.0f)
                .g(1.0f)
                .b(0.0f)
                .a(1.0f);
    }

    @Override
    public void update(float deltaTime) {
        updateFPS();
        updateTamagotchi(deltaTime);
        updateDVD(deltaTime);
        render();
    }

    private void updateFPS() {
        long currentTime = System.nanoTime();
        frames++;

        if (currentTime - lastTime >= 1_000_000_000L) {
            fps = frames;
            frames = 0;
            lastTime = currentTime;
        }
    }

    private void updateTamagotchi(float deltaTime) {
        handleInput(deltaTime);

        if (playerTamagotchi.getFullLevel() > 0 && playerTamagotchi.getFunLevel() > -5) {
            switch (KeyListener.getKeyPressed()) {
                case GLFW_KEY_1:
                    playerTamagotchi.increaseFun();
                    break;
                case GLFW_KEY_2:
                    playerTamagotchi.increaseFullness();
                    break;
            }
        }
    }

    private void checkCornerHit() {

        int width = Window.get().getWidth();
        int height = Window.get().getHeight();

        boolean inCorner =
                (dvdX <= 0 && dvdY <= 0) ||
                        (dvdX + 100 >= width && dvdY <= 0) ||
                        (dvdX <= 0 && dvdY + 100 >= height) ||
                        (dvdX + 100 >= width && dvdY + 100 >= height);

        if (inCorner && !wasInCorner) {
            dvdCornerHits++;

            IO.println("DVD corner hits: " + dvdCornerHits);
        }

        wasInCorner = inCorner;
    }

    private void updateDVD(float deltaTime) {

        dvdX += dx * deltaTime;
        dvdY += dy * deltaTime;

        int width = Window.get().getWidth();
        int height = Window.get().getHeight();

        if (dvdX <= 0) {
            dvdX = 0;
            dx *= -1;
        } else if (dvdX + 100 >= width) {
            dvdX = width - 100;
            dx *= -1;
        }

        if (dvdY <= 0) {
            dvdY = 0;
            dy *= -1;
        } else if (dvdY + 100 >= height) {
            dvdY = height - 100;
            dy *= -1;
        }

        checkCornerHit();
    }

    private Job randomJob() {
        int pick = new Random().nextInt(Job.values().length);
        return Job.values()[pick];
    }

    private void handleInput(float deltaTime) {

        if (KeyListener.isKeyPressed(GLFW_KEY_UP)) {
            tamaY -= 200.0f * deltaTime;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_DOWN)) {
            tamaY += 200.0f * deltaTime;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT)) {
            tamaX -= 200.0f * deltaTime;
        }

        if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT)) {
            tamaX += 200.0f * deltaTime;
        }

        tamaX = Math.clamp(
                tamaX,
                0,
                Window.get().getWidth() - 200.0f
        );

        tamaY = Math.clamp(
                tamaY,
                0,
                Window.get().getHeight() - 200.0f
        );
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

    private void render() {

        int width = Window.get().getWidth();
        int height = Window.get().getHeight();

        nvgBeginFrame(vg, width, height, 1);

        nvgFontSize(vg, 48.0f);
        nvgFontFace(vg, "mono");

        nvgFillColor(vg, color);

        nvgText(vg, 0, 100, "FPS: " + fps);

        nvgText(
                vg,
                0,
                1000,
                "WIDTH: " + width + " Height: " + height
        );

        nvgText(
                vg,
                width / 2,
                200,
                "Corner Hits: " + dvdCornerHits
        );

        drawTamagotchi(tamaX, tamaY);
        drawDVDLOGO(dvdX, dvdY);

        nvgEndFrame(vg);
    }
}