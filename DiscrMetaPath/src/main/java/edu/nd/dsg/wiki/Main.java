package edu.nd.dsg.wiki;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
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
        }
        System.out.println("Usage:");
        System.out.println("Generate paths: -GEN [-NoSQL cache types first to speedup] [-all get all paths instead of two]");
        System.out.println("Translate paths: -TRANS [-nd do not get most discri/similar paths] [-oNum get NUM paths in between discri&similar paths]");
        System.exit(0);
    }
}
