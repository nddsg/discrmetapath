package edu.nd.dsg.wiki.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

public class PatPathSet {
    protected static PatTypeFinderSQL typeFinder;
    protected static Logger logger = LogManager.getLogger(PatPathSet.class.getName());

    protected int src, dest;

    protected HashSet<PatPath> pathSet;
    protected HashSet<PatPath> siblingSet;
    protected HashSet<String> siblingTypeVector;
    protected LinkedList<HashSet<String>> siblingOrderedTypeVector;

    protected boolean useSQLQuery = true;

    protected LinkedList<PatPath> pathList = null;

    protected LinkedList<PatPath> opathList = null;

    public PatPathSet(int src, int dest) {
        this.src = src;
        this.dest = dest;
        pathSet = new HashSet<PatPath>();
        siblingSet = new HashSet<PatPath>();
        siblingTypeVector = new HashSet<String>();
        siblingOrderedTypeVector = new LinkedList<HashSet<String>>();
    }

    public PatPathSet(int src, int dest, boolean useSQLQuery) {
        this.useSQLQuery = useSQLQuery;
        this.src = src;
        this.dest = dest;
        pathSet = new HashSet<PatPath>();
        siblingSet = new HashSet<PatPath>();
        siblingTypeVector = new HashSet<String>();
        siblingOrderedTypeVector = new LinkedList<HashSet<String>>();
    }

    public PatPathSet(String pathStr) {
        String[] pathArray = pathStr.split("->");
        this.src = Integer.parseInt(pathArray[0]);
        this.dest = Integer.parseInt(pathArray[1]);
        pathSet = new HashSet<PatPath>();
        siblingSet = new HashSet<PatPath>();
        siblingTypeVector = new HashSet<String>();
        siblingOrderedTypeVector = new LinkedList<HashSet<String>>();
    }

    public PatPathSet(String pathStr, boolean useSQLQuery){
        this.useSQLQuery = useSQLQuery;
        String[] pathArray = pathStr.split("->");
        this.src = Integer.parseInt(pathArray[0]);
        this.dest = Integer.parseInt(pathArray[1]);
        pathSet = new HashSet<PatPath>();
        siblingSet = new HashSet<PatPath>();
        siblingTypeVector = new HashSet<String>();
        siblingOrderedTypeVector = new LinkedList<HashSet<String>>();
    }

    public boolean putPath(String pathStr) {
        PatPath PatPath = new PatPath(src, dest, true, useSQLQuery);
        PatPath.putPath(pathStr);
        return pathSet.add(PatPath);
    }

    public boolean putSibling(String pathStr) {
        String[] pathArray = pathStr.split("->");
        int src = Integer.parseInt(pathArray[0]);
        int dest = Integer.parseInt(pathArray[pathArray.length - 1]);
        PatPath PatPath = new PatPath(src, dest, true, useSQLQuery);
        PatPath.putPath(pathStr, siblingOrderedTypeVector);
        siblingTypeVector.addAll(PatPath.getOverallTypeVector());
        while (siblingOrderedTypeVector.size() < pathArray.length) {
            siblingOrderedTypeVector.add(new HashSet<String>());
        }
        for (int i = 0; i < pathArray.length; i++) {
            if(PatPath.getOrderedTypeVector(i) == null){
                break;
            }
            siblingOrderedTypeVector.get(i).addAll(PatPath.getOrderedTypeVector(i));
        }
        return siblingSet.add(PatPath);
    }

    protected void calculateSiblingTypeVector() {
        if (siblingTypeVector.size() == 0) {
            for (PatPath sibling : siblingSet) {
                siblingTypeVector.addAll(sibling.getOverallTypeVector());
            }
        }
    }

    protected double getDiscriminativeRate(PatPath targetPath) {
        HashSet<String> targetVector = targetPath.getOverallTypeVector();
        double intersection = 0;
        for (String type : targetVector) {
            if (siblingTypeVector.contains(type)) {
                intersection++;
            }
        }
        return intersection / (double)siblingTypeVector.size();
    }

    protected double getDiscriminativeRate(HashSet<Integer> targetVector) {
        double intersection = 0;
        for (int type : targetVector) {
            if (siblingTypeVector.contains(type)) {
                intersection++;
            }
        }
        return intersection / (double)siblingTypeVector.size();
    }

    protected double getDiscriminativeRateByOrder(PatPath targetPath) {
        double intersection = 1;
        int iterSize = targetPath.size() < siblingOrderedTypeVector.size() ? targetPath.size() : siblingOrderedTypeVector.size();
        for (int i = 0; i < iterSize; i++) {
            //If path is L1 and it is longer than siblings L2, we only calculate first L2 position.
            double tmp = 0;
            for (String type : targetPath.getOrderedTypeVector(i)) {
                if (siblingOrderedTypeVector.get(i).contains(type)) {
                    tmp++;
                }
            }
            intersection *= tmp / (double)siblingOrderedTypeVector.get(i).size();
        }
        return intersection;
    }

    protected void calculateIntersectionRate() {

        if(pathList == null){
            pathList = new LinkedList<PatPath>();
        }

        calculateSiblingTypeVector();
        for (PatPath p : pathSet) {
            double r = getDiscriminativeRate(p);
            p.setDiscRatio(r);
            pathList.add(p);
        }

        Collections.sort(pathList, new Comparator<PatPath>() {
            @Override
            public int compare(PatPath o1, PatPath o2) {
                if (o1.getDiscRatio() > o2.getDiscRatio()) {
                    return 1;
                } else if (o1.getDiscRatio() == o2.getDiscRatio()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });


    }

    protected void calculateIntersectionRateByOrder() {

        if(opathList == null){
            opathList = new LinkedList<PatPath>();
        }

        calculateSiblingTypeVector();
        for (PatPath p : pathSet) {
            double r = getDiscriminativeRateByOrder(p);
            p.setDiscoRatio(r);
            opathList.add(p);
        }

        Collections.sort(opathList, new Comparator<PatPath>() {
            @Override
            public int compare(PatPath o1, PatPath o2) {
                if(o1.getDiscoRatio() > o2.getDiscoRatio()){
                    return 1;
                }else if(o1.getDiscoRatio() == o2.getDiscoRatio()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });

    }

    public LinkedList<PatPath> getAllNonOrderedPath(){
        if(pathList == null){
            calculateIntersectionRate();
        }
        return pathList;
    }

    public LinkedList<PatPath> getAllOrderedPath(){
        if(opathList == null){
            calculateIntersectionRateByOrder();
        }
        return opathList;
    }

    public PatPath getDiscriminativePath() {
        if (pathList == null) {
            calculateIntersectionRate();
        }
        if(pathList.size() == 0){
            return null;
        }
        return pathList.getFirst();
    }

    public PatPath getDiscriminativePathByOrder() {
        if (opathList == null) {
            calculateIntersectionRateByOrder();
        }
        if(opathList.size() == 0){
            return null;
        }
        return opathList.getFirst();
    }

    public PatPath getSimilarPathByOrder() {
        if (opathList == null) {
            calculateIntersectionRateByOrder();
        }
        if(opathList.size() == 0){
            return null;
        }
        return opathList.getLast();
    }

    public double getDiscriminativeIntersectionCountByOrder() {
        if (opathList == null) {
            calculateIntersectionRateByOrder();
        }
        if(opathList.size() == 0){
            return -1;
        }
        return opathList.getFirst().getDiscoRatio();
    }

    public double getSimilarIntersectionCountByOrder() {
        if (opathList == null) {
            calculateIntersectionRateByOrder();
        }
        if(opathList.size() == 0){
            return -1;
        }
        return opathList.getLast().getDiscoRatio();
    }

    public PatPath getSimilarPath() {
        if (pathList == null) {
            calculateIntersectionRate();
        }
        if(pathList.size() == 0){
            return null;
        }
        return pathList.getLast();
    }

    public double getDiscriminativeIntersectionCount() {
        if (pathList == null) {
            calculateIntersectionRate();
        }
        if(pathList.size() == 0){
            return -1;
        }
        return pathList.getFirst().getDiscRatio();
    }

    public double getSimilarIntersectionCount() {
        if (pathList == null) {
            calculateIntersectionRate();
        }
        if(pathList.size() == 0){
            return -1;
        }
        return pathList.getLast().getDiscRatio();
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
