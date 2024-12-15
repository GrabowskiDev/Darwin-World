package agh.ics.darwin.model;

import java.util.Random;

public class Genes {
    private final int[] genes; //Values between 0 and 7
    private int index = 0;

    public Genes(int[] genes) {
        for (int gene : genes) {
            if (gene < 0 || gene > 7) {
                throw new IllegalArgumentException("Gene must be between 0 and 7");
            }
        }

        this.genes = genes;
        this.index = new Random().nextInt(genes.length);
    }

    //It creates a new Genes object by combining two parents' genes
    //len1 is number of genes from parent1, and len2 is number of genes from right parent
    public Genes(int[] parent1, int[] parent2, int len1, int len2) {
        this.genes = new int[len1 + len2];
        int side = new Random().nextInt(2);
        //First parent on left side
        if ((side == 0 && len1 >= len2) || (side == 1 && len1 < len2)) {
            System.arraycopy(parent1, 0, genes, 0, len1);
            System.arraycopy(parent2, len1, genes, len1, len2);
        } else {
            System.arraycopy(parent2, 0, genes, 0, len2);
            System.arraycopy(parent1, len2, genes, len2, len1);
        }

        //Mutating random number of genes
        int genesToMutate = new Random().nextInt(genes.length+1);
        RandomIntGenerator randomIntGenerator = new RandomIntGenerator(genes.length);
        for (int i=0; i<genesToMutate; i++) {
            if (randomIntGenerator.iterator().hasNext()) {
                int geneIndex = randomIntGenerator.iterator().next();
                mutate(geneIndex);
            }
        }
    }

    public int getCurrentGene() {
        return genes[index];
    }

    public void nextGene() {
        index = (index + 1) % genes.length;
    }

    public void mutate(int i) {
        genes[i] = new Random().nextInt(8);
    }

    @Override
    public String toString() {
        return "Genes: " + java.util.Arrays.toString(genes);
    }

    public int[] getGenes() {
        return genes;
    }
}
