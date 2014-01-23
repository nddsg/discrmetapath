package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.WikiPath;
import edu.nd.dsg.wiki.util.WikiPathSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class PathGenerator {
    private static Logger logger = LogManager.getLogger(PathGenerator.class.getName());

    public static void main(String[] args){
        boolean useSQL = true;
        boolean allPath = false;
        HashMap<String, LinkedList<String>> siblingPathText;

        for(String arg : args){
            if(arg.startsWith("-NoSQL")){
                useSQL = false;
            }
            if(arg.startsWith("-all")){
                allPath = true;
            }
        }

        siblingPathText = loadSiblingPathText("data/wikiotherpaths.txt");

        loadWikiPath("data/wikipath.txt", useSQL, siblingPathText, allPath);

    }

    protected static void outputResult(WikiPathSet wikiPathSet){

        StringBuilder stringBuilder = new StringBuilder();
        WikiPath wikiPath;
        stringBuilder.append("{discr:{path:");
        wikiPath = wikiPathSet.getDiscriminativePath();
        if(wikiPath!=null){
            stringBuilder.append(wikiPath.getPath());
        }else{
            logger.warn("Can not get Discriminative path for " + wikiPathSet.toString());
            return;
        }
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getDiscriminativeIntersectionCount());
        stringBuilder.append("},simi:{path:");
        stringBuilder.append(wikiPathSet.getSimilarPath().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getSimilarIntersectionCount());
        stringBuilder.append("},discro:{path:");
        stringBuilder.append(wikiPathSet.getDiscriminativePathByOrder().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getDiscriminativeIntersectionCountByOrder());
        stringBuilder.append("},simio:{path:");
        stringBuilder.append(wikiPathSet.getSimilarPathByOrder().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getSimilarIntersectionCountByOrder());
        stringBuilder.append("}}");

        System.out.println(stringBuilder.toString());
    }

    protected static HashMap<String, LinkedList<String>> loadSiblingPathText(String path){
        HashMap<String, LinkedList<String>> siblingPathText = new HashMap<String, LinkedList<String>>();
        int lineCounter = 0;
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            String line;
            String key = "";

            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){
                if(!line.contains("Timeout")&&!line.contains("Too big")){
                    if(line.contains("(")){
                        key = line.split(",")[0].replace("(","")+
                                "->"+
                                line.split("->")[1].replace("(","").replace(")","").replace(" ","");
                        key = key.trim();
                        if(!siblingPathText.containsKey(key)){
                            siblingPathText.put(key, new LinkedList<String>());
                        }
                    }else if(line.contains("...")){
                        //omit
                    }else{
                        siblingPathText.get(key).add(line);
                    }
                    line = bufferedReader.readLine();
                    lineCounter++;

                    if(lineCounter%10000 == 0){
                        logger.info(lineCounter+" lines are loaded");
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return siblingPathText;
    }

    protected static void loadWikiPath(String path, boolean useSQL,
                                       HashMap<String, LinkedList<String>> siblingPathText,
                                       boolean allPath){
        int lineCounter = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            WikiPathSet wikiPathSet = null;
            String line;
            String key="";
            String newkey = "";

            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){

                if(!line.contains("Timeout")&&!line.contains("Too big")){
                    if(line.contains("...")){
                        //new path src&dest

                        newkey = line.replace("...-","").trim();

                        if(!key.equals("") && !key.equals(newkey)){
                            //new path set starts, get all siblings for old path set

                            LinkedList<String> pathList = siblingPathText.get(key);
                            if(pathList!=null){
                                for(String sibling : pathList){
                                    wikiPathSet.putSibling(sibling);
                                }
                                if(allPath){
                                    if(wikiPathSet.getAllNonOrderedPath()!=null){
                                        LinkedList<WikiPath> wikiPathLinkedList = wikiPathSet.getAllNonOrderedPath();
                                        System.out.println("non-order,"+wikiPathLinkedList.size());
                                        for(WikiPath wp : wikiPathLinkedList){
                                            System.out.println(wp.getPath()+","+wp.getDiscRatio());
                                        }
                                    }
                                    if(wikiPathSet.getAllOrderedPath()!=null){
                                        LinkedList<WikiPath> wikiPathLinkedList = wikiPathSet.getAllOrderedPath();
                                        System.out.println("order,"+wikiPathLinkedList.size());
                                        for(WikiPath wp : wikiPathLinkedList){
                                            System.out.println(wp.getPath()+","+wp.getDiscoRatio());
                                        }
                                    }
                                }else{
                                    //output and then delete old path set
                                    outputResult(wikiPathSet);
                                }

                            }else{
                                logger.error("can not find sibling for "+key);
                            }

                        }

                        key = newkey;

                            //new path
                            wikiPathSet = new WikiPathSet(key, useSQL);
                            logger.trace("New src&dest "+ key);

                    }else{
                        if(wikiPathSet!=null){
                            wikiPathSet.putPath(line);
                        }
                    }
                }

                line = bufferedReader.readLine();
                lineCounter++;

                if(lineCounter%100 == 0){
                    logger.info(lineCounter+" lines are loaded");
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
