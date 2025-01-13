package agh.ics.darwin.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class GenesTest {

    @Test
    void nextGene() {
        Genes genes = new Genes(new int[]{0, 1, 2, 3, 4});
        int initialGene = genes.getCurrentGene();
        genes.nextGene();
        genes.nextGene();
        assertEquals((initialGene+2)%5, genes.getCurrentGene());
    }

    @Test
    void nextGeneLooping() {
        Genes genes = new Genes(new int[]{0, 1, 2, 3, 4});
        int initialGene = genes.getCurrentGene();
        genes.nextGene();
        genes.nextGene();
        genes.nextGene();
        genes.nextGene();
        genes.nextGene();
        assertEquals(initialGene, genes.getCurrentGene());
    }

    @Test
    void mutate() {
        Genes genes = new Genes(new int[]{1, 2, 3, 4, 5});
        genes.mutate(0);
        genes.mutate(1);
        genes.mutate(2);
        genes.mutate(3);
        genes.mutate(4);
        assertNotEquals(1, genes.getGenes()[0]);
        assertNotEquals(2, genes.getGenes()[1]);
        assertNotEquals(3, genes.getGenes()[2]);
        assertNotEquals(4, genes.getGenes()[3]);
        assertNotEquals(5, genes.getGenes()[4]);
    }

    @Test
    void testToString() {
        Genes genes = new Genes(new int[]{1, 2, 3, 4, 5});
        assertEquals("[1, 2, 3, 4, 5]", genes.toString());
    }

    @Test
    void getGenes() {
        Genes genes = new Genes(new int[]{1, 2, 3, 4, 5});
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, genes.getGenes());
    }

    @Test
    void childGenesConstructorNoMutations() {
        int[] parent1 = {0, 1, 2, 3};
        int[] parent2 = {4, 5, 6, 7};
        Genes genes = new Genes(parent1, parent2, 2, 2, 0, 0);
        
        int[] expectedGenesSide0 = {0, 1, 6, 7};
        int[] expectedGenesSide1 = {4, 5, 2, 3};

        assertTrue(
                Arrays.equals(expectedGenesSide0, genes.getGenes()) ||
                        Arrays.equals(expectedGenesSide1, genes.getGenes())
        );
    }

    @Test
    void childGenesConstructorDifferentLength() {
        int[] parent1 = {0, 1, 2, 3, 4};
        int[] parent2 = {1, 5, 6, 7, 2};
        Genes genes = new Genes(parent1, parent2, 3, 2, 0, 0);

        int[] expectedGenesSide0 = {0, 1, 2, 7, 2};
        int[] expectedGenesSide1 = {1, 5, 2, 3, 4};

        assertTrue(
                Arrays.equals(expectedGenesSide0, genes.getGenes()) ||
                        Arrays.equals(expectedGenesSide1, genes.getGenes())
        );
    }

    @Test
    void childGenesConstructorMutatingAll() {
        int[] parent1 = {0, 1, 2, 3};
        int[] parent2 = {0, 1, 2, 3};
        Genes genes = new Genes(parent1, parent2, 2, 2, 4, 4);

        int[] notExpectedGenes = {0, 1, 2, 3};
        assertNotEquals(notExpectedGenes, genes.getGenes());

        for (int i = 0; i < 4; i++) {
            assertNotEquals(notExpectedGenes[i], genes.getGenes()[i]);
        }
    }


}