package agh.ics.darwin.model;

public class SuperPlant implements WorldElement{
    private final Vector2d position;

    public SuperPlant(Vector2d position) {
        this.position = position;
    }

    public Vector2d getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "S";
    }
}
