package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.WikiPathSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class.getName());

    private static HashMap<String, WikiPathSet> wikiPathSetHashMap = new HashMap<String, WikiPathSet>();


    public static void main(String[] args){
        boolean useSQL = true;
        HashMap<String, LinkedList<String>> siblingPathText;

        for(String arg : args){
            if(arg.startsWith("-NoSQL")){
                useSQL = false;
            }
        }

        siblingPathText = loadSiblingPathText("data/wikiotherpaths.txt");

        loadWikiPath("data/wikipath.txt", useSQL, siblingPathText);

    }

    protected static void outputResult(WikiPathSet wikiPathSet){

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{discr:{path:");
        stringBuilder.append(wikiPathSet.getDiscriminativePath().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getDiscriminativeRate());
        stringBuilder.append("},simi:{path:");
        stringBuilder.append(wikiPathSet.getSimilarPath().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getSimilarRate());
        stringBuilder.append("},discro:{path:");
        stringBuilder.append(wikiPathSet.getDiscriminativePathByOrder().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getDiscriminativeRateByOrder());
        stringBuilder.append("},simio:{path:");
        stringBuilder.append(wikiPathSet.getSimilarPathByOrder().getPath());
        stringBuilder.append(", inter:");
        stringBuilder.append(wikiPathSet.getSimilarRateByOrder());
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

    protected static void loadWikiPath(String path, boolean useSQL, HashMap<String, LinkedList<String>> siblingPathText){
        int lineCounter = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
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
                                    wikiPathSetHashMap.get(key).putSibling(sibling);
                                }

                                //output and then delete old path set
                                outputResult(wikiPathSetHashMap.get(key));
                                wikiPathSetHashMap.remove(key);
                            }else{
                                logger.warn("can not find sibling for "+key);
                            }

                        }

                        key = newkey;

                        if(!wikiPathSetHashMap.containsKey(key)){
                            //new path
                            wikiPathSetHashMap.put(key, new WikiPathSet(key, useSQL));
                            logger.trace("New src&dest "+ key);
                        }

                    }else{
                        wikiPathSetHashMap.get(key).putPath(line);
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
