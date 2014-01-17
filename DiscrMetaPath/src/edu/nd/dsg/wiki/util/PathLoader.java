package edu.nd.dsg.wiki.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class PathLoader {
    private static Logger logger = LogManager.getLogger(PathLoader.class.getName());

    private static PathLoader instance = null;

    private static HashMap<String, WikiPath> wikiPathList =
            new HashMap<String, WikiPath>();

    private static HashMap<String, WikiPath> wikiOtherPathList =
            new HashMap<String, WikiPath>();

    private static boolean wikiPathFinished = false;
    private static boolean wikiOtherPathFinished = false;


    private PathLoader(){
    }

    public static PathLoader getInstance(){
        if (instance == null) {
            instance = new PathLoader();
        }
        return instance;
    }

    public HashMap<String, WikiPath> loadWikiPath(){
        return loadWikiPath("./data/wikipath.txt");
    }
    public HashMap<String, WikiPath> loadWikiPath(String path){

        if(wikiPathFinished){
            return wikiPathList;
        }

        wikiPathFinished = true;

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

                        if(!wikiPathList.containsKey(key)){
                            //new path
                            wikiPathList.put(key, new WikiPath());
                            logger.trace("New src&dest "+ key);
                        }

                    }else{
                        wikiPathList.get(key).put(line);
                    }
                }

                line = bufferedReader.readLine();
                lineCounter++;

                if(lineCounter%10000 == 0){
                    logger.info(lineCounter+" lines are loaded");
                }

            }
        }catch (FileNotFoundException e){
            logger.error("Can not find file wikipath.txt");
            e.printStackTrace();
            wikiPathList = null;
        }catch (IOException e){
            logger.error(e);
            e.printStackTrace();
            wikiPathList = null;
        }

        return wikiPathList;
    }

    public HashMap<String, WikiPath> loadOtherWikiPath(){
        return loadOtherWikiPath("./data/wikiotherpaths.txt");
    }
    public HashMap<String, WikiPath> loadOtherWikiPath(String path){

        if(wikiOtherPathFinished){
            return wikiOtherPathList;
        }

        wikiOtherPathFinished = true;

        int lineCounter = 0;

        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

            String line;
            String key = "";

            line = bufferedReader.readLine();
            lineCounter++;

            while(line != null && !line.isEmpty()){
                if(line.contains("(")){
                    key = line.split(",")[0].replace("(","")+
                            "->"+
                            line.split("->")[1].replace("(","").replace(")","").replace(" ","");
                    key = key.trim();
                    if (!wikiOtherPathList.containsKey(key)){
                        wikiOtherPathList.put(key, new WikiPath());
                    }

                    if(!wikiPathList.containsKey(key)){
                        logger.warn("Can not find siblings in wikipath, src&dest are "+ key);
                    }

                }else if(line.contains("...")){
                    //omit
                }else{
                    wikiOtherPathList.get(key).put(line);
                }
                line = bufferedReader.readLine();
                lineCounter++;

                if(lineCounter%10000 == 0){
                    logger.info(lineCounter+" lines are loaded");
                }
            }


        }catch (FileNotFoundException e){
            logger.error("Can not find file wikiotherpaths.txt");
            e.printStackTrace();
            wikiOtherPathList = null;
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
            wikiOtherPathList = null;
        }

        return wikiOtherPathList;
    }


}
