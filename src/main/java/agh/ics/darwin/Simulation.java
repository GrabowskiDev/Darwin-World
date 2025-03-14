package agh.ics.darwin;

import agh.ics.darwin.model.*;
import agh.ics.darwin.model.variants.PlantGrowthVariant;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Simulation implements Runnable {
    private final Lock lock = new ReentrantLock();
    private final Condition pausedCondition = lock.newCondition();
    private final Parameters parameters;
    private final WorldMap map;
    private int sleepDuration = 700;
    private final List<DailyStatistics> dailyStatistics = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger day = new AtomicInteger(0);
    private volatile boolean isPaused = false;
    private volatile boolean isStarted = false;
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
            for (int j = 0; j < genesArray.length; j++) {
                genesArray[j] = new Random().nextInt(8);
            }

            Animal animal = new Animal(animalPosition, getRandomMapDirection(), parameters.startEnergy(), new Genes(genesArray));
            map.place(animal);
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public synchronized void pause() {
        lock.lock();
        try {
            isPaused = true;
        } finally {
            lock.unlock();
        }
    }

    public synchronized void resume() {
        lock.lock();
        try {
            isPaused = false;
            pausedCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public synchronized void stop() {
        lock.lock();
        try {
            isFinished = true;
            pausedCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void run() {
        isStarted = true;
        while (true) {
            lock.lock();
            try {
                while (isPaused) {
                    pausedCondition.await();
                }
                if (isFinished) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            removeDeadAnimals();
            moveAnimals();
            eatPlants();
            reproduceAnimals();
            growNewPlants();
            day.incrementAndGet();
            map.notifyObservers();
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeDeadAnimals() {
        map.getAnimals().values().parallelStream().forEach(animals -> {
            List<Animal> deadAnimals = new ArrayList<>();
            for (Animal animal : animals) {
                if (animal.getEnergy() <= 0) {
                    animal.setDayOfDeath(day.get());
                    deadAnimals.add(animal);
                }
            }
            if (!deadAnimals.isEmpty()) {
                animals.removeAll(deadAnimals);
                deadAnimals.forEach(map::remove);
            }
        });
    }

    private void moveAnimals() {
        map.getAnimals().values().parallelStream().flatMap(List::stream).forEach(animal -> {
            map.move(animal);
            animal.loseEnergy(1);
            animal.incrementAge();
        });
    }

    private void eatPlants() {
        if (parameters.plantGrowth() == PlantGrowthVariant.Equator) {
            map.getPlants().entrySet().parallelStream().forEach(entry -> {
                Vector2d plantPosition = entry.getKey();
                Plant plant = (Plant) entry.getValue();
                if (map.isOccupiedByAnimal(plantPosition)) {
                    CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition);
                    if (animals != null && !animals.isEmpty()) {
                        Animal animal = animals.get(0); // Assuming the list is sorted by strength
                        animal.gainEnergy(parameters.plantEnergy());
                        animal.addPlantsEaten();
                        map.remove(plant);
                    }
                }
            });
        } else {
            //square jungle
            map.getPlants().entrySet().parallelStream().forEach(entry -> {
                Vector2d plantPosition = entry.getKey();
                WorldElement plantElement = entry.getValue();
                if (plantElement.getClass() == Plant.class) {
                    if (map.isOccupiedByAnimal(plantPosition)) {
                        CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition);
                        if (animals != null && !animals.isEmpty()) {
                            Animal animal = animals.get(0); // Assuming the list is sorted by strength
                            animal.gainEnergy(parameters.plantEnergy());
                            animal.addPlantsEaten();
                            map.remove(plantElement);
                        }
                    }
                } else if (plantElement.getClass() == SuperPlant.class) {
                    List<Animal> animalsArray = new ArrayList<>();

                    Vector2d plantPosition2 = plantPosition.add(new Vector2d(1,0));
                    Vector2d plantPosition3 = plantPosition.add(new Vector2d(1,1));
                    Vector2d plantPosition4 = plantPosition.add(new Vector2d(0,1));

                    if (map.isOccupiedByAnimal(plantPosition)) {
                        CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition);
                        if (animals != null && !animals.isEmpty()) {
                            Animal animal = animals.get(0);
                            animalsArray.add(animal);
                        }
                    }
                    if (map.isOccupiedByAnimal(plantPosition2)) {
                        CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition2);
                        if (animals != null && !animals.isEmpty()) {
                            Animal animal = animals.get(0);
                            animalsArray.add(animal);
                        }
                    }
                    if (map.isOccupiedByAnimal(plantPosition3)) {
                        CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition3);
                        if (animals != null && !animals.isEmpty()) {
                            Animal animal = animals.get(0);
                            animalsArray.add(animal);
                        }
                    }
                    if (map.isOccupiedByAnimal(plantPosition4)) {
                        CopyOnWriteArrayList<Animal> animals = map.getAnimals().get(plantPosition4);
                        if (animals != null && !animals.isEmpty()) {
                            Animal animal = animals.get(0);
                            animalsArray.add(animal);
                        }
                    }

                    animalsArray.sort(Comparator.comparingInt(Animal::getEnergy).reversed()
                            .thenComparingInt(Animal::getAge).reversed()
                            .thenComparingInt(Animal::getNumberOfChildren).reversed()
                            .thenComparing(a -> new Random().nextInt()));

                    if (animalsArray.isEmpty()) {return;}
                    Animal animal = animalsArray.get(0);
                    animal.gainEnergy(parameters.plantEnergy() * 3);
                    animal.addPlantsEaten();
                    map.remove(plantElement);
                }
            });
        }
    }

    private void reproduceAnimals() {
        map.getAnimals().values().parallelStream().forEach(animals -> {
            for (int i = 0; i < animals.size() - 1; i += 2) {
                Animal parent1 = animals.get(i);
                Animal parent2 = animals.get(i + 1);
                if (parent2.getEnergy() >= parameters.energyToBeFed()) {
                    int totalEnergy = parent1.getEnergy() + parent2.getEnergy();
                    int parent1Len = (int) Math.ceil(((double) parent1.getEnergy() / totalEnergy) * parameters.genomeLength());
                    int parent2Len = parameters.genomeLength() - parent1Len;
                    Genes childGenes = new Genes(parent1.getGenes().getGenes(), parent2.getGenes().getGenes(), parent1Len, parent2Len, parameters.minMutations(), parameters.maxMutations());

                    Animal child = new Animal(parent1.getPosition(), getRandomMapDirection(), parameters.energyUsedToBreed() * 2, childGenes, parent1.getId(), parent2.getId());
                    map.place(child);
                    parent1.loseEnergy(parameters.energyUsedToBreed());
                    parent2.loseEnergy(parameters.energyUsedToBreed());
                    parent1.addChildren();
                    parent2.addChildren();
                    handleDescendants(child);
                } else {
                    break;
                }
            }
        });
    }

    private void handleDescendants(Animal child) {
        List<Integer> parents = child.getParentsId();
        if (parents.get(0) != null) {
            Animal parent = map.getAnimalById(parents.get(0));
            if (parent != null) {
                parent.addDescendants(1);
                handleDescendants(parent);
            }
        }
        if (parents.get(1) != null) {
            Animal parent = map.getAnimalById(parents.get(1));
            if (parent != null) {
                parent.addDescendants(1);
                handleDescendants(parent);
            }
        }
    }

    private void growNewPlants() {
        if (parameters.plantGrowth() == PlantGrowthVariant.Equator) {
            int jungleBottom = map.getJungleBottom();
            int jungleHeight = map.getJungleTop() - map.getJungleBottom() + 1;

            RandomUniquePositionGenerator junglePositionGenerator = new RandomUniquePositionGenerator(parameters.width(), jungleHeight);
            RandomUniquePositionGenerator outsideJunglePositionGenerator = new RandomUniquePositionGenerator(parameters.width(), parameters.height() - jungleHeight);
            Random random = new Random();

            int i = 0;
            while (i < parameters.plantsPerDay()) {
                boolean placeOutside = false;
                if (random.nextDouble() < 0.8) {
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
        } else {
            //square jungle
            int squareJungleLength = map.getSquareJungleLength();
            Vector2d squareJunglePosition = map.getSquareJunglePosition();

            RandomUniquePositionGenerator positionGenerator = new RandomUniquePositionGenerator(parameters.width(), parameters.height());
            RandomUniquePositionGenerator junglePositionGenerator = new RandomUniquePositionGenerator(squareJungleLength, squareJungleLength);
            Random random = new Random();
            for (int i = 0; i < parameters.plantsPerDay(); i++) {
                if (random.nextDouble() < 0.8) {
                    while (positionGenerator.iterator().hasNext()) {
                        Vector2d plantPosition = positionGenerator.iterator().next();
                        if (!map.isOccupiedByPlant(plantPosition)) {
                            Plant plant = new Plant(plantPosition);
                            map.place(plant);
                            break;
                        }
                    }
                } else {
                    while (junglePositionGenerator.iterator().hasNext()) {
                        Vector2d plantPosition = (junglePositionGenerator.iterator().next()).add(squareJunglePosition);
                        if (plantPosition.getX() == squareJunglePosition.getX() + squareJungleLength - 1) {
                            plantPosition = new Vector2d(plantPosition.getX() - 1, plantPosition.getY());
                        }
                        if (plantPosition.getY() == squareJunglePosition.getY() + squareJungleLength - 1) {
                            plantPosition = new Vector2d(plantPosition.getX(), plantPosition.getY() - 1);
                        }

                        Vector2d plantPosition2 = plantPosition.add(new Vector2d(1, 0));
                        Vector2d plantPosition3 = plantPosition.add(new Vector2d(1, 1));
                        Vector2d plantPosition4 = plantPosition.add(new Vector2d(0, 1));

                        if (!map.isOccupiedByPlant(plantPosition) && !map.isOccupiedByPlant(plantPosition2) && !map.isOccupiedByPlant(plantPosition3) && !map.isOccupiedByPlant(plantPosition4)) {
                            SuperPlant superPlant = new SuperPlant(plantPosition);
                            map.place(superPlant);
                            break;
                        }
                    }
                }
            }
        }
    }

    private MapDirection getRandomMapDirection() {
        int random = new Random().nextInt(8);
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
        return day.get();
    }
}