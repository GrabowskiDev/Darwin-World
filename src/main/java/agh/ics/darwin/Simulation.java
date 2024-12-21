package agh.ics.darwin;

import agh.ics.darwin.model.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

public class Simulation {
    private final Parameters parameters;
    private final WorldMap map;

    public Simulation(Parameters parameters) {
        this.parameters = parameters;
        this.map = new WorldMap(parameters.width(), parameters.height(), parameters.startPlants(), parameters.plantGrowth());

        RandomPositionGenerator randomPositionGenerator = new RandomPositionGenerator(parameters.width(), parameters.height(), parameters.startAnimals());
        for (Vector2d animalPosition : randomPositionGenerator) {
            Animal animal = new Animal(animalPosition, parameters.startEnergy());
            map.place(animal);
        }
    }

    private void removeDeadAnimals() {
        for (Map.Entry<Vector2d, ArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            ArrayList<Animal> animals = entry.getValue();
            for (Animal animal : animals) {
                if (animal.getEnergy() <= 0) {
                    map.remove(animal);
                }
            }
        }
    }

    private void moveAnimals() {
        for (Map.Entry<Vector2d, ArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            ArrayList<Animal> animals = entry.getValue();
            for (Animal animal : animals) {
                animal.move();
            }
        }
    }

    private void eatPlants() {
    }

    private void reproduceAnimals() {}

    private void growNewPlants() {}
}
