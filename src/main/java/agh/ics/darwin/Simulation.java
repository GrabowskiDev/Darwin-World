package agh.ics.darwin;

import agh.ics.darwin.model.*;

import java.util.ArrayList;
import java.util.Map;

public class Simulation {
    private final Parameters parameters;
    private final WorldMap map;
    private final static int MAX_ITERATIONS = 1000; //TEMPORARY SOLUTION

    public Simulation(Parameters parameters) {
        this.parameters = parameters;
        this.map = new WorldMap(parameters.width(), parameters.height(), parameters.startPlants(), parameters.plantGrowth());
        for (int i = 0; i < parameters.startAnimals(); i++) {
            Vector2d animalPosition = new Vector2d((int) (Math.random() * parameters.width()), (int) (Math.random() * parameters.height()));
            int[] genesArray = new int[parameters.genomeLength()];
            for (int j=0; j<genesArray.length; j++) {
                genesArray[j] = new java.util.Random().nextInt(8);
            }
            Animal animal = new Animal(animalPosition, parameters.startEnergy(), new Genes(genesArray));
            map.place(animal);
        }
    }

    public void run() {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            System.out.println(i);
            removeDeadAnimals();
            moveAnimals();
            eatPlants();
            reproduceAnimals();
            growNewPlants();
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
                map.move(animal);
            }
        }
    }

    private void eatPlants() {
        ArrayList <Plant> plantsToRemove = new ArrayList<>();
        for (Map.Entry<Vector2d, Plant> entry : map.getPlants().entrySet()) {
            Vector2d plantPosition = entry.getKey();
            Plant plant = entry.getValue();
            if (map.isOccupiedByAnimal(plantPosition)) {
                ArrayList<Animal> animals = map.getAnimals().get(plantPosition);
                Animal animal = animals.getFirst();
                animal.gainEnergy(parameters.plantEnergy());
                plantsToRemove.add(plant);
            }
        }
        for (Plant plant : plantsToRemove) {
            map.remove(plant);
        }
    }

    private void reproduceAnimals() {
        for (Map.Entry<Vector2d, ArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            ArrayList<Animal> animals = entry.getValue();
            for (int i = 0; i < animals.size()-1; i+=2) {
                Animal parent1 = animals.get(i);
                Animal parent2 = animals.get(i+1);
                if (parent2.getEnergy() >= parameters.energyToBeFed()) {
                    //Genes
                    int totalEnergy = parent1.getEnergy() + parent2.getEnergy();
                    int parent1Len = (int) Math.ceil(((double) parent1.getEnergy()/totalEnergy ) * parameters.genomeLength());
                    int parent2Len = parameters.genomeLength() - parent1Len;
                    Genes childGenes = new Genes(parent1.getGenes().getGenes(), parent2.getGenes().getGenes(), parent1Len, parent2Len);

                    Animal child = new Animal(parent1.getPosition(), parameters.energyUsedToBreed() * 2, childGenes);
                    System.out.println("reproduced");
                    map.place(child);
                    parent1.loseEnergy(parameters.energyUsedToBreed());
                    parent2.loseEnergy(parameters.energyUsedToBreed());
                } else {
                    break;
                }
            }
        }
    }

    private void growNewPlants() {
        RandomUniquePositionGenerator randomUniquePositionGenerator = new RandomUniquePositionGenerator(parameters.width(), parameters.height());
        int i = 0;
        while (i < parameters.plantsPerDay()) {
            if (!randomUniquePositionGenerator.iterator().hasNext()) {
                break;
            }
            Vector2d plantPosition = randomUniquePositionGenerator.iterator().next();
            if (!map.isOccupiedByPlant(plantPosition)) {
                Plant plant = new Plant(plantPosition);
                map.place(plant);
                i++;
            }
        }
    }

    public WorldMap getMap() {
        return map;
    }
}
