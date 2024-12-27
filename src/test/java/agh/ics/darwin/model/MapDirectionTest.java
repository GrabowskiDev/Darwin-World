package agh.ics.darwin.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class MapDirectionTest {

    @Test
    void testToString() {
        assertEquals("^", MapDirection.NORTH.toString());
        assertEquals("↗", MapDirection.NORTHEAST.toString());
        assertEquals(">", MapDirection.EAST.toString());
        assertEquals("↘", MapDirection.SOUTHEAST.toString());
        assertEquals("v", MapDirection.SOUTH.toString());
        assertEquals("↙", MapDirection.SOUTHWEST.toString());
        assertEquals("<", MapDirection.WEST.toString());
        assertEquals("↖", MapDirection.NORTHWEST.toString());
    }

    @Test
    void next() {
        assertEquals(MapDirection.NORTHEAST, MapDirection.NORTH.next());
        assertEquals(MapDirection.EAST, MapDirection.NORTHEAST.next());
        assertEquals(MapDirection.SOUTHEAST, MapDirection.EAST.next());
        assertEquals(MapDirection.SOUTH, MapDirection.SOUTHEAST.next());
        assertEquals(MapDirection.SOUTHWEST, MapDirection.SOUTH.next());
        assertEquals(MapDirection.WEST, MapDirection.SOUTHWEST.next());
        assertEquals(MapDirection.NORTHWEST, MapDirection.WEST.next());
        assertEquals(MapDirection.NORTH, MapDirection.NORTHWEST.next());
    }

    @Test
    void rotate() {
        assertEquals(MapDirection.NORTH, MapDirection.NORTH.rotate(-5));
        assertEquals(MapDirection.NORTH, MapDirection.NORTH.rotate(0));
        assertEquals(MapDirection.NORTHEAST, MapDirection.NORTH.rotate(1));
        assertEquals(MapDirection.EAST, MapDirection.NORTH.rotate(2));
        assertEquals(MapDirection.SOUTHEAST, MapDirection.NORTH.rotate(3));
        assertEquals(MapDirection.SOUTH, MapDirection.NORTH.rotate(4));
        assertEquals(MapDirection.SOUTHWEST, MapDirection.NORTH.rotate(5));
        assertEquals(MapDirection.WEST, MapDirection.NORTH.rotate(6));
        assertEquals(MapDirection.NORTHWEST, MapDirection.NORTH.rotate(7));
        assertEquals(MapDirection.NORTH, MapDirection.NORTH.rotate(8));
    }

    @Test
    void toUnitVector() {
        assertEquals(new Vector2d(0, 1), MapDirection.NORTH.toUnitVector());
        assertEquals(new Vector2d(1, 1), MapDirection.NORTHEAST.toUnitVector());
        assertEquals(new Vector2d(1, 0), MapDirection.EAST.toUnitVector());
        assertEquals(new Vector2d(1, -1), MapDirection.SOUTHEAST.toUnitVector());
        assertEquals(new Vector2d(0, -1), MapDirection.SOUTH.toUnitVector());
        assertEquals(new Vector2d(-1, -1), MapDirection.SOUTHWEST.toUnitVector());
        assertEquals(new Vector2d(-1, 0), MapDirection.WEST.toUnitVector());
        assertEquals(new Vector2d(-1, 1), MapDirection.NORTHWEST.toUnitVector());
    }
}