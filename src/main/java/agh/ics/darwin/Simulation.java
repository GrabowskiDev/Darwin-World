package agh.ics.darwin;

import agh.ics.darwin.model.*;

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
        for (Map.Entry<Vector2d, Animal> animal : map.getAnimals().entrySet()) {
            Vector2d position = animal.getKey();
            Animal currentAnimal = animal.getValue();
            if (currentAnimal.getEnergy() <= 0) {
                map.getAnimals().remove(position, currentAnimal);
            }
        }
    }

    private void moveAnimals() {}

    private void eatPlants() {}

    private void reproduceAnimals() {}

    private void growNewPlants() {}
}
