package edu.nd.dsg.wiki.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashSet;
import java.util.LinkedList;

public class DiscPath {

    private static Logger logger = LogManager.getLogger(DiscPath.class.getName());


    private int srcNode;
    private int destNode;

    private LinkedHashSet<DiscPathElement> paths;

    public DiscPath(int src, int dest){
        srcNode = src;
        destNode = dest;
        paths = new LinkedHashSet<DiscPathElement>();
    }

    public boolean add(LinkedList<Integer> path, float probability){
        return paths.add(new DiscPathElement(path, probability));
    }

    public DiscPathElement mostDiscPath(){
        DiscPathElement mostdp = null;
        float probability = 100;
        for(DiscPathElement discPathElement : paths) {

            if (discPathElement.getProbability() < probability){
                mostdp = discPathElement;
            }

        }
        return mostdp;
    }

    public DiscPathElement mostSimilarPath(){
        DiscPathElement mostsp = null;
        float probability = 0;
        for(DiscPathElement discPathElement : paths) {

            if(discPathElement.getProbability() > probability) {
                mostsp = discPathElement;
            }
        }
        return mostsp;
    }

    public String getSimilarPath(){
        return getPath("similar");
    }

    private String getPath(String type){
        DiscPathElement discPathElement = type.equals("similar") ? mostSimilarPath() : mostDiscPath();
        String rtnStr = "";
        try{
            rtnStr = "{" +
                    "src:"+srcNode+"," +
                    "dest:"+destNode+"," +
                    "path:"+discPathElement.getPath()+"," +
                    "probability:"+discPathElement.getProbability() +
                    "}";
        }catch (NullPointerException e){ //TODO: Fix this exception
            logger.warn("There is an error when trying to get disc path from "+srcNode+"->"+destNode);
        }

        return rtnStr;
    }

    public String getDiscPath(){
        return getPath("disc");
    }

    @Override
    public String toString(){
        return "{" +
                "src:"+srcNode+"," +
                "dest:"+destNode+"," +
                "paths:"+paths.toString()+
                "}";
    }



}