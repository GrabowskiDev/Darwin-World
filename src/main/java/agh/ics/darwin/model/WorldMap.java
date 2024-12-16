package agh.ics.darwin.model;

import java.util.HashMap;
import java.util.Map;

public class WorldMap {
    private final int width;
    private final int height;
    private final Map<Vector2d, Animal> animals = new HashMap<>();
    private final Map<Vector2d, Plant> plants = new HashMap<>();
    //TODO: Jungle

    public WorldMap(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void place(WorldElement element) {
        Vector2d position = element.getPosition();
        if (element.getClass() == Animal.class) {
            animals.put(position, (Animal) element);
        } else if (element.getClass() == Plant.class) {
            plants.put(position, (Plant) element);
        }
    }

    public void remove(WorldElement element) {
        Vector2d position = element.getPosition();
        if (element instanceof Animal) {
            animals.remove(position, element);
        } else if (element instanceof Plant) {
            plants.remove(position, element);
        }
    }

    public Map<Vector2d, Animal> getAnimals() {
        return animals;
    }

    public Map<Vector2d, Plant> getPlants() {
        return plants;
    }
}
