package agh.ics.darwin.model;

public class DailyStatistics {
    private final int day;
    private final int numAnimals;
    private final int numPlants;
    private final double avgEnergy;
    private final double avgLifespan;
    private final double avgChildren;

    public DailyStatistics(int day, int numAnimals, int numPlants, double avgEnergy, double avgLifespan, double avgChildren) {
        this.day = day;
        this.numAnimals = numAnimals;
        this.numPlants = numPlants;
        this.avgEnergy = avgEnergy;
        this.avgLifespan = avgLifespan;
        this.avgChildren = avgChildren;
    }

    public int getDay() {
        return day;
    }

    public int getNumAnimals() {
        return numAnimals;
    }

    public int getNumPlants() {
        return numPlants;
    }

    public double getAvgEnergy() {
        return avgEnergy;
    }

    public double getAvgLifespan() {
        return avgLifespan;
    }

    public double getAvgChildren() {
        return avgChildren;
    }
}
