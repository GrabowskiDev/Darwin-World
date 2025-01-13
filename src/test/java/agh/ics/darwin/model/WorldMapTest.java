package agh.ics.darwin.model;

import agh.ics.darwin.model.variants.PlantGrowthVariant;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WorldMapTest {

    @Test
    void testPlaceAnimal() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        assertTrue(map.isOccupiedByAnimal(new Vector2d(2, 3)));
    }

    @Test
    void testPlacePlant() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Plant plant = new Plant(new Vector2d(2, 3));
        map.place(plant);
        assertTrue(map.isOccupiedByPlant(new Vector2d(2, 3)));
    }

    @Test
    void testRemoveAnimal() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        map.remove(animal);
        assertFalse(map.isOccupiedByAnimal(new Vector2d(2, 3)));
    }

    @Test
    void testRemovePlant() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Plant plant = new Plant(new Vector2d(2, 3));
        map.place(plant);
        map.remove(plant);
        assertFalse(map.isOccupiedByPlant(new Vector2d(2, 3)));
    }

    @Test
    void testMoveAnimal() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        map.move(animal);
        assertNotEquals(new Vector2d(2, 3), animal.getPosition());
    }

    @Test
    void testMoveAnimalMovingCorrectly() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Genes genes = new Genes(new int[]{0, 2, 4, 6});
        Animal animal = new Animal(new Vector2d(2, 2), 10, genes);
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
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertThrows(IllegalArgumentException.class, () -> map.move(animal));
    }

    @Test
    void testInsideBorders() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        assertTrue(map.insideBorders(new Vector2d(5, 5)));
        assertFalse(map.insideBorders(new Vector2d(10, 10)));
    }

    @Test
    void testGetAnimals() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        map.place(animal);
        Map<Vector2d, ArrayList<Animal>> animals = map.getAnimals();
        assertTrue(animals.containsKey(new Vector2d(2, 3)));
    }

    @Test
    void testGetPlants() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator);
        Plant plant = new Plant(new Vector2d(2, 3));
        map.place(plant);
        Map<Vector2d, Plant> plants = map.getPlants();
        assertTrue(plants.containsKey(new Vector2d(2, 3)));
    }

    @Test
    void testJungleBoundaries() {
        WorldMap map1 = new WorldMap(10, 1, 0, PlantGrowthVariant.Equator);
        int expectedJungleBottom1 = 0;
        int expectedJungleTop1 = 0;

        WorldMap map2 = new WorldMap(10, 2, 0, PlantGrowthVariant.Equator);
        int expectedJungleBottom2 = 0;
        int expectedJungleTop2 = 1;


        WorldMap map3 = new WorldMap(10, 3, 0, PlantGrowthVariant.Equator);
        int expectedJungleBottom3 = 1;
        int expectedJungleTop3 = 1;


        WorldMap map4 = new WorldMap(10, 4, 0, PlantGrowthVariant.Equator);
        int expectedJungleBottom4 = 1;
        int expectedJungleTop4 = 2;


        WorldMap map5 = new WorldMap(10, 5, 0, PlantGrowthVariant.Equator);
        int expectedJungleBottom5 = 2;
        int expectedJungleTop5 = 2;


        WorldMap map6 = new WorldMap(10, 6, 0, PlantGrowthVariant.Equator);
        int expectedJungleBottom6 = 2;
        int expectedJungleTop6 = 3;

        WorldMap map7 = new WorldMap(10, 100, 0, PlantGrowthVariant.Equator);
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
        WorldMap map = new WorldMap(100, 100, 200, PlantGrowthVariant.Equator);
        int jungleBottom = map.getJungleBottom();
        int jungleTop = map.getJungleTop();
        int junglePlantCount = 0;
        int outsideJunglePlantCount = 0;

        for (Map.Entry<Vector2d, Plant> entry : map.getPlants().entrySet()) {
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
}