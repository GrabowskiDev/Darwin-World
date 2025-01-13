package agh.ics.darwin.model;


import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.MapVariant;
import agh.ics.darwin.model.variants.MutationVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;

//wysokość i szerokość mapy,
//wariant mapy (wyjaśnione w sekcji poniżej),
//startowa liczba roślin,
//energia zapewniana przez zjedzenie jednej rośliny,
//liczba roślin wyrastająca każdego dnia,
//wariant wzrostu roślin (wyjaśnione w sekcji poniżej),
//startowa liczba zwierzaków,
//startowa energia zwierzaków,
//energia konieczna, by uznać zwierzaka za najedzonego (i gotowego do rozmnażania),
//energia rodziców zużywana by stworzyć potomka,
//minimalna i maksymalna liczba mutacji u potomków (może być równa 0),
//wariant mutacji (wyjaśnione w sekcji poniżej),
//długość genomu zwierzaków,
//wariant zachowania zwierzaków (wyjaśnione w sekcji poniżej).
public record Parameters(
        int width,                         //Wysokość mapy
        int height,                        //Szerokość mapy
        MapVariant mapType,                //Wariant mapy
        int startPlants,                   //Startowa liczba roślin
        int plantEnergy,                   //Energia zapewniana przez zjedzenie jednej rośliny
        int plantsPerDay,                  //Liczba roślin wyrastająca każdego dnia
        PlantGrowthVariant plantGrowth,    //Wariant wzrostu roślin
        int startAnimals,                  //Startowa liczba zwierzaków
        int startEnergy,                   //Startowa energia zwierzaków
        int energyToBeFed,                 //Energia konieczna by zwierzak był najedzony (i gotowy do rozmnażania)
        int energyUsedToBreed,             //Energia rodziców zużywana by stworzyć potomka
        int minMutations,                  //Minimalna liczba mutacji u potomków
        int maxMutations,                  //Maksymalna liczba mutacji u potomków
        MutationVariant mutationType,      //Wariant mutacji
        int genomeLength,                  //Długość genomu zwierzaków
        BehaviourVariant animalBehaviour   //Wariant zachowania zwierzaków
        ) {
    public Parameters {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        if (startPlants < 0 || plantEnergy < 0 || plantsPerDay < 0) {
            throw new IllegalArgumentException("Plant parameters must be non-negative");
        }
        if (startAnimals < 0 || startEnergy < 0 || energyToBeFed < 0 || energyUsedToBreed < 0) {
            throw new IllegalArgumentException("Animal parameters must be non-negative");
        }
        if (minMutations < 0 || maxMutations < 0 || minMutations > maxMutations) {
            throw new IllegalArgumentException("Mutation parameters must be non-negative and min must be less than max");
        }
        if (maxMutations > genomeLength) {
            throw new IllegalArgumentException("Max mutations must be less than genome length");
        }
        if (genomeLength < 0) {
            throw new IllegalArgumentException("Genome length must be non-negative");
        }
        if (energyUsedToBreed > energyToBeFed) {
            throw new IllegalArgumentException("Energy used to breed must be less than energy to be fed");
        }
    }
}
