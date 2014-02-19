package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.IDFCalculator;
import edu.nd.dsg.wiki.util.IDFOkapiCalculator;

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
        System.out.println("Generate Term frequency: -TERM [-BuildWikiTF generate term frequency] [-BuildPatentTF generate term frequency] [-BuildWikiDF generate document frequency] [-BuildPatentDF generate document frequency]");
        System.out.println("Generate Cos distance frequency: -COS [-p build patent]");
        System.out.println("Generate BM25 score: -BM [-ACC accumulate (x,y),(x+y,z),...] [-NODE (x,y),(y,z),...]");
        System.out.println("Caclulate cosine similarity: -COS");
        System.exit(0);
    }
}
