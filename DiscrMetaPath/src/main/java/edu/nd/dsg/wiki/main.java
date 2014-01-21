package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.WikiPathSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class.getName());

    private static HashMap<String, WikiPathSet> wikiPathSetHashMap = new HashMap<String, WikiPathSet>();

    public static void main(String[] args){

        loadWikiPath("data/wikipath.txt");
        loadSiblingPath("data/wikiotherpaths.txt");

        Set<String> keySet = wikiPathSetHashMap.keySet();
        WikiPathSet wikiPathSet = null;

        for(String key : keySet) {
            wikiPathSet = wikiPathSetHashMap.get(key);
            outputResult(wikiPathSet);
        }
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

    protected static String loadNextPathSet(BufferedReader bufferedReader, String key){
        int lineCounter = 0;

        try{
            String line;
            String newKey;

            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){

                if(!line.contains("Timeout")&&!line.contains("Too big")){
                    if(line.contains("...")){
                        //new path src&dest

                        newKey = line.replace("...-","").trim();

                        if(!wikiPathSetHashMap.containsKey(newKey)){
                            //new path
                            wikiPathSetHashMap.put(newKey, new WikiPathSet(newKey));
                            logger.trace("New src&dest "+ newKey);
                            logger.info("Load "+lineCounter+" paths");
                            return newKey;
                        }else{
                            key = newKey;
                        }

                    }else{
                        logger.info("put path "+line+" into set");
                        wikiPathSetHashMap.get(key).putPath(line);
                    }
                }

                line = bufferedReader.readLine();
                lineCounter++;

                if(lineCounter%2==0){
                    logger.info("load "+lineCounter+" paths");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static String loadNextSiblingPathSet(BufferedReader bufferedReader, String key){
        int lineCounter = 0;
        try{
            String line;
            String newKey;
            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){
                if(line.contains("(")){
                    newKey = line.split(",")[0].replace("(","")+
                            "->"+
                            line.split("->")[1].replace("(","").replace(")","").replace(" ","");
                    newKey = newKey.trim();
                    if(!wikiPathSetHashMap.containsKey(newKey)){
                        logger.info("Load "+lineCounter+" sibling paths");
                        return newKey;
                    }else{
                        key = newKey;
                    }
                }else if(line.contains("...")){
                    //omit
                }else{
                    logger.info("put sibiling path "+line+" into set");
                    wikiPathSetHashMap.get(key).putSibling(line);
                }
                line = bufferedReader.readLine();
                lineCounter++;
                if(lineCounter%2==0){
                    logger.info("load "+lineCounter+" paths");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static void loadSiblingPath(String path){
        int lineCounter = 0;

        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            String line;
            String key = "";
            String newkey = "";

            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){
                if(line.contains("(")){
                    newkey = line.split(",")[0].replace("(","")+
                            "->"+
                            line.split("->")[1].replace("(","").replace(")","").replace(" ","");
                    newkey = newkey.trim();
                    outputResult(wikiPathSetHashMap.get(key));
                    key = newkey;
                    if(!wikiPathSetHashMap.containsKey(key)){
                        logger.warn("Can not find target path, src&dest are "+ key);
                    }

                }else if(line.contains("...")){
                    //omit
                }else{
                    wikiPathSetHashMap.get(key).putSibling(line);
                }
                line = bufferedReader.readLine();
                lineCounter++;

                if(lineCounter%10000 == 0){
                    logger.info(lineCounter+" lines are loaded");
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void loadWikiPath(String path){
        int lineCounter = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            String key="";

            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){

                if(!line.contains("Timeout")&&!line.contains("Too big")){
                    if(line.contains("...")){
                        //new path src&dest

                        key = line.replace("...-","").trim();

                        if(!wikiPathSetHashMap.containsKey(key)){
                            //new path
                            wikiPathSetHashMap.put(key, new WikiPathSet(key));
                            logger.trace("New src&dest "+ key);
                        }

                    }else{
                        wikiPathSetHashMap.get(key).putPath(line);
                    }
                }

                line = bufferedReader.readLine();
                lineCounter++;

                if(lineCounter%10000 == 0){
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
