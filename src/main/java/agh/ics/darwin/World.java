package agh.ics.darwin;

import agh.ics.darwin.model.Genes;
import agh.ics.darwin.model.Parameters;
import agh.ics.darwin.model.WorldMap;
import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.MapVariant;
import agh.ics.darwin.model.variants.MutationVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;

public class World {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        Parameters parameters = testParameters();
        Simulation simulation = new Simulation(parameters);
        WorldMap initMap = simulation.getMap();
        initMap.getAnimals().forEach((k, v) -> System.out.println(k + " -> " + v));

        simulation.run();
        WorldMap finalMap = simulation.getMap();
        finalMap.getAnimals().forEach((k, v) -> System.out.println(k + " -> " + v));

    }

    private static Parameters testParameters() {
        return new Parameters(
                10,
                10,
                MapVariant.Globe,
                40,
                10,
                10,
                PlantGrowthVariant.Equator,
                20,
                20,
                15,
                10,
                0,
                2,
                MutationVariant.Random,
                8,
                BehaviourVariant.Predestination
        );
    }
}
