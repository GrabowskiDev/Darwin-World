package agh.ics.darwin.model;

import org.junit.jupiter.api.Test;

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
}