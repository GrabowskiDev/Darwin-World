# Darwin-World

## Overview

The Darwin Simulation Project is a Java-based simulation that models the behavior of animals in a virtual world. The simulation includes various features such as animal movement, plant growth, reproduction with genes simulaton, fighting for food and jungle with bigger plants as well as equator with more plants. 

## Features

- **Animal Movement**: Animals move around the map based on their genes and behavior variant. The map functions like a globe so going east out of bounds means you pop up on the west side of the map. Going north means you stay on north but rotated 180 degrees.
- **Behaviour Variants**: Different genetic variants influence animal behavior and mutation.
- **Plant Growth**: Plants grow on the map according to the Jungle and Equator variants.
- **Jungle and Equator**: The map has two variants: Jungle and Equator. The jungle has bigger plants that give 3 times more food, while equator has more plants.
- **Reproduction**: Animals can reproduce if they meet certain energy requirements. While reproducing, both animals give some of their genes to the offspring (number of genes recieved from each parent is based on the energy difference between the two parents). Some genes can also mutate, giving in result different genes
- **Energy**: Animals have energy that they use to move and reproduce. They gain energy by eating plants.
- **Death**: Animals die when their energy reaches zero. This way we can clearly see the behaviour of animals on the map, and their battle for survival.
- **Animal Interaction**: Animals interact with each other and the environment, where only the strongest one can eat a plant, and where they pair up for reproduction based on their energy levels and other criteria.

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/GrabowskiDev/darwin-simulation.git
    cd darwin-simulation
    ```

2. Build the project using Gradle:
    ```sh
    ./gradlew build
    ```

### Running the Simulation

To run the simulation, execute the following command:
```sh
./gradlew run
```

### Running Tests

To run the unit tests, execute the following command:
```sh
./gradlew test
```

## Usage

### Simulation Parameters

The simulation parameters are defined in the `Parameters` class. You can customize the simulation by modifying these parameters:

- `width`: Width of the map
- `height`: Height of the map
- `startPlants`: Number of plants at the start
- `plantEnergy`: Energy given by plants
- `plantsPerDay`: Number of plants that grow each day
- `plantGrowth`: Plant growth variant
- `startAnimals`: Number of animals at the start
- `startEnergy`: Initial energy of animals
- `energyToBeFed`: Energy required for animals to reproduce
- `energyUsedToBreed`: Energy used by animals to breed
- `minMutations`: Minimum number of mutations
- `maxMutations`: Maximum number of mutations
- `genomeLength`: Length of the animal genome
- `animalBehaviour`: Animal behavior variant

You can also import and export these settings to a file.

### Pausing, Resuming and changing speed of the Simulation

You can pause and resume the simulation using the `pause` and `resume` buttons

You can also change the speed of the simulation using the `speed` slider.

### Statistics

You can also see the statistics of the simulation in form of numbers on the left side, or in form of a graph on the bottom of the window.  
These statistics include:
- Number of all animals
- Number of all plants
- Number of free fields
- Average energy of all animals
- Average lifespan
- Average number of children
- Top 5 most popular genotypes with number of animals with that genotype

#### Exporting Statistics

You can export the statistics to a CSV file using the `export` button.

### Tracking a specific animal

You can track a specific animal by clicking on it on a map. After `clicking on a place on a map`, a list of animals on that position will appear on the right side of a window. Then you can select which animal you want to track.  
Tracking the animal will show you the following information:
- Animal's ID
- Genotype
- Which Genome is active
- Energy
- Number of eaten plants
- Number of children
- Number of descendants
- Lifespan
- Day of Death (if the animal is dead)

## Authors

- **Łukasz Grabowski** - [GrabowskiDev](https://github.com/GrabowskiDev)
- **Bartosz Wójcik** - [TheVTV](https://github.com/TheVTV)

### Work Distribution

#### Łukasz Grabowski
- Animal
- Genes
- Vector2d
- MapDirection
- Plant
- WorldMap
- Movement around the globe
- Equator variant
- Simulation including removing dead animals, movement, eating plants, reproducing, growing new plants
- RandomIntGenerator and RandomUniquePositionsGenerator
- Animals behaviour variants
- All tests

#### Bartosz Wójcik
- Whole GUI
- SimulationApp
- Observers
- Running multiple simulations and displaying them
- Statistics with all functionality
- Exporting statistics to CSV
- Tracking animals
- Resources such as images

#### Both
- GoodHarvest variant (with separation of Jungle and Equator in all parts of the simulation)
