package agh.ics.darwin.model;

import agh.ics.darwin.model.variants.PlantGrowthVariant;

import java.util.*;

public class WorldMap {
    private final int width;
    private final int height;
    private final int startPlants;
    private final PlantGrowthVariant plantGrowthVariant;

    private final Map<Vector2d, ArrayList<Animal>> animals = new HashMap<>();
    private final Map<Vector2d, Plant> plants = new HashMap<>();
    //TODO: Jungle

    public WorldMap(int width, int height, int startPlants, PlantGrowthVariant plantGrowthVariant) {
        this.width = width;
        this.height = height;
        this.startPlants = startPlants;
        this.plantGrowthVariant = plantGrowthVariant;

        //Placing plants
        RandomUniquePositionGenerator randomUniquePositionGenerator = new RandomUniquePositionGenerator(width, height);
        for (int i = 0; i < startPlants; i++) {
            if (!randomUniquePositionGenerator.iterator().hasNext()) {
                throw new IllegalArgumentException("Not enough space for plants");
            }
            Vector2d plantPosition = randomUniquePositionGenerator.iterator().next();
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
        if (!insideBorders(position)) {
            throw new IllegalArgumentException("Position out of borders");
        }
        if (element.getClass() == Animal.class) {
            if (animals.containsKey(position)) {
                animals.get(position).add((Animal) element);
                animals.get(position).sort(Comparator.comparingInt(Animal::getEnergy)
                        .thenComparingInt(Animal::getAge)
                        .thenComparingInt(Animal::getNumberOfChildren)
                        .thenComparing(a -> new Random().nextInt()));
            } else {
                animals.put(position, new ArrayList<>(Collections.singletonList((Animal) element)));
            }
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
        if (element instanceof Animal) {
            ArrayList<Animal> animalsArray = animals.get(position);
            if (animalsArray.contains(element)) {
                if (animalsArray.size() == 1) {
                    animals.remove(position);
                } else {
                    animalsArray.remove(element);
                }
            }
        } else if (element instanceof Plant && plants.get(position) == element) {
            plants.remove(position, element);
        }
    }

    public void move(Animal animal) {
        Vector2d oldPosition = animal.getPosition();
        if (!animals.containsKey(oldPosition) || !animals.get(oldPosition).contains(animal)) {
            throw new IllegalArgumentException("Animal does not belong to this WorldMap");
        }
        animal.move(this);
    }

    public boolean isOccupiedByPlant(Vector2d position) {
        return plants.containsKey(position);
    }

    public boolean isOccupiedByAnimal(Vector2d position) {
        return animals.containsKey(position);
    }

    public boolean insideBorders(Vector2d position) {
        return position.follows(new Vector2d(0, 0)) && position.precedes(new Vector2d(width-1, height-1));
    }

    public Map<Vector2d, ArrayList<Animal>> getAnimals() {
        return animals;
    }

    public Map<Vector2d, Plant> getPlants() {
        return plants;
    }
}
