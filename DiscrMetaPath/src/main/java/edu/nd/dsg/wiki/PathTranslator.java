package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.TitleFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class PathTranslator {

    private static Logger logger = LogManager.getLogger(PathTranslator.class.getName());
    private static TitleFinder titleFinder = TitleFinder.getInstance();

    public static void main(String[] args){
        boolean retrieveDistinguishPaths = true;
        boolean retrieveOtherPaths = false;
        int otherPathNumber = 0;

        for(String arg : args){
            if(arg.startsWith("-nd")){
                retrieveDistinguishPaths = false;
            }
            if(arg.startsWith("-d")){
                retrieveDistinguishPaths = true;
            }
            if(arg.startsWith("-o")){
                otherPathNumber = Integer.parseInt(arg.replace("-o",""));
                if(otherPathNumber>0 && otherPathNumber < 50){ // a reasonable interval
                    retrieveOtherPaths = true;

                }else{
                    retrieveOtherPaths = false;
                }
            }
            if(arg.startsWith("-no")){
                retrieveOtherPaths = false;
            }
        }

        pathLoader("./data/allpath.txt", retrieveDistinguishPaths, retrieveOtherPaths, otherPathNumber);

    }

    protected static void pathLoader(String path, boolean retrieveDistinguishPaths,
                              boolean retrieveOtherPaths, int otherPathNumber){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            int minSize = 0;
            TitleFinder titleFinder = TitleFinder.getInstance();

            if(retrieveDistinguishPaths){
                minSize += 2;
            }
            if(retrieveOtherPaths){
                minSize += otherPathNumber;
            }

            line = bufferedReader.readLine();

            while(line != null && !line.isEmpty()) {
                if(line.startsWith("non-order")||line.startsWith("order")){
                    int size = Integer.parseInt(line.split(",")[1]);
                    if(size >= minSize) {
                        HashSet<Integer> nodeSet = new HashSet<Integer>();
                        LinkedList<String> pathList = new LinkedList<String>();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(line.split(",")[0]);
                        stringBuilder.append("\n");
                        while (size > 0){
                            size--;
                            line = bufferedReader.readLine();
                            logger.debug(line);
                            pathList.add(line.replace("],", "];"));
                            String[] data = line.split("],");
                            String[] nodes = data[0].replace("[","").split(",");
                            for(String node : nodes) {
                                nodeSet.add(Integer.parseInt(node.trim()));
                            }

                        }
                        if(retrieveDistinguishPaths){
                            stringBuilder.append(pathList.pollFirst());
                            stringBuilder.append("\n");
                            stringBuilder.append(pathList.pollLast());
                            stringBuilder.append("\n");
                        }
                        if(retrieveOtherPaths){
                            int cnt = 1;
                            while(cnt <= otherPathNumber){
                                stringBuilder.append(pathList.get((pathList.size()-1) * cnt/otherPathNumber));
                                stringBuilder.append("\n");
                                cnt++;
                            }
                        }
                        System.out.print(translatePath(stringBuilder.toString(), nodeSet));

                    }else{
                        logger.warn("Size "+size+" is smaller than "+minSize);
                        while(size > 0){
                            bufferedReader.readLine();
                            size--;
                        }
                    }
                }
                line = bufferedReader.readLine();
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String translatePath(String targetStr, HashSet<Integer> nodeSet){
        HashMap<Integer, String> map = titleFinder.getTitle(nodeSet);
        logger.debug("mapSize:"+map.values().size());
        for(Integer key : map.keySet()) {
            logger.debug(key+" "+map.get(key));
            targetStr = targetStr.replaceAll("(?<=[^.])("+key.toString()+"(?=[^0-9]))", map.get(key));
        }
        logger.debug(targetStr);
        return targetStr;
    }
}
