package edu.nd.dsg.util;

import java.util.HashSet;
import java.util.LinkedList;

public abstract class Path {

    abstract public int getSrc();
    abstract public int getDest();
    abstract public int getPathLength();
    abstract public boolean putPath(String pathStr);
    abstract public HashSet<Integer> getOverallTypeVector();
    abstract public LinkedList<Integer> getPath();
    abstract public HashSet<Integer> getOrderedTypeVector(int pos);
}
