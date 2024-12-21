package agh.ics.darwin.model;

public class Animal implements WorldElement {
    private MapDirection direction;
    private Vector2d position;
    private final Genes genes;
    private int energy;
    private int age = 0;
    private int numberOfChildren = 0;

    public Animal(Vector2d initialPosition, int initialEnergy) {
        this.position = initialPosition;
        this.energy = initialEnergy;

        int random = new java.util.Random().nextInt(8);
        this.direction = switch (random) {
            case 0 -> MapDirection.NORTH;
            case 1 -> MapDirection.NORTHEAST;
            case 2 -> MapDirection.EAST;
            case 3 -> MapDirection.SOUTHEAST;
            case 4 -> MapDirection.SOUTH;
            case 5 -> MapDirection.SOUTHWEST;
            case 6 -> MapDirection.WEST;
            case 7 -> MapDirection.NORTHWEST;
            default -> throw new IllegalStateException("Unexpected value: " + random);
        };

        int[] genesArray = new int[7];
        for (int i=0; i<7; i++) {
            genesArray[i] = new java.util.Random().nextInt(8);
        }
        this.genes = new Genes(genesArray);
    }

    @Override
    public Vector2d getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        return this.direction.toString();
    }

    public void move() {
        int currentGene = this.genes.getCurrentGene();
        for (int i=0; i<currentGene; i++) {
            this.direction = this.direction.next();
        }
        this.position = this.position.add(this.direction.toUnitVector());
        this.genes.nextGene();
    }

    public void loseEnergy(int energy) {
        if (energy <= 0) {
            throw new IllegalArgumentException("Energy loss must be greater than 0");
        }
        this.energy -= energy;
    }

    public int getEnergy() {
        return this.energy;
    }

    public int getAge() {
        return this.age;
    }

    public int getNumberOfChildren() {
        return this.numberOfChildren;
    }
}
