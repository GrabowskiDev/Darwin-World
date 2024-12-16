package agh.ics.darwin.model;

public class Plant implements WorldElement {
    private final Vector2d position;
    private static final int energy = 10; //TODO: getting from params

    public Plant(Vector2d position) {
        this.position = position;
    }

    public Vector2d getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "*";
    }
}
