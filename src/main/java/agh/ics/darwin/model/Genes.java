package agh.ics.darwin.model;

public class Genes {
    private final int[] genes; //Values between 0 and 7
    private int index = 0;

    public Genes(int[] genes) {
        this.genes = genes;
        index = 0; //TODO: Random index
    }

    //Todo: ReproductionGenes

    public int getCurrentGene() {
        return genes[index];
    }

    public int nextGene() {
        index = (index + 1) % genes.length;
        return genes[index];
    }

    //TODO: Mutation

}
