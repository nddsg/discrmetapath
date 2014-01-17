package edu.nd.dsg.wiki.util;

import java.util.LinkedList;

public class DiscPathElement {
    private LinkedList<Integer> path;
    private float probability;

    public LinkedList<Integer> getPath() {
        return path;
    }

    public float getProbability() {
        return probability;
    }

    public DiscPathElement(LinkedList<Integer> path, float probability){
        this.path = path;
        this.probability = probability;
    }

    @Override
    public String toString(){
        return "{" +
                "path:"+path+"," +
                "prob:"+probability +
                "}";
    }

}