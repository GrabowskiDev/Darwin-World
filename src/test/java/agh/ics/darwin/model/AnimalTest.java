package agh.ics.darwin.model;

import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimalTest {
    @Test
    void getPosition() {
        Vector2d position = new Vector2d(2, 3);
        Animal animal = new Animal(position, 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertEquals(position, animal.getPosition());
    }

    @Test
    void testToString() {
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertNotNull(animal.toString());
    }

    @Test
    void move() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        Vector2d oldPosition = animal.getPosition();
        animal.move(map);
        assertNotEquals(oldPosition, animal.getPosition());
    }

    @Test
    void loseEnergy() {
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        animal.loseEnergy(5);
        assertEquals(5, animal.getEnergy());
    }

    @Test
    void gainEnergy() {
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        animal.gainEnergy(5);
        assertEquals(15, animal.getEnergy());
    }

    @Test
    void getEnergy() {
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertEquals(10, animal.getEnergy());
    }

    @Test
    void getAge() {
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertEquals(0, animal.getAge());
    }

    @Test
    void getNumberOfChildren() {
        Animal animal = new Animal(new Vector2d(2, 3), 10, new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7}));
        assertEquals(0, animal.getNumberOfChildren());
    }

    @Test
    void getGenes() {
        Genes genes = new Genes(new int[]{0, 1, 2, 3, 4, 5, 6, 7});
        Animal animal = new Animal(new Vector2d(2, 3), 10, genes);
        assertEquals(genes, animal.getGenes());
    }

    @Test
    void testGenesMoveAnimalCorrectly() {
        WorldMap map = new WorldMap(10, 10, 0, PlantGrowthVariant.Equator, BehaviourVariant.Predestination);
        Genes genes = new Genes(new int[]{0, 2, 4, 6}); // Define specific genes for testing
        Animal animal = new Animal(new Vector2d(2, 2), 10, genes);

        // Move the animal according to the genes
        MapDirection direction = animal.getDirection();
        int currentGene = animal.getGenes().getCurrentGene();
        Vector2d position = animal.getPosition();
        animal.move(map);
        assertEquals(position.add(direction.rotate(currentGene).toUnitVector()), animal.getPosition());
    }

}