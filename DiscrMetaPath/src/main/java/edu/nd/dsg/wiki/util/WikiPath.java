package edu.nd.dsg.wiki.util;


import edu.nd.dsg.util.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class WikiPath extends Path {

    private static final String SRC_DEST_SPLITTER = "-...->";
    private static final String PATH_SPLITTER = "->";
    private boolean generateOrderedType = true;

    private static final Logger logger = LogManager.getLogger(WikiPath.class.getName());
    private static TypeFinderSQL typeFinder = TypeFinderSQL.getInstance();

    private int src, dest;
    private LinkedList<Integer> path;
    private LinkedList<HashSet<Integer>> orderedTypeVector;
    private HashSet<Integer> typeVector;

    private void illegalInit(){
        src = -1;
        dest = -1;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<Integer>>();
        typeVector = new HashSet<Integer>();
    }

    public WikiPath(int src, int dest, boolean generateOrderedType){
        this.generateOrderedType = generateOrderedType;
        this.src = src;
        this.dest = dest;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<Integer>>();
        typeVector = new HashSet<Integer>();
    }

    public WikiPath(int src, int dest){
        this.src = src;
        this.dest = dest;
        path = new LinkedList<Integer>();
        orderedTypeVector = new LinkedList<HashSet<Integer>>();
        typeVector = new HashSet<Integer>();
    }

    public WikiPath(String pathStr){
        if(pathStr.split(SRC_DEST_SPLITTER).length != 2){
            logger.warn("Got illegal path initialize string " + pathStr);
            illegalInit();
        }else{
            src = Integer.parseInt(pathStr.split(SRC_DEST_SPLITTER)[0]);
            dest = Integer.parseInt(pathStr.split(SRC_DEST_SPLITTER)[1]);
            path = new LinkedList<Integer>();
            orderedTypeVector = new LinkedList<HashSet<Integer>>();
            typeVector = new HashSet<Integer>();
        }
    }

    public boolean putPath(String pathStr){
        return putPath(pathStr, null);
    }

    public boolean putPath(String pathStr, LinkedList<HashSet<Integer>> ignoreSetList){
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

        return true;
    }

    private LinkedList<Integer> constructPath(ArrayList<String> pathArray){
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

    private LinkedList<HashSet<Integer>> constructOrderedTypeVector(ArrayList<String> pathArray, LinkedList<HashSet<Integer>> ignoreSetList){

        LinkedList<HashSet<Integer>> orderedTypedVector = new LinkedList<HashSet<Integer>>();
        Iterator<HashSet<Integer>> ignoreSetIter = null;
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

    private HashSet<Integer> constructOverallTypeVector(LinkedList<HashSet<Integer>> orderedTypeVector){
        HashSet<Integer> overallTypeVector = new HashSet<Integer>();
        for(HashSet<Integer> typeVector : orderedTypeVector){
            overallTypeVector.addAll(typeVector);
        }
        return overallTypeVector;
    }

    private HashSet<Integer> constructOverallTypeVectorWithoutOrderedTypeVector(ArrayList<String> pathArray){
        HashSet<Integer> overallTypeVector = new HashSet<Integer>();

        for(String node : pathArray){
            overallTypeVector.addAll(typeFinder.getTypeVector(node, overallTypeVector));
        }
        return overallTypeVector;
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

    public HashSet<Integer> getOrderedTypeVector(int pos){
        return orderedTypeVector.get(pos);
    }

    public HashSet<Integer> getOverallTypeVector(){
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

}
