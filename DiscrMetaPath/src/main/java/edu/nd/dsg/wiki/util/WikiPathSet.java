package edu.nd.dsg.wiki.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
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

    private LinkedList<WikiPath> pathList = null;

    private LinkedList<WikiPath> opathList = null;

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
            if(wikiPath.getOrderedTypeVector(i) == null){
                break;
            }
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

    private double getDiscriminativeRate(WikiPath targetPath) {
        HashSet<Integer> targetVector = targetPath.getOverallTypeVector();
        double intersection = 0;
        for (int type : targetVector) {
            if (siblingTypeVector.contains(type)) {
                intersection++;
            }
        }
        logger.debug(targetPath.toString() + " rate " + intersection);
        return intersection / (double)siblingTypeVector.size();
    }

    private double getDiscriminativeRate(HashSet<Integer> targetVector) {
        double intersection = 0;
        for (int type : targetVector) {
            if (siblingTypeVector.contains(type)) {
                intersection++;
            }
        }
        return intersection / (double)siblingTypeVector.size();
    }

    private double getDiscriminativeRateByOrder(WikiPath targetPath) {
        double intersection = 1;
        int iterSize = targetPath.size() < siblingOrderedTypeVector.size() ? targetPath.size() : siblingOrderedTypeVector.size();
        for (int i = 0; i < iterSize; i++) {
            //If path is L1 and it is longer than siblings L2, we only calculate first L2 position.
            double tmp = 0;
            for (int type : targetPath.getOrderedTypeVector(i)) {
                if (siblingOrderedTypeVector.get(i).contains(type)) {
                    tmp++;
                }
            }
            intersection *= tmp / (double)siblingOrderedTypeVector.get(i).size();
        }
        return intersection;
    }

    private void calculateIntersectionRate() {

        if(pathList == null){
            pathList = new LinkedList<WikiPath>();
        }

        calculateSiblingTypeVector();
        for (WikiPath p : pathSet) {
            double r = getDiscriminativeRate(p);
            p.setDiscRatio(r);
            pathList.add(p);
        }

        Collections.sort(pathList, new Comparator<WikiPath>() {
            @Override
            public int compare(WikiPath o1, WikiPath o2) {
                if(o1.getDiscRatio() > o2.getDiscRatio()){
                    return 1;
                }else if(o1.getDiscRatio() == o2.getDiscRatio()){
                    return 0;
                }else{
                    return -1;
                }
            }
        });


    }

    private void calculateIntersectionRateByOrder() {

        if(opathList == null){
            opathList = new LinkedList<WikiPath>();
        }

        calculateSiblingTypeVector();
        for (WikiPath p : pathSet) {
            double r = getDiscriminativeRateByOrder(p);
            p.setDiscoRatio(r);
            opathList.add(p);
        }

        Collections.sort(opathList, new Comparator<WikiPath>() {
            @Override
            public int compare(WikiPath o1, WikiPath o2) {
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

    public LinkedList<WikiPath> getallNonOrderedPath(){
        if(pathList == null){
            calculateIntersectionRate();
        }
        return pathList;
    }

    public LinkedList<WikiPath> getallOrderedPath(){
        if(opathList == null){
            calculateIntersectionRateByOrder();
        }
        return opathList;
    }

    public WikiPath getDiscriminativePath() {
        if (pathList == null) {
            calculateIntersectionRate();
        }
        if(pathList.size() == 0){
            return null;
        }
        return pathList.getFirst();
    }

    public WikiPath getDiscriminativePathByOrder() {
        if (opathList == null) {
            calculateIntersectionRateByOrder();
        }
        if(opathList.size() == 0){
            return null;
        }
        return opathList.getFirst();
    }

    public WikiPath getSimilarPathByOrder() {
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

    public WikiPath getSimilarPath() {
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
        logger.info(pathList.getFirst().getDiscRatio());
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
