package se.zodiakengine.richard.Tamagotchi;

public class Tamagotchi {
    private final String name;
    private int fullLevel;
    private int funLevel;

    public Tamagotchi(String name) {
        this.name = name;
        this.fullLevel = 10;
        this.funLevel = 10;
    }

    public int getFullLevel() {
        return fullLevel;
    }

    public void setFullLevel(int fullLevel) {
        this.fullLevel = fullLevel;
    }

    public int getFunLevel() {
        return funLevel;
    }

    public void setFunLevel(int funLevel) {
        this.funLevel = funLevel;
    }

    public void increaseFun() {
        this.funLevel++;
        this.fullLevel--;
    }

    public void increaseFullness() {
        this.fullLevel++;
        this.funLevel--;
    }


    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name + " FULLNESS LEVEL: " + this.fullLevel + " FUN LEVEL: " + this.funLevel;
    }
}
