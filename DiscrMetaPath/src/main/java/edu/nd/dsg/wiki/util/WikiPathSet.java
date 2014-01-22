package edu.nd.dsg.wiki.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.LinkedList;

public class WikiPathSet {

    private static Logger logger = LogManager.getLogger(WikiPathSet.class.getName());

    private int src, dest;

    private HashSet<WikiPath> pathSet;
    private HashSet<WikiPath> siblingSet;
    private HashSet<Integer> siblingTypeVector;
    private LinkedList<HashSet<Integer>> siblingOrderedTypeVector;

    private boolean useSQLQuery = true;

    private long drate;
    private long srate;
    private WikiPath dpath = null;
    private WikiPath spath = null;

    private long dorate;
    private long sorate;
    private WikiPath dopath = null;
    private WikiPath sopath = null;

    public WikiPathSet(int src, int dest) {
        this.src = src;
        this.dest = dest;
        pathSet = new HashSet<WikiPath>();
        siblingSet = new HashSet<WikiPath>();
        siblingTypeVector = new HashSet<Integer>();
        siblingOrderedTypeVector = new LinkedList<HashSet<Integer>>();
    }

    public WikiPathSet(int src, int dest, boolean useSQLQuery) {
        this.useSQLQuery = useSQLQuery;
        this.src = src;
        this.dest = dest;
        pathSet = new HashSet<WikiPath>();
        siblingSet = new HashSet<WikiPath>();
        siblingTypeVector = new HashSet<Integer>();
        siblingOrderedTypeVector = new LinkedList<HashSet<Integer>>();
    }

    public WikiPathSet(String pathStr) {
        String[] pathArray = pathStr.split("->");
        this.src = Integer.parseInt(pathArray[0]);
        this.dest = Integer.parseInt(pathArray[1]);
        pathSet = new HashSet<WikiPath>();
        siblingSet = new HashSet<WikiPath>();
        siblingTypeVector = new HashSet<Integer>();
        siblingOrderedTypeVector = new LinkedList<HashSet<Integer>>();
    }

    public WikiPathSet(String pathStr, boolean useSQLQuery){
        this.useSQLQuery = useSQLQuery;
        String[] pathArray = pathStr.split("->");
        this.src = Integer.parseInt(pathArray[0]);
        this.dest = Integer.parseInt(pathArray[1]);
        pathSet = new HashSet<WikiPath>();
        siblingSet = new HashSet<WikiPath>();
        siblingTypeVector = new HashSet<Integer>();
        siblingOrderedTypeVector = new LinkedList<HashSet<Integer>>();
    }

    public boolean putPath(String pathStr) {
        WikiPath wikiPath = new WikiPath(src, dest, true, useSQLQuery);
        wikiPath.putPath(pathStr);
        return pathSet.add(wikiPath);
    }

    public boolean putSibling(String pathStr) {
        String[] pathArray = pathStr.split("->");
        int src = Integer.parseInt(pathArray[0]);
        int dest = Integer.parseInt(pathArray[pathArray.length - 1]);
        WikiPath wikiPath = new WikiPath(src, dest, true, useSQLQuery);
        wikiPath.putPath(pathStr, siblingOrderedTypeVector);
        siblingTypeVector.addAll(wikiPath.getOverallTypeVector());
        while (siblingOrderedTypeVector.size() < pathArray.length) {
            siblingOrderedTypeVector.add(new HashSet<Integer>());
        }
        for (int i = 0; i < pathArray.length; i++) {
            siblingOrderedTypeVector.get(i).addAll(wikiPath.getOrderedTypeVector(i));
        }
        return siblingSet.add(wikiPath);
    }

    private void calculateSiblingTypeVector() {
        if (siblingTypeVector.size() == 0) {
            for (WikiPath sibling : siblingSet) {
                siblingTypeVector.addAll(sibling.getOverallTypeVector());
            }
        }
    }

    private long getDiscriminativeRate(WikiPath targetPath) {
        HashSet<Integer> targetVector = targetPath.getOverallTypeVector();
        long intersection = 0;
        for (int type : targetVector) {
            if (siblingTypeVector.contains(type)) {
                intersection++;
            }
        }
        logger.debug(targetPath.toString() + " rate " + intersection);
        return intersection;
    }

    private long getDiscriminativeRate(HashSet<Integer> targetVector) {
        long intersection = 0;
        for (int type : targetVector) {
            if (siblingTypeVector.contains(type)) {
                intersection++;
            }
        }
        return intersection;
    }

    private long getDiscriminativeRateByOrder(WikiPath targetPath) {
        long intersection = 1;
        int iterSize = targetPath.size() < siblingOrderedTypeVector.size() ? targetPath.size() : siblingOrderedTypeVector.size();
        for (int i = 0; i < iterSize; i++) {
            //If path is L1 and it is longer than siblings L2, we only calculate first L2 position.
            long tmp = 0;
            for (int type : targetPath.getOrderedTypeVector(i)) {
                if (siblingOrderedTypeVector.get(i).contains(type)) {
                    tmp++;
                }
            }
            intersection *= tmp;
        }
        return intersection;
    }

    private void calculateIntersectionRate() {
        calculateSiblingTypeVector();
        drate = Long.MAX_VALUE; // discriminative rate (default 1 means all similar)
        srate = Long.MIN_VALUE; // (default 0 means all different)
        for (WikiPath p : pathSet) {
            long r = getDiscriminativeRate(p);
            if (r < drate) {
                dpath = p;
                drate = r;
            }
            if (r > srate) {
                spath = p;
                srate = r;
            }
        }
    }

    private void calculateIntersectionRateByOrder() {
        calculateSiblingTypeVector();
        dorate = Long.MAX_VALUE;
        sorate = Long.MIN_VALUE;
        for (WikiPath p : pathSet) {
            long r = getDiscriminativeRateByOrder(p);
            if (r < dorate) {
                dopath = p;
                dorate = r;
            }
            if (r > sorate) {
                sopath = p;
                sorate = r;
            }
        }
    }

    public WikiPath getDiscriminativePath() {
        if (dpath == null) {
            calculateIntersectionRate();
        }
        return dpath;
    }

    public WikiPath getDiscriminativePathByOrder() {
        if (dopath == null) {
            calculateIntersectionRateByOrder();
        }
        return dopath;
    }

    public WikiPath getSimilarPathByOrder() {
        if (sopath == null) {
            calculateIntersectionRateByOrder();
        }
        return sopath;
    }

    public long getDiscriminativeRateByOrder() {
        if (dopath == null) {
            calculateIntersectionRateByOrder();
        }
        return dorate;
    }

    public long getSimilarRateByOrder() {
        if (sopath == null) {
            calculateIntersectionRateByOrder();
        }
        return sorate;
    }

    public WikiPath getSimilarPath() {
        if (spath == null) {
            calculateIntersectionRate();
        }
        return spath;
    }

    public long getDiscriminativeRate() {
        if (dpath == null) {
            calculateIntersectionRate();
        }
        return drate;
    }

    public long getSimilarRate() {
        if (spath == null) {
            calculateIntersectionRate();
        }
        return srate;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("pathSet:");
        stringBuilder.append(pathSet.toString());
        stringBuilder.append(" siblingSet:");
        stringBuilder.append(siblingSet.toString());

        return stringBuilder.toString();
    }


}
