package agh.ics.darwin;

import agh.ics.darwin.model.Genes;

public class World {
    public static void main(String[] args) {
        System.out.println("Hello, World!");

        Genes genes1 = new Genes(new int[]{1, 2, 2, 1, 5, 6, 7});
        Genes genes2 = new Genes(new int[]{0, 4, 2, 6, 3, 3, 1});

        Genes genes3 = new Genes(genes1.getGenes(), genes2.getGenes(), 3, 4);
        System.out.println(genes1);
        System.out.println(genes2);
        System.out.println(genes3);

    }
}
