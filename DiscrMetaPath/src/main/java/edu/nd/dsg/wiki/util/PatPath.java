package edu.nd.dsg.wiki.util;

import edu.nd.dsg.util.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PatPath {

    protected static PatTypeFinderSQL typeFinder = PatTypeFinderSQL.getInstance();

    protected static final String SRC_DEST_SPLITTER = "-...->";
    protected static final String PATH_SPLITTER = "->";
    protected boolean generateOrderedType = true;
    protected double discoRatio = 0;
    protected LinkedList<Integer> path;
    protected LinkedList<HashSet<String>> orderedTypeVector;
    protected HashSet<String> typeVector;

    protected static final Logger logger = LogManager.getLogger(WikiPath.class.getName());

    protected int src, dest;

    public double getDiscRatio() {
        return discRatio;
    }

    public void setDiscRatio(double discRatio) {
        this.discRatio = discRatio;
    }

    protected double discRatio = 0;

    public double getDiscoRatio() {
        return discoRatio;
    }

    public void setDiscoRatio(double discoRatio) {
        this.discoRatio = discoRatio;
    }

    protected void illegalInit(){
        src = -1;
        dest = -1;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<String>>();
        typeVector = new HashSet<String>();
    }

    public PatPath(int src, int dest, boolean generateOrderedType, boolean useMySQL){
        this.generateOrderedType = generateOrderedType;
        this.src = src;
        this.dest = dest;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<String>>();
        typeVector = new HashSet<String>();
    }

    public PatPath(int src, int dest, boolean generateOrderedType){
        this.generateOrderedType = generateOrderedType;
        this.src = src;
        this.dest = dest;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<String>>();
        typeVector = new HashSet<String>();
    }

    public PatPath(int src, int dest){
        this.src = src;
        this.dest = dest;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<String>>();
        typeVector = new HashSet<String>();
    }

    public PatPath(String pathStr){
        if(pathStr.split(SRC_DEST_SPLITTER).length != 2){
            logger.warn("Got illegal path initialize string " + pathStr);
            illegalInit();
        }else{
            src = Integer.parseInt(pathStr.split(SRC_DEST_SPLITTER)[0]);
            dest = Integer.parseInt(pathStr.split(SRC_DEST_SPLITTER)[1]);
            path = new LinkedList<Integer>();
            orderedTypeVector = new LinkedList<HashSet<String>>();
            typeVector = new HashSet<String>();
        }
    }

    public boolean putPath(String pathStr){
        return putPath(pathStr, null);
    }

    public boolean putPath(String pathStr, LinkedList<HashSet<String>> ignoreSetList){
        ArrayList<String> pathArray = new ArrayList<String>(Arrays.asList(pathStr.split(PATH_SPLITTER)));
        if(pathArray.get(pathArray.size()-1).equals("")){
            pathArray.remove(pathArray.size()-1);
        }
        if(pathArray.size() < 2){
            logger.warn("Got illegal path string " + pathStr);
            return false;
        }

        path = constructPath(pathArray);
        if(path == null) {
            return false;
        }

        if(generateOrderedType){
            orderedTypeVector = constructOrderedTypeVector(pathArray, ignoreSetList);
            if(orderedTypeVector == null) {
                return false;
            }

            typeVector = constructOverallTypeVector(orderedTypeVector);
            if(typeVector == null) {
                return false;
            }
        }else{
            orderedTypeVector = null;
            typeVector = constructOverallTypeVectorWithoutOrderedTypeVector(pathArray);
        }

//        System.out.println()

        return true;
    }

    protected LinkedList<Integer> constructPath(ArrayList<String> pathArray){
        LinkedList<Integer> path = new LinkedList<Integer>();
        try{
            for(String node : pathArray) {
                path.add(Integer.parseInt(node));
            }
        }catch (Exception e){
            path = null;
        }

        return path;
    }


    public int getSrc(){
        return src;
    }

    public int getDest(){
        return dest;
    }

    public int getPathLength(){
        return path.size();
    }

    public LinkedList<Integer> getPath(){
        return path;
    }

    public HashSet<String> getOrderedTypeVector(int pos){
        if(orderedTypeVector.size()<pos+1){
            return null;
        }
        return orderedTypeVector.get(pos);
    }

    public HashSet<String> getOverallTypeVector(){
        return typeVector;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();;
        stringBuilder.append(path);
        stringBuilder.append(" typeVector size:");
        stringBuilder.append(typeVector.size());
        return stringBuilder.toString();
    }

    public int size(){
        return path.size();
    }

    protected LinkedList<HashSet<String>> constructOrderedTypeVector(ArrayList<String> pathArray, LinkedList<HashSet<String>> ignoreSetList){

        LinkedList<HashSet<String>> orderedTypedVector = new LinkedList<HashSet<String>>();
        Iterator<HashSet<String>> ignoreSetIter = null;
        if (ignoreSetList!=null && ignoreSetList.size() >= pathArray.size()) {
            ignoreSetIter = ignoreSetList.iterator();
        }
        for(String node : pathArray){
            if(ignoreSetIter!=null){
                orderedTypedVector.add(typeFinder.getTypeVector(node, ignoreSetIter.next()));
            }else{
                orderedTypedVector.add(typeFinder.getTypeVector(node));
            }
        }
        return orderedTypedVector;
    }

    protected HashSet<String> constructOverallTypeVector(LinkedList<HashSet<String>> orderedTypeVector){
        HashSet<String> overallTypeVector = new HashSet<String>();
        for(HashSet<String> typeVector : orderedTypeVector){
            overallTypeVector.addAll(typeVector);
        }
        return overallTypeVector;
    }

    protected HashSet<String> constructOverallTypeVectorWithoutOrderedTypeVector(ArrayList<String> pathArray){
        HashSet<String> overallTypeVector = new HashSet<String>();

        for(String node : pathArray){
            overallTypeVector.addAll(typeFinder.getTypeVector(node, overallTypeVector));
        }
        return overallTypeVector;
    }

}
