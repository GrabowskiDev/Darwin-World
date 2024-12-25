package agh.ics.darwin.model;

import org.junit.jupiter.api.Test;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
class Vector2dTest {
    Vector2d vector = new Vector2d(1, 2);

    @Test
    void getX() {
        assertEquals(1, vector.getX());
    }

    @Test
    void getY() {
        assertEquals(2, vector.getY());
    }

    @Test
    void testToString() {
        assertEquals("(1, 2)", vector.toString());
    }

    @Test
    void add() {
        Vector2d vector2 = new Vector2d(3, 4);
        assertEquals(new Vector2d(4, 6), vector.add(vector2));
        assertEquals(new Vector2d(0, 0), vector.add(new Vector2d(-1, -2)));
    }

    @Test
    void testEquals() {
        assertTrue(vector.equals(new Vector2d(1, 2)));
        assertFalse(vector.equals(new Vector2d(1, 3)));
        assertFalse(vector.equals(new Vector2d(3, 2)));
        assertFalse(vector.equals(new Vector2d(3, 4)));
        assertFalse(vector.equals(new Vector2d(0, 0)));
        assertFalse(vector.equals(new Vector2d(-1, -2)));
        assertFalse(vector.equals(new Object()));
        assertFalse(vector.equals(null));
        assertFalse(vector.equals("(1, 2)"));
    }
}