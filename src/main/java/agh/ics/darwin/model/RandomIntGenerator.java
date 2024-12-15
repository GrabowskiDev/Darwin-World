package agh.ics.darwin.model;

public class RandomIntGenerator implements Iterable<Integer> {
    private final int[] numbers;
    private int index = 0;

    public RandomIntGenerator(int maxNumber) {
        this.numbers = new int[maxNumber];
        for (int i = 0; i < maxNumber; i++) {
            this.numbers[i] = i;
        }
    }

    @Override
    public java.util.Iterator<Integer> iterator() {
        return new java.util.Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                int number = numbers[index];
                index = (index + 1) % numbers.length;
                return number;
            }
        };
    }
}
