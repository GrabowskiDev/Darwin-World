package agh.ics.darwin.model;

public class Animal implements WorldElement {
    private MapDirection direction;
    private Vector2d position;
    private final Genes genes;
    private int energy;
    private int age = 0;
    private int numberOfChildren = 0;

    public Animal(Vector2d initialPosition, MapDirection direction, int initialEnergy, Genes genes) {
        this.position = initialPosition;
        this.energy = initialEnergy;
        this.genes = genes;
        this.direction = direction;
    }

    @Override
    public Vector2d getPosition() {
        return this.position;
    }

    public MapDirection getDirection() {
        return this.direction;
    }

    @Override
    public String toString() {
        return this.direction.toString();
    }

    public void move(WorldMap map) {
        int currentGene = this.genes.getCurrentGene();
        this.direction = this.direction.rotate(currentGene);

        int width = map.getWidth();
        int height = map.getHeight();
        Vector2d oldPosition = this.position;
        Vector2d newPosition = this.position.add(this.direction.toUnitVector());

        if (newPosition.getX() >= width) {
            newPosition = new Vector2d(0, newPosition.getY());
        } else if (newPosition.getX() < 0) {
            newPosition = new Vector2d(width-1, newPosition.getY());
        }

        if (newPosition.getY() >= height || newPosition.getY() < 0) {
            newPosition = new Vector2d(newPosition.getX(), oldPosition.getY());
            this.direction = this.direction.rotate(4);
        }

        this.position = newPosition;

        switch (map.getBehaviourVariant()) {
            case Predestination -> {
                this.genes.nextGene();
            }
            case Madness -> {
                if (new java.util.Random().nextDouble() < 0.8) {
                    this.genes.nextGene();
                } else {
                    this.genes.selectRandomGene();
                }
            }
        }
    }

    public void loseEnergy(int energy) {
        if (energy <= 0) {
            throw new IllegalArgumentException("Energy loss must be greater than 0");
        }
        this.energy -= energy;
    }

    public void gainEnergy(int energy) {
        if (energy <= 0) {
            throw new IllegalArgumentException("Energy gain must be greater than 0");
        }
        this.energy += energy;
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

    public void addChildren() {
        this.numberOfChildren += 1;
    }

    public void incrementAge() {
        this.age += 1;
    }

    public Genes getGenes() {
        return genes;
    }
}
