package se.zodiakengine.richard.GameInnards;

public abstract class Scene {

    public Scene() {

    }

    public void init()
    {

    }

    /// Takes care of updating scenes
    ///
    /// @param deltaTime
    public abstract void update(float deltaTime);
}
