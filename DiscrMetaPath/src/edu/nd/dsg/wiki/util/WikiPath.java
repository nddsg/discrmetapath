package edu.nd.dsg.wiki.util;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WikiPath {

    private static Logger logger = LogManager.getLogger(WikiPath.class.getName());

    private HashMap<Integer, LinkedList<LinkedList<Integer>>> pathList;
    //Key: length Value: LinkedList(without src&dest nodes)

    private HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> pathProb;
    //Key: length Value: {key: Position, Value: HashMap{Key: node, Value: Count}}

    /**
     *
     * @param path Path string, format is "[0-9\-\>]+"
     * @return boolean If successfully inserted, then return true, if not an valid path then return false.
     */
    public boolean put(String path){

        logger.trace("Put new path "+path);

        int pathLen = path.split("->").length - 2;
        if(pathLen < 0){ // illegal path
            logger.warn("Path length < 0, path is "+ path);
            return false;
        }

        if(!pathList.containsKey(pathLen)){
            pathList.put(pathLen, new LinkedList<LinkedList<Integer>>());
        }
        if(!pathProb.containsKey(pathLen)){
            pathProb.put(pathLen, new HashMap<Integer, HashMap<Integer, Integer>>());
        }
        if(pathLen == 0) {
            pathList.get(pathLen).add(new LinkedList<Integer>());
            return true;
        }

        String[] strPath = path.split("->");
        pathList.get(pathLen).add(new LinkedList<Integer>());
        for(int i = 1; i < strPath.length - 1; i++){
            pathList.get(pathLen).get(pathList.get(pathLen).size()-1).add(Integer.parseInt(strPath[i]));
            if(!pathProb.get(pathLen).containsKey(i)){
                pathProb.get(pathLen).put(i, new HashMap<Integer, Integer>());
            }
            int count = 1;
            if(pathProb.get(pathLen).get(i).containsKey(Integer.parseInt(strPath[i]))){
                count += pathProb.get(pathLen).get(i).get(Integer.parseInt(strPath[i]));
            }

            pathProb.get(pathLen).get(i).put(Integer.parseInt(strPath[i]), count);

            if(!pathProb.get(pathLen).get(i).containsKey(0)){
                pathProb.get(pathLen).get(i).put(0,0);
            }

            pathProb.get(pathLen).get(i).put(0, pathProb.get(pathLen).get(i).get(0)+1);

        }

        logger.trace("PathList result: "+ pathList.toString());
        logger.trace("PathProb result: "+ pathProb.toString());

        return true;

    }

    /**
     *
     * @param key String, format is "srcNode->destNode"
     * @param srcPath WikiPath Object, which contain path candidates
     * @return DiscPath Object, containing discriminative path list and its corresponding similarity probability.
     */
    public DiscPath findDiscriminativePath(WikiPath srcPath, String key){
        HashMap<Integer, LinkedList<LinkedList<Integer>>> srcPathList = srcPath.getPathList();

        int src,dest;
        src = Integer.parseInt(key.split("->")[0]);
        dest = Integer.parseInt(key.split("->")[1]);

        DiscPath discPath = new DiscPath(src, dest);
        for(int len : srcPathList.keySet()){
            HashMap<Integer, HashMap<Integer, Integer>> prob = pathProb.get(len);
            for(LinkedList<Integer> path : srcPathList.get(len)){
                Integer[] pathArry = path.toArray(new Integer[10]);
                float sumPathProb = 0;
                for(int i = 0 ; i < pathArry.length; i++){
                    //calculate probability
                    if((prob != null) && prob.containsKey(i + 1) && prob.get(i + 1).containsKey(pathArry[i])){
                        logger.debug("path "+path+" "+prob.get(i+1).get(pathArry[i]) + "/" + prob.get(i+1).get(0));
                        sumPathProb += (float)prob.get(i+1).get(pathArry[i]) / (float)prob.get(i+1).get(0);
                    }
                }

                discPath.add(path, sumPathProb);
                logger.debug("Prob:"+ sumPathProb +", Path:"+path);

            }
        }

        return discPath;
    }

    protected HashMap<Integer, LinkedList<LinkedList<Integer>>> getPathList(){
        return pathList;
    }

    public WikiPath(){

        pathList = new HashMap<Integer, LinkedList<LinkedList<Integer>>>();
        pathProb = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
    }

}

