package edu.nd.dsg.wiki.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public  abstract interface TFCalculator {
    public abstract LinkedList<Double> getaccumulatedTF(LinkedList<Integer> path);
    public abstract HashMap<Integer, HashMap<String, Integer>> getTermFreqMap(HashSet<Integer> nodeSet);
    public abstract double getTF(LinkedList<Integer> x, LinkedList<Integer> y);
}
