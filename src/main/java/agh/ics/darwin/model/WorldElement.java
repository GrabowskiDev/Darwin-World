package agh.ics.darwin.model;

/**
 * The interface responsible for objects that will be a part of a world.
 */

public interface WorldElement {

    /**
     * Gets the position of an element
     *
     * @return Position of an element.
     */
    Vector2d getPosition();

    /**
     * Converts element to graphical representation
     *
     * @return String of a graphical representation of an element.
     */
    String toString();

}
