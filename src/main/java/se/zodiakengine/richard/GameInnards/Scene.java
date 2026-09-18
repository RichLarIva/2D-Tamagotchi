package se.zodiakengine.richard.GameInnards;

public abstract class Scene {

    public Scene() {

    }

    /// Takes care of updating scenes
    ///
    /// @param deltaTime
    public abstract void update(float deltaTime);
}
