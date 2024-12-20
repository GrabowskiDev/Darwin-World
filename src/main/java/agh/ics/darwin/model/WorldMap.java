package agh.ics.darwin.model;

import agh.ics.darwin.model.variants.PlantGrowthVariant;

import java.util.HashMap;
import java.util.Map;

public class WorldMap {
    private final int width;
    private final int height;
    private final int startPlants;
    private final PlantGrowthVariant plantGrowthVariant;

    private final Map<Vector2d, Animal> animals = new HashMap<>();
    private final Map<Vector2d, Plant> plants = new HashMap<>();
    //TODO: Jungle

    public WorldMap(int width, int height, int startPlants, PlantGrowthVariant plantGrowthVariant) {
        this.width = width;
        this.height = height;
        this.startPlants = startPlants;
        this.plantGrowthVariant = plantGrowthVariant;

        //Placing plants
        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(width, height, startPlants);
        for (Vector2d plantPosition : randomPositionGenerator) {
            Plant plant = new Plant(plantPosition);
            plants.put(plantPosition, plant);
        }
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
            if (isOccupiedByPlant(position)) {
                throw new IllegalArgumentException("Cannot place plant on another plant");
            } else {
                plants.put(position, (Plant) element);
            }
        }
    }

    public void remove(WorldElement element) {
        Vector2d position = element.getPosition();
        if (element instanceof Animal && animals.get(position) == element) {
            animals.remove(position, element);
        } else if (element instanceof Plant && plants.get(position) == element) {
            plants.remove(position, element);
        }
    }

    public boolean isOccupiedByPlant(Vector2d position) {
        return plants.containsKey(position);
    }

    public Map<Vector2d, Animal> getAnimals() {
        return animals;
    }

    public Map<Vector2d, Plant> getPlants() {
        return plants;
    }
}
