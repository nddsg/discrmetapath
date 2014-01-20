package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.WikiPathSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Main {
    private static Logger logger = LogManager.getLogger(Main.class.getName());

    private static HashMap<String, WikiPathSet> wikiPathSetHashMap = new HashMap<String, WikiPathSet>();

    public static void main(String[] args){

        loadWikiPath("./data/wikipath.txt");
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
                        wikiPathSetHashMap.get(key).putPath(key);
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
