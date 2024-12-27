package agh.ics.darwin.model;

import java.util.*;

public class RandomUniquePositionGenerator implements Iterable<Vector2d> {
    private final int maxWidth;
    private final int maxHeight;
    private final ArrayList<Vector2d> allPositions;
    private int currentIndex = 0;
    private final Random random;

    public RandomUniquePositionGenerator(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.allPositions = new ArrayList<>();
        this.random = new Random();

        generateAllPositions();
    }

    private void generateAllPositions() {
        for (int x = 0; x < maxWidth; x++) {
            for (int y = 0; y < maxHeight; y++) {
                allPositions.add(new Vector2d(x, y));
            }
        }
    }

    @Override
    public Iterator<Vector2d> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !allPositions.isEmpty();
            }

            @Override
            public Vector2d next() {
                int index = random.nextInt(allPositions.size());
                currentIndex++;
                return allPositions.remove(index);
            }
        };
    }
}
