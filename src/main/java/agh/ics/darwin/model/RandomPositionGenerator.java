package agh.ics.darwin.model;

import java.util.*;
import java.util.function.Consumer;

public class RandomPositionGenerator implements Iterable<Vector2d> {
    private final int maxWidth;
    private final int maxHeight;
    private final int grassCount;
    private final ArrayList<Vector2d> allPositions;
    private int currentIndex = 0;
    private final Random random;

    public RandomPositionGenerator(int maxWidth, int maxHeight, int grassCount) {
        this.grassCount = grassCount;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.allPositions = new ArrayList<>();
        this.random = new Random();

        generateAllPositions();
    }

    private void generateAllPositions() {
        for (int x = 0; x <= maxWidth; x++) {
            for (int y = 0; y <= maxHeight; y++) {
                allPositions.add(new Vector2d(x, y));
            }
        }
    }

    @Override
    public Iterator<Vector2d> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return currentIndex < grassCount;
            }

            @Override
            public Vector2d next() {
                int index = random.nextInt(allPositions.size() - currentIndex);
                currentIndex++;
                return allPositions.remove(index);
            }
        };
    }
}
