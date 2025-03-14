package agh.ics.darwin.model;

import java.util.Arrays;
import java.util.Objects;
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
    public Genes(int[] parent1, int[] parent2, int len1, int len2, int minMutations, int maxMutations) {
        if (len1 < 0 || len2 < 0 || len1 > parent1.length || len2 > parent2.length) {
            throw new IllegalArgumentException("Invalid lengths");
        }
        if (minMutations < 0 || maxMutations < 0 || minMutations > maxMutations || maxMutations > len1 + len2) {
            throw new IllegalArgumentException("Invalid mutation range");
        }

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

        //Mutating genes in range [minMutations, maxMutations]
        int genesToMutate = new Random().nextInt(maxMutations - minMutations + 1) + minMutations;
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

    public void selectRandomGene() {
        int[] availableIndexes = new int[genes.length - 1];
        for (int i = 0, j = 0; i < genes.length; i++) {
            if (i != index) {
                availableIndexes[j++] = i;
            }
        }
        index = availableIndexes[new Random().nextInt(availableIndexes.length)];
    }

    public void mutate(int i) {
        RandomIntGenerator randomIntGenerator = new RandomIntGenerator(8);
        while (randomIntGenerator.iterator().hasNext()) {
            int newGene = randomIntGenerator.iterator().next();
            if (newGene != genes[i]) {
                genes[i] = newGene;
                break;
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(genes);
    }

    public int[] getGenes() {
        return genes;
    }

    public int getIdx() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genes genes1 = (Genes) o;
        return Arrays.equals(genes, genes1.genes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(genes);
    }
}
