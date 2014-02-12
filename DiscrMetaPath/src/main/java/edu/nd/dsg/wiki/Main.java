package edu.nd.dsg.wiki;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        for(String arg : args){
            if(arg.startsWith("-GEN")){
                PathGenerator.main(args);
                System.exit(0);
            }
            if(arg.startsWith("-TRANS")){
                PathTranslator.main(args);
                System.exit(0);
            }
            if(arg.startsWith("-TERM")){
                BuildTermFrequencyIndex.main(args);
                System.exit(0);
            }
            if(arg.startsWith("-COS")){
                PathCosSimilarity.main(args);
                System.exit(0);
            }
            if(arg.startsWith("-BM")){
                BM25Distance.main(args);
                System.exit(0);
            }
        }
        System.out.println("Usage:");
        System.out.println("Generate paths: -GEN [-NoSQL cache types first to speedup] [-all get all paths instead of two]");
        System.out.println("Translate paths: -TRANS [-nd do not get most discri/similar paths] [-oNum get NUM paths in between discri&similar paths]");
        System.out.println("Generate Term frequency: -TERM [-BuildTF generate term frequency] [-BuildDF] generate document frequency");
        System.out.println("Caclulate cosine similarity: -COS");
        System.exit(0);
    }
}
