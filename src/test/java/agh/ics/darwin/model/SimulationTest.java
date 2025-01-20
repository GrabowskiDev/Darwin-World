package agh.ics.darwin.model;

import agh.ics.darwin.Simulation;
import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.MapVariant;
import agh.ics.darwin.model.variants.MutationVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SimulationTest {
    private static final Parameters parameters = new Parameters(
            20,
            20,
            MapVariant.Globe,
            0,
            10,
            20,
            PlantGrowthVariant.Equator,
            10,
            100,
            10,
            10,
            0,
            0,
            MutationVariant.Random,
            8,
            BehaviourVariant.Predestination);

    // Test Initialization
    @Test
    void testSimulationInitialization() {
        Simulation simulation = new Simulation(parameters);

        assertNotNull(simulation.getMap());
        assertEquals(0, simulation.getDay());
        assertFalse(simulation.isStarted());

        WorldMap map = simulation.getMap();
        assertEquals(0, map.getPlants().size());
    }

    // Test Simulation Run
    @Test
    void testSimulationRun() throws InterruptedException {
        Simulation simulation = new Simulation(parameters);

        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        Thread.sleep(1000);
        simulation.stop();
        simulationThread.join();

        assertTrue(simulation.getDay() > 0);
    }

    @Test
    void testPlantGrowth() throws InterruptedException{
        Simulation simulation = new Simulation(parameters);
        WorldMap map = simulation.getMap();

        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        Thread.sleep(1000);
        simulation.stop();
        simulationThread.join();

        int plantCount = map.getPlants().size();
        assertTrue(plantCount > 0);
    }

    @Test
    void testAnimalPlacement() {
        Simulation simulation = new Simulation(parameters);
        WorldMap map = simulation.getMap();
        ConcurrentHashMap<Vector2d, CopyOnWriteArrayList<Animal>> animals = map.getAnimals();
        int totalAnimals = animals.values().stream()
                .mapToInt(CopyOnWriteArrayList::size)
                .sum();
        assertEquals(parameters.startAnimals(), totalAnimals);
    }

    @Test
    void animalsAreDying() throws InterruptedException{
        Parameters parameters2 = new Parameters(
                100,
                100,
                MapVariant.Globe,
                0,
                1,
                0,
                PlantGrowthVariant.Equator,
                10,
                1,
                1000,
                1000,
                0,
                0,
                MutationVariant.Random,
                8,
                BehaviourVariant.Predestination);
        Simulation simulation = new Simulation(parameters2);

        Thread simulationThread = new Thread(simulation);
        simulationThread.start();
        Thread.sleep(2200);
        simulation.stop();
        simulationThread.join();

        WorldMap map = simulation.getMap();

        int totalAnimals = map.getAnimals().values().stream()
                .mapToInt(CopyOnWriteArrayList::size)
                .sum();
        assertEquals(0, totalAnimals, "It's still luck based, try running it one more time");
    }
}
