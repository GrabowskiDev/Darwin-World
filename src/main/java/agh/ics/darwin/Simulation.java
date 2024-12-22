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
        for (Map.Entry<Vector2d, Plant> entry : map.getPlants().entrySet()) {
            Vector2d plantPosition = entry.getKey();
            Plant plant = entry.getValue();
            if (map.isOccupiedByAnimal(plantPosition)) {
                ArrayList<Animal> animals = map.getAnimals().get(plantPosition);
                Animal animal = animals.getFirst();
                animal.gainEnergy(parameters.plantEnergy());
                map.remove(plant);
            }
        }
    }

    private void reproduceAnimals() {
        for (Map.Entry<Vector2d, ArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            ArrayList<Animal> animals = entry.getValue();
            for (int i = 0; i < animals.size()-1; i+=2) {
                Animal parent1 = animals.get(i);
                Animal parent2 = animals.get(i+1);
                if (parent2.getEnergy() >= parameters.energyToBeFed()) {
                    Vector2d childPosition = parent1.getPosition();

                    //Genes
                    int totalEnergy = parent1.getEnergy() + parent2.getEnergy();
                    int parent1Len = (int) Math.ceil((double) (parent1.getEnergy()/totalEnergy) * parameters.genomeLength());
                    int parent2Len = parameters.genomeLength() - parent1Len;
                    Genes childGenes = new Genes(parent1.getGenes().getGenes(), parent2.getGenes().getGenes(), parent1Len, parent2Len);

                    Animal child = new Animal(childPosition, parameters.energyUsedToBreed() * 2, childGenes);
                    map.place(child);
                    parent1.loseEnergy(parameters.energyUsedToBreed());
                    parent2.loseEnergy(parameters.energyUsedToBreed());
                } else {
                    break;
                }
            }
        }
    }

    private void growNewPlants() {}
}
