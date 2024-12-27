package agh.ics.darwin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomUniquePositionGeneratorTest {

    @Test
    void generatingInBorders() {
        RandomUniquePositionGenerator generator = new RandomUniquePositionGenerator(10, 10);
        while (generator.iterator().hasNext()) {
            Vector2d position = generator.iterator().next();
            assertTrue(position.getX() >= 0 && position.getX() < 10);
            assertTrue(position.getY() >= 0 && position.getY() < 10);
        }
    }

    @Test
    void generatingAllPositions() {
        RandomUniquePositionGenerator generator = new RandomUniquePositionGenerator(2, 2);
        int counter = 0;
        for (Vector2d position : generator) {
            counter++;
        }
        assertEquals(4, counter);
    }

    @Test
    void generatingUniquePositions() {
        RandomUniquePositionGenerator generator = new RandomUniquePositionGenerator(2, 2);
        Vector2d first = generator.iterator().next();
        Vector2d second = generator.iterator().next();
        Vector2d third = generator.iterator().next();
        Vector2d fourth = generator.iterator().next();
        assertNotEquals(first, second);
        assertNotEquals(first, third);
        assertNotEquals(first, fourth);
        assertNotEquals(second, third);
        assertNotEquals(second, fourth);
        assertNotEquals(third, fourth);
    }

}