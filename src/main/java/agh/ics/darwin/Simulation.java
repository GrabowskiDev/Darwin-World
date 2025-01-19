package agh.ics.darwin;

import agh.ics.darwin.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Simulation implements Runnable {
    private final Parameters parameters;
    private final WorldMap map;
    private final static int MAX_ITERATIONS = 50; //TEMPORARY SOLUTION
    private int sleepDuration = 700;
    private final List<DailyStatistics> dailyStatistics = new ArrayList<>();
    private int day = 0;
    private boolean isPaused = false;
    private boolean isStarted = false;
    private volatile boolean isFinished = false;


    public void addDailyStatistics(DailyStatistics stats) {
        dailyStatistics.add(stats);
    }

    public List<DailyStatistics> getDailyStatistics() {
        return dailyStatistics;
    }

    public void setSleepDuration(int sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public Simulation(Parameters parameters) {
        this.parameters = parameters;
        this.map = new WorldMap(parameters.width(), parameters.height(), parameters.startPlants(), parameters.plantGrowth(), parameters.animalBehaviour());
        for (int i = 0; i < parameters.startAnimals(); i++) {
            Vector2d animalPosition = new Vector2d((int) (Math.random() * parameters.width()), (int) (Math.random() * parameters.height()));
            int[] genesArray = new int[parameters.genomeLength()];
            for (int j=0; j<genesArray.length; j++) {
                genesArray[j] = new java.util.Random().nextInt(8);
            }

            Animal animal = new Animal(animalPosition, getRandomMapDirection(), parameters.startEnergy(), new Genes(genesArray));
            map.place(animal);
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public synchronized void pause() {
        isPaused = true;
    }

    public synchronized void resume() {
        isPaused = false;
        notify();
    }

    public synchronized void stop() {
        isFinished = true;
    }

    public void run() {
        isStarted = true;
        while (true) {
            if (isFinished) {
                break;
            }
            synchronized (this) {
                while (isPaused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            removeDeadAnimals();
            moveAnimals();
            eatPlants();
            reproduceAnimals();
            growNewPlants();
            day++;
            map.notifyObservers();
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void removeDeadAnimals() {
        ArrayList <Animal> animalsToRemove = new ArrayList<>();
        for (Map.Entry<Vector2d, CopyOnWriteArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            CopyOnWriteArrayList<Animal> animals = entry.getValue();
            for (Animal animal : animals) {
                if (animal.getEnergy() <= 0) {
                    animalsToRemove.add(animal);
                }
            }
        }
        for (Animal animal : animalsToRemove) {
            animal.setDayOfDeath(day);
            map.remove(animal);
        }
    }

    private void moveAnimals() {
        ArrayList <Animal> animalsToMove = new ArrayList<>();
        for (Map.Entry<Vector2d, CopyOnWriteArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            CopyOnWriteArrayList<Animal> animals = entry.getValue();
            for (Animal animal : animals) {
                animalsToMove.add(animal);
            }
        }
        for (Animal animal : animalsToMove) {
            map.move(animal);
            animal.loseEnergy(1);
            animal.incrementAge();
        }
    }

    private synchronized void eatPlants() {
        ArrayList<Plant> plantsToRemove = new ArrayList<>();
        for (Map.Entry<Vector2d, Plant> entry : map.getPlants().entrySet()) {
            Vector2d plantPosition = entry.getKey();
            Plant plant = entry.getValue();
            if (map.isOccupiedByAnimal(plantPosition)) {
                CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition);
                if (animals != null && !animals.isEmpty()) {
                    Animal animal = animals.get(0); // Assuming the list is sorted by strength
                    animal.gainEnergy(parameters.plantEnergy());
                    animal.addPlantsEaten();
                    plantsToRemove.add(plant);
                }
            }
        }
        for (Plant plant : plantsToRemove) {
            map.remove(plant);
        }
    }

    private void reproduceAnimals() {
        ArrayList <Animal> animalsToPlace = new ArrayList<>();
        for (Map.Entry<Vector2d, CopyOnWriteArrayList<Animal>> entry : map.getAnimals().entrySet()) {
            CopyOnWriteArrayList<Animal> animals = entry.getValue();
            for (int i = 0; i < animals.size()-1; i+=2) {
                Animal parent1 = animals.get(i);
                Animal parent2 = animals.get(i+1);
                if (parent2.getEnergy() >= parameters.energyToBeFed()) {
                    //Genes
                    int totalEnergy = parent1.getEnergy() + parent2.getEnergy();
                    int parent1Len = (int) Math.ceil(((double) parent1.getEnergy()/totalEnergy ) * parameters.genomeLength());
                    int parent2Len = parameters.genomeLength() - parent1Len;
                    Genes childGenes = new Genes(parent1.getGenes().getGenes(), parent2.getGenes().getGenes(), parent1Len, parent2Len, parameters.minMutations(), parameters.maxMutations());

                    Animal child = new Animal(parent1.getPosition(), getRandomMapDirection(), parameters.energyUsedToBreed() * 2, childGenes);
                    animalsToPlace.add(child);
                    parent1.loseEnergy(parameters.energyUsedToBreed());
                    parent2.loseEnergy(parameters.energyUsedToBreed());
                    parent1.addChildren();
                    parent2.addChildren();
                } else {
                    break;
                }
            }
        }

        for (Animal animal : animalsToPlace) {
            map.place(animal);
        }
    }

    private void growNewPlants() {
        int jungleBottom = map.getJungleBottom();
        int jungleHeight = map.getJungleTop() - map.getJungleBottom() + 1;

        RandomUniquePositionGenerator junglePositionGenerator = new RandomUniquePositionGenerator(parameters.width(), jungleHeight);
        RandomUniquePositionGenerator outsideJunglePositionGenerator = new RandomUniquePositionGenerator(parameters.width(), parameters.height() - jungleHeight);
        Random random = new Random();

        int i = 0;
        while (i < parameters.plantsPerDay()) {
            boolean placeOutside = false;
            if (random.nextDouble() < 0.8) {
                // Place plant inside the jungle
                if (!junglePositionGenerator.iterator().hasNext()) {
                    placeOutside = true;
                } else {
                    Vector2d plantPosition = junglePositionGenerator.iterator().next();
                    plantPosition = new Vector2d(plantPosition.getX(), plantPosition.getY() + jungleBottom);
                    if (!map.isOccupiedByPlant(plantPosition)) {
                        Plant plant = new Plant(plantPosition);
                        map.place(plant);
                        i++;
                    }
                }
            } else {
                placeOutside = true;
            }
            if (placeOutside) {
                // Place plant outside the jungle
                if (!outsideJunglePositionGenerator.iterator().hasNext()) {
                    break;
                }
                Vector2d plantPosition = outsideJunglePositionGenerator.iterator().next();
                if (plantPosition.getY() >= jungleBottom) {
                    plantPosition = new Vector2d(plantPosition.getX(), plantPosition.getY() + jungleHeight);
                }
                if (!map.isOccupiedByPlant(plantPosition)) {
                    Plant plant = new Plant(plantPosition);
                    map.place(plant);
                    i++;
                }
            }
        }
    }

    private MapDirection getRandomMapDirection() { //TODO: Create an util with functions like this
        int random = new java.util.Random().nextInt(8);
        return switch (random) {
            case 0 -> MapDirection.NORTH;
            case 1 -> MapDirection.NORTHEAST;
            case 2 -> MapDirection.EAST;
            case 3 -> MapDirection.SOUTHEAST;
            case 4 -> MapDirection.SOUTH;
            case 5 -> MapDirection.SOUTHWEST;
            case 6 -> MapDirection.WEST;
            case 7 -> MapDirection.NORTHWEST;
            default -> throw new IllegalStateException("Unexpected value: " + random);
        };
    }

    public WorldMap getMap() {
        return map;
    }

    public int getDay() {
        return day;
    }
}
