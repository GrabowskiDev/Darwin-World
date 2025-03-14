package agh.ics.darwin.model;

import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class WorldMapTest {

    @Test
    void testPlaceAnimal() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(2, 3), MapDirection.NORTH,10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        assertTrue(map.isOccupiedByAnimal(new Vector2d(2, 3)));
    }

    @Test
    void testPlacePlant() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Plant plant = new Plant(new Vector2d(2, 3));
        map.place(plant);
        assertTrue(map.isOccupiedByPlant(new Vector2d(2, 3)));
    }

    @Test
    void testRemoveAnimal() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(2, 3), MapDirection.NORTH,10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        map.remove(animal);
        assertFalse(map.isOccupiedByAnimal(new Vector2d(2, 3)));
    }

    @Test
    void testRemovePlant() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Plant plant = new Plant(new Vector2d(2, 3));
        map.place(plant);
        map.remove(plant);
        assertFalse(map.isOccupiedByPlant(new Vector2d(2, 3)));
    }

    @Test
    void testMoveAnimal() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(2, 3), MapDirection.NORTH,10, new Genes(new int[]{0, 0, 0, 0, 0, 0, 0, 0}));
        map.place(animal);
        map.move(animal);
        assertEquals(new Vector2d(2, 2), animal.getPosition());
    }

    @Test
    void testMoveAnimalMovingCorrectly() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Genes genes = new Genes(new int[]{0, 2, 4, 6});
        Animal animal = new Animal(new Vector2d(2, 2), MapDirection.NORTH,10, genes);
        map.place(animal);

        // Move the animal according to the genes
        MapDirection direction = animal.getDirection();
        int currentGene = animal.getGenes().getCurrentGene();
        Vector2d position = animal.getPosition();
        map.move(animal);
        assertEquals(position.add(direction.rotate(currentGene).toUnitVector()), animal.getPosition());

        direction = animal.getDirection();
        currentGene = animal.getGenes().getCurrentGene();
        position = animal.getPosition();
        map.move(animal);
        assertEquals(position.add(direction.rotate(currentGene).toUnitVector()), animal.getPosition());
    }

    @Test
    void animalDoesNotBelongToThisWorldMap() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(2, 3), MapDirection.NORTH,10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertThrows(IllegalArgumentException.class, () -> map.move(animal));
    }

    @Test
    void testInsideBorders() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        assertTrue(map.insideBorders(new Vector2d(5, 5)));
        assertFalse(map.insideBorders(new Vector2d(10, 10)));
    }

    @Test
    void testGetAnimals() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(2, 3), MapDirection.NORTH,10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        ConcurrentHashMap<Vector2d, CopyOnWriteArrayList<Animal>> animals = map.getAnimals();
        assertTrue(animals.containsKey(new Vector2d(2, 3)));
    }

    @Test
    void testGetPlants() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Plant plant = new Plant(new Vector2d(2, 3));
        map.place(plant);
        Map<Vector2d, WorldElement> plants = map.getPlants();
        assertTrue(plants.containsKey(new Vector2d(2, 3)));
    }

    @Test
    void testJungleBoundaries() {
        WorldMap map1 = new WorldMap(10, 1, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom1 = 0;
        int expectedJungleTop1 = 0;

        WorldMap map2 = new WorldMap(10, 2, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom2 = 0;
        int expectedJungleTop2 = 1;


        WorldMap map3 = new WorldMap(10, 3, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom3 = 1;
        int expectedJungleTop3 = 1;


        WorldMap map4 = new WorldMap(10, 4, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom4 = 1;
        int expectedJungleTop4 = 2;


        WorldMap map5 = new WorldMap(10, 5, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom5 = 2;
        int expectedJungleTop5 = 2;


        WorldMap map6 = new WorldMap(10, 6, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom6 = 2;
        int expectedJungleTop6 = 3;

        WorldMap map7 = new WorldMap(10, 100, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int expectedJungleBottom7 = 40;
        int expectedJungleTop7 = 59;

        assertEquals(expectedJungleBottom1, map1.getJungleBottom());
        assertEquals(expectedJungleTop1, map1.getJungleTop());

        assertEquals(expectedJungleBottom2, map2.getJungleBottom());
        assertEquals(expectedJungleTop2, map2.getJungleTop());

        assertEquals(expectedJungleBottom3, map3.getJungleBottom());
        assertEquals(expectedJungleTop3, map3.getJungleTop());

        assertEquals(expectedJungleBottom4, map4.getJungleBottom());
        assertEquals(expectedJungleTop4, map4.getJungleTop());

        assertEquals(expectedJungleBottom5, map5.getJungleBottom());
        assertEquals(expectedJungleTop5, map5.getJungleTop());

        assertEquals(expectedJungleBottom6, map6.getJungleBottom());
        assertEquals(expectedJungleTop6, map6.getJungleTop());

        assertEquals(expectedJungleBottom7, map7.getJungleBottom());
        assertEquals(expectedJungleTop7, map7.getJungleTop());
    }

    @Test
    void testJunglePlantDistribution() {
        WorldMap map = new WorldMap(100, 100, 200, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        int jungleBottom = map.getJungleBottom();
        int jungleTop = map.getJungleTop();
        int junglePlantCount = 0;
        int outsideJunglePlantCount = 0;

        for (Map.Entry<Vector2d, WorldElement> entry : map.getPlants().entrySet()) {
            Vector2d position = entry.getKey();
            if (position.getY() >= jungleBottom && position.getY() <= jungleTop) {
                junglePlantCount++;
            } else {
                outsideJunglePlantCount++;
            }
        }

        // Check if approximately 80% of the plants are in the jungle
        assertTrue(junglePlantCount > outsideJunglePlantCount);
    }

    @Test
    void testMovingRightOutOfBounds() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(9, 5), MapDirection.EAST,10, new Genes(new int[]{0}));
        map.place(animal);
        map.move(animal);
        assertEquals(new Vector2d(0, 5), animal.getPosition());
        assertEquals(MapDirection.EAST, animal.getDirection());
    }

    @Test
    void testMovingLeftOutOfBounds() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(0, 5), MapDirection.WEST,10, new Genes(new int[]{0}));
        map.place(animal);
        map.move(animal);
        assertEquals(new Vector2d(9, 5), animal.getPosition());
        assertEquals(MapDirection.WEST, animal.getDirection());
    }

    @Test
    void testMovingUpOutOfBounds() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(5, 0), MapDirection.NORTH,10, new Genes(new int[]{0}));
        Animal animal2 = new Animal(new Vector2d(2, 0), MapDirection.NORTHWEST,10, new Genes(new int[]{0}));
        Animal animal3 = new Animal(new Vector2d(7, 0), MapDirection.NORTHEAST,10, new Genes(new int[]{0}));

        map.place(animal);
        map.move(animal);
        map.place(animal2);
        map.move(animal2);
        map.place(animal3);
        map.move(animal3);

        assertEquals(new Vector2d(5, 0), animal.getPosition());
        assertEquals(MapDirection.SOUTH, animal.getDirection());
        assertEquals(new Vector2d(1, 0), animal2.getPosition());
        assertEquals(MapDirection.SOUTHEAST, animal2.getDirection());
        assertEquals(new Vector2d(8, 0), animal3.getPosition());
        assertEquals(MapDirection.SOUTHWEST, animal3.getDirection());
    }

    @Test
    void testMovingDownOutOfBounds() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(5, 9), MapDirection.SOUTH,10, new Genes(new int[]{0}));
        Animal animal2 = new Animal(new Vector2d(2, 9), MapDirection.SOUTHWEST,10, new Genes(new int[]{0}));
        Animal animal3 = new Animal(new Vector2d(7, 9), MapDirection.SOUTHEAST,10, new Genes(new int[]{0}));

        map.place(animal);
        map.move(animal);
        map.place(animal2);
        map.move(animal2);
        map.place(animal3);
        map.move(animal3);

        assertEquals(new Vector2d(5, 9), animal.getPosition());
        assertEquals(MapDirection.NORTH, animal.getDirection());
        assertEquals(new Vector2d(1, 9), animal2.getPosition());
        assertEquals(MapDirection.NORTHEAST, animal2.getDirection());
        assertEquals(new Vector2d(8, 9), animal3.getPosition());
        assertEquals(MapDirection.NORTHWEST, animal3.getDirection());
    }

    @Test
    void testAnimalsSortedByEnergy() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Vector2d position = new Vector2d(5, 5);

        Animal animal1 = new Animal(position, MapDirection.NORTH, 10, new Genes(new int[]{0}));
        Animal animal2 = new Animal(position, MapDirection.NORTH, 20, new Genes(new int[]{0}));
        Animal animal3 = new Animal(position, MapDirection.NORTH, 15, new Genes(new int[]{0}));

        map.place(animal1);
        map.place(animal2);
        map.place(animal3);

        CopyOnWriteArrayList<Animal> animalsAtPosition = map.getAnimals().get(position);
        assertNotNull(animalsAtPosition);
        assertEquals(3, animalsAtPosition.size());

        // Check if animals are sorted by energy level
        assertEquals(animal2, animalsAtPosition.get(0));
        assertEquals(animal3, animalsAtPosition.get(1));
        assertEquals(animal1, animalsAtPosition.get(2));
    }

    @Test
    void occupiedBySuperPlants() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        SuperPlant superPlant1 = new SuperPlant(new Vector2d(5, 5));

        map.place(superPlant1);

        assertTrue(map.isOccupiedByPlant(new Vector2d(5, 5)));
        assertTrue(map.isOccupiedByPlant(new Vector2d(6, 5)));
        assertTrue(map.isOccupiedByPlant(new Vector2d(6, 6)));
        assertTrue(map.isOccupiedByPlant(new Vector2d(5, 6)));

        assertFalse(map.isOccupiedByPlant(new Vector2d(4, 6)));
        assertFalse(map.isOccupiedByPlant(new Vector2d(4, 5)));
        assertFalse(map.isOccupiedByPlant(new Vector2d(4, 4)));
        assertFalse(map.isOccupiedByPlant(new Vector2d(5, 4)));
        assertFalse(map.isOccupiedByPlant(new Vector2d(6, 4)));
    }

    @Test
    void testSquareJungleDimensionsAndPositionSmallMap() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        assertEquals(4, map.getSquareJungleLength());
        assertEquals(new Vector2d(3, 3), map.getSquareJunglePosition());
    }

    @Test
    void testSquareJungleDimensionsAndPositionMediumMap() {
        WorldMap map = new WorldMap(20, 20, 0, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        assertEquals(8, map.getSquareJungleLength());
        assertEquals(new Vector2d(6, 6), map.getSquareJunglePosition());
    }

    @Test
    void testSquareJungleDimensionsAndPositionLargeMap() {
        WorldMap map = new WorldMap(50, 50, 0, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        assertEquals(22, map.getSquareJungleLength());
        assertEquals(new Vector2d(14, 14), map.getSquareJunglePosition());
    }

    @Test
    void testSquareJungleDimensionsAndPositionWide() {
        WorldMap map = new WorldMap(50, 10, 0, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        assertEquals(10, map.getSquareJungleLength());
        assertEquals(new Vector2d(20, 0), map.getSquareJunglePosition());
    }

    @Test
    void testSquareJungleDimensionsAndPositionTall() {
        WorldMap map = new WorldMap(10, 50, 0, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        assertEquals(10, map.getSquareJungleLength());
        assertEquals(new Vector2d(0, 20), map.getSquareJunglePosition());
    }

    @Test
    void superPlantsBeingPlacedInJungle() {
        WorldMap map = new WorldMap(20, 20, 400, PlantGrowthVariant.GoodHarvest, BehaviourVariant.Predestination);
        Vector2d junglePosition = map.getSquareJunglePosition();
        int jungleLength = map.getSquareJungleLength();

        for (Map.Entry<Vector2d, WorldElement> entry : map.getPlants().entrySet()) {
            if (entry.getValue() instanceof SuperPlant) {
                Vector2d position = entry.getKey();
                boolean insideJungle = position.follows(junglePosition) &&
                        position.precedes(junglePosition.add(new Vector2d(jungleLength - 2, jungleLength - 2))); //-2 because position is the upper left position of a plant
                assertTrue(insideJungle);
            }
        }
    }

}