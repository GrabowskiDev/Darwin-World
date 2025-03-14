package agh.ics.darwin.model;

import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class WorldMap {
    private final int width;
    private final int height;
    private final int startPlants;
    private final PlantGrowthVariant plantGrowthVariant;
    private final BehaviourVariant behaviourVariant;
    private final int jungleBottom;
    private final int jungleTop;
    private final Vector2d squareJunglePosition;
    private final int squareJungleLength;
    private final ConcurrentHashMap<Vector2d, CopyOnWriteArrayList<Animal>> animals = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Vector2d, CopyOnWriteArrayList<Animal>> deadAnimals = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Vector2d, WorldElement> plants = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Vector2d, SuperPlant> superPlants = new ConcurrentHashMap<>();
    protected final List<MapChangeListener> observers = new ArrayList<>();

    public void notifyObservers() {
        for (MapChangeListener observer : observers) {
            observer.mapChanged(this);
        }
    }

    public void addObserver(MapChangeListener observer) { observers.add(observer); }

    public void removeObserver(MapChangeListener observer) { observers.remove(observer); }

    public WorldMap(int width, int height, int startPlants, PlantGrowthVariant plantGrowthVariant, BehaviourVariant behaviourVariant) {
        this.width = width;
        this.height = height;
        this.startPlants = startPlants;
        this.plantGrowthVariant = plantGrowthVariant;
        this.behaviourVariant = behaviourVariant;

        // Equator
        if (plantGrowthVariant == PlantGrowthVariant.Equator) {
            this.squareJunglePosition = new Vector2d(0,0);
            this.squareJungleLength = 0;

            int jungleHeight = (int) Math.ceil(height / 5.0);
            if ((height%2 != 0 && jungleHeight%2 == 0) || (height%2 == 0 && jungleHeight%2 != 0)) {
                jungleHeight++;
            }
            this.jungleBottom = (height - jungleHeight) / 2;
            this.jungleTop = jungleBottom + jungleHeight - 1;

            RandomUniquePositionGenerator junglePositionGenerator = new RandomUniquePositionGenerator(width, jungleHeight);
            RandomUniquePositionGenerator outsideJunglePositionGenerator = new RandomUniquePositionGenerator(width, height - jungleHeight);
            Random random = new Random();

            for (int i = 0; i < startPlants; i++) {
                boolean placeOutside = false;
                if (random.nextDouble() < 0.8) {
                    // Place plant inside the jungle
                    if (!junglePositionGenerator.iterator().hasNext()) {
                        placeOutside = true;
                    } else {
                        Vector2d plantPosition = junglePositionGenerator.iterator().next();
                        plantPosition = new Vector2d(plantPosition.getX(), plantPosition.getY() + jungleBottom); // Adjust y-coordinate to jungle range
                        Plant plant = new Plant(plantPosition);
                        plants.put(plantPosition, plant);
                    }
                } else {
                    placeOutside = true;
                }
                if (placeOutside) {
                    // Place plant outside the jungle
                    if (!outsideJunglePositionGenerator.iterator().hasNext()) {
                        throw new IllegalArgumentException("Not enough space for plants");
                    }
                    Vector2d plantPosition = outsideJunglePositionGenerator.iterator().next();
                    if (plantPosition.getY() >= jungleBottom) {
                        plantPosition = new Vector2d(plantPosition.getX(), plantPosition.getY() + jungleHeight); // Adjust y-coordinate to skip jungle range
                    }
                    Plant plant = new Plant(plantPosition);
                    plants.put(plantPosition, plant);
                }
            }
        } else {
            // Square jungle
            this.jungleBottom = 0;
            this.jungleTop = 0;

            this.squareJungleLength = Math.min(Math.min(this.width, this.height), (int) Math.floor(Math.sqrt(width*height*0.2)));

            int x = (width - squareJungleLength)/2;
            int y = (height - squareJungleLength)/2;
            this.squareJunglePosition = new Vector2d(x, y);

            RandomUniquePositionGenerator positionGenerator = new RandomUniquePositionGenerator(width, height);
            RandomUniquePositionGenerator junglePositionGenerator = new RandomUniquePositionGenerator(squareJungleLength, squareJungleLength);
            Random random = new Random();
            for (int i = 0; i < startPlants; i++) {
                if (random.nextDouble() < 0.8) {
                    while (true) {
                        Vector2d plantPosition = positionGenerator.iterator().next();
                        if (!isOccupiedByPlant(plantPosition)) {
                            Plant plant = new Plant(plantPosition);
                            plants.put(plantPosition, plant);
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

                        if (!isOccupiedByPlant(plantPosition) && !isOccupiedByPlant(plantPosition2) && !isOccupiedByPlant(plantPosition3) && !isOccupiedByPlant(plantPosition4)) {
                            SuperPlant superPlant = new SuperPlant(plantPosition);
                            plants.put(plantPosition, superPlant);
                            break;
                        }
                    }
                }
            }
        }
    }



    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public synchronized void place(WorldElement element) {
        Vector2d position = element.getPosition();
        if (!insideBorders(position)) {
            throw new IllegalArgumentException("Position out of borders");
        }
        if (element.getClass() == Animal.class) {
            if (animals.containsKey(position)) {
                animals.get(position).add((Animal) element);
                animals.get(position).sort(Comparator.comparingInt(Animal::getEnergy).reversed()
                        .thenComparingInt(Animal::getAge).reversed()
                        .thenComparingInt(Animal::getNumberOfChildren).reversed()
                        .thenComparing(a -> new Random().nextInt()));
            } else {
                animals.put(position, new CopyOnWriteArrayList<>(Collections.singletonList((Animal) element)));
            }
        } else if (element.getClass() == Plant.class) {
            if (isOccupiedByPlant(position)) {
                throw new IllegalArgumentException("Cannot place plant on another plant");
            } else {
                plants.put(position, (Plant) element);
            }
        } else if (element.getClass() == SuperPlant.class) {
            Vector2d position2 = position.add(new Vector2d(1, 0));
            Vector2d position3 = position.add(new Vector2d(1, 1));
            Vector2d position4 = position.add(new Vector2d(0, 1));

            if (isOccupiedByPlant(position)) {
                throw new IllegalArgumentException("Cannot place plant on another plant");
            } else {
                plants.put(position, element);
            }
        }
    }

    public synchronized void remove(WorldElement element) {
        Vector2d position = element.getPosition();
        if (element instanceof Animal) {
            CopyOnWriteArrayList<Animal> animalsArray = animals.get(position);
            if (animalsArray != null && animalsArray.contains(element)) {
                if (animalsArray.size() == 1) {
                    animals.remove(position);
                } else {
                    animalsArray.remove(element);
                }
            }
            deadAnimals.computeIfAbsent(position, k -> new CopyOnWriteArrayList<>()).add((Animal) element);
        } else if ((element instanceof Plant && plants.get(position) == element) ||
                (element instanceof SuperPlant && plants.get(position) == element)) {
            plants.remove(position, element);
        }
    }

    public synchronized void move(Animal animal) {
        Vector2d oldPosition = animal.getPosition();
        if (!animals.containsKey(oldPosition) || !animals.get(oldPosition).contains(animal)) {
            throw new IllegalArgumentException("Animal does not belong to this WorldMap");
        }
        animal.move(this);
        Vector2d newPosition = animal.getPosition();
        CopyOnWriteArrayList<Animal> animalsArray = animals.get(oldPosition);
        if (animalsArray.size() == 1) {
            animals.remove(oldPosition);
        } else {
            animalsArray.remove(animal);
        }
        place(animal);

    }

    public boolean isOccupiedByPlant(Vector2d position) {
        if (plantGrowthVariant == PlantGrowthVariant.Equator) {
            return plants.containsKey(position);
        } else {
            //super plant
            Vector2d pos1 = position.add(new Vector2d(0, -1));
            Vector2d pos2 = position.add(new Vector2d(-1, -1));
            Vector2d pos3 = position.add(new Vector2d(-1, 0));

            WorldElement el1 = plants.get(pos1);
            WorldElement el2 = plants.get(pos2);
            WorldElement el3 = plants.get(pos3);

            return (plants.containsKey(position) || el1 instanceof SuperPlant ||
                    el2 instanceof SuperPlant || el3 instanceof SuperPlant);
        }
    }

    public boolean isOccupiedByAnimal(Vector2d position) {
        return animals.containsKey(position);
    }

    public boolean insideBorders(Vector2d position) {
        return position.follows(new Vector2d(0, 0)) && position.precedes(new Vector2d(width-1, height-1));
    }

    public ConcurrentHashMap<Vector2d, CopyOnWriteArrayList<Animal>> getAnimals() {
        return animals;
    }

    public ConcurrentHashMap<Vector2d, CopyOnWriteArrayList<Animal>> getDeadAnimals() {
        return deadAnimals;
    }

    public Map<Vector2d, WorldElement> getPlants() {
        return plants;
    }

    public int getJungleBottom() {
        return jungleBottom;
    }

    public int getJungleTop() {
        return jungleTop;
    }

    public Vector2d getSquareJunglePosition() {
        return squareJunglePosition;
    }

    public int getSquareJungleLength() {
        return squareJungleLength;
    }

    public BehaviourVariant getBehaviourVariant() {
        return behaviourVariant;
    }

    public PlantGrowthVariant getPlantGrowthVariant() {
        return plantGrowthVariant;
    }

    public Animal getAnimalById(int id) {
        for (CopyOnWriteArrayList<Animal> animalsArray : animals.values()) {
            for (Animal animal : animalsArray) {
                if (animal.getId() == id) {
                    return animal;
                }
            }
        }
        return null;
    }
}
