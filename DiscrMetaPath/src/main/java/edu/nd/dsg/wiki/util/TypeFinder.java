package edu.nd.dsg.wiki.util;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class TypeFinder {

    /**
     * Generate type(category) vector for node
     * @param node start node, this function will find out all categories that connected to **node**
     * @return LinkedHashSet containing all categories that belongs to **node**
     */
    abstract public LinkedHashSet<Integer> getTypeVector(int node);

    abstract public LinkedHashSet<Integer> getTypeVector(String node);

    abstract public LinkedHashSet<Integer> getTypeVector(String node, Set<Integer> ignoreSet);

    /**
     * Generate type(category) vector for node
     * @param node start node, this function will find out all categories that connected to **node**
     * @param ignoreSet HashSet that containing all types(categories) that could ignore.
     *                  Usually used for speed up by ignoring duplicate categories.
     * @return LinkedHashSet containing all categories that belongs to **node**
     */
    abstract public LinkedHashSet<Integer> getTypeVector(int node, Set<Integer> ignoreSet);
}
