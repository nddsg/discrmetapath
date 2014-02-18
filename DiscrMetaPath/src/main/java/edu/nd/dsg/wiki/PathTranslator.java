package edu.nd.dsg.wiki;

import edu.nd.dsg.wiki.util.PatTypeFinderSQL;
import edu.nd.dsg.wiki.util.PatentTitleFinder;
import edu.nd.dsg.wiki.util.TitleFinder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathTranslator {

    private static Logger logger = LogManager.getLogger(PathTranslator.class.getName());
    private static TitleFinder titleFinder = TitleFinder.getInstance();
    private static PatentTitleFinder patTitleFinder = PatentTitleFinder.getInstance();
    private static final String SEPEATOR = ",";

    public static void main(String[] args){
        boolean retrieveDistinguishPaths = true;
        boolean retrieveOtherPaths = false;
        boolean isWiki = true;
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
            if(arg.startsWith("-p")){
                isWiki = false;
            }
        }

        if(isWiki){
            txtPathLoader("./data/allpath.txt", retrieveDistinguishPaths, retrieveOtherPaths, otherPathNumber,isWiki);
        }else{
            txtPathLoader("./data/allpatentpath.txt", retrieveDistinguishPaths, retrieveOtherPaths, otherPathNumber,isWiki);
        }

    }

    protected static String trimPath(String path){
        path = path.substring(0, path.indexOf(";")-1).replace("]", "").replace("[", "");
        logger.debug(path);
        String[] nodes = path.split(",");
        if(nodes.length<=2){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\""+"->");
        for(int i = 1; i < nodes.length-1; i++){
            sb.append(nodes[i].trim());
            sb.append("->");
        }
        sb.append("\"");
        logger.debug(sb.toString());
        return sb.toString();
    }

    protected static void txtPathLoader(String path, boolean retrieveDistinguishPaths,
                                        boolean retrieveOtherPaths, int otherPathNumber, boolean isWiki){
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            int minSize = 0;
            PrintWriter writer = new PrintWriter("non_order.csv", "UTF-8");
            PrintWriter writerOrdered = new PrintWriter("ordered.csv", "UTF-8");

            //write header to files
            writer.println("start,end,path_1,path_2,path_3,path_4,path_5");
            writerOrdered.println("start,end,path_1,path_2,path_3,path_4,path_5");

            PrintWriter w = null;

            //mark its separator

            if(retrieveDistinguishPaths){
                minSize += 2;
            }
            if(retrieveOtherPaths){
                minSize += otherPathNumber;
            }

            line = bufferedReader.readLine();


            while(line != null && !line.isEmpty()) {
                if(line.startsWith("non-order")||line.startsWith("order")){
                    if(line.startsWith("non-order")){
                        w = writer;
                    }else{
                        w = writerOrdered;
                    }
                    int size = Integer.parseInt(line.split(",")[1]);
                    if(size >= minSize) {
                        HashSet<Integer> nodeSet = new HashSet<Integer>();
                        LinkedList<String> pathList = new LinkedList<String>();
                        StringBuilder stringBuilder = new StringBuilder();
                        while (size > 0){
                            size--;
                            line = bufferedReader.readLine();
                            logger.debug(line);
                            pathList.add(line.replace("],", "];"));
                            String[] data = line.split("],");
                            String[] nodes = data[0].replace("[","").split(",");
                            if(nodes.length<=2){
                                pathList.pollLast();
                            }
                            for(String node : nodes) {
                                nodeSet.add(Integer.parseInt(node.trim()));
                            }
                            if(stringBuilder.length() == 0){
                                stringBuilder.append("\""+nodes[0].trim()+"\""+SEPEATOR);
                                stringBuilder.append("\""+nodes[nodes.length-1].trim()+"\""+SEPEATOR);
                            }
                        }
                        if(pathList.size()>=minSize){
                            if(retrieveDistinguishPaths){
                                stringBuilder.append(trimPath(pathList.pollFirst()));
                                stringBuilder.append(SEPEATOR);
                                stringBuilder.append(trimPath(pathList.pollLast()));
                                stringBuilder.append(SEPEATOR);
                            }
                            if(retrieveOtherPaths){
                                int cnt = 1;
                                if(pathList.size() == otherPathNumber){
                                    for(String pa : pathList){
                                        stringBuilder.append(trimPath(pa));
                                        stringBuilder.append(SEPEATOR);
                                    }
                                }else{
                                    while(cnt <= otherPathNumber){
                                        System.out.println("pathSize"+pathList.size()+"this size"+((pathList.size() - 1) * cnt / otherPathNumber));
                                        stringBuilder.append(trimPath(pathList.get((pathList.size() - 1) * cnt / otherPathNumber)));
                                        stringBuilder.append(SEPEATOR);
                                        cnt++;
                                    }
                                }

                            }
                            String s;
                            if(isWiki){
                                s = translatePath(stringBuilder.toString(), nodeSet);
                            }else{
                                s = translatePatPath(stringBuilder.toString(), nodeSet);
                            }
                            if(s!=null){
                                w.println(s);
                            }else{
                                System.out.println("empty string");
                            }
                        }

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

    protected static String translatePatPath(String targetStr, HashSet<Integer> nodeSet){
        HashMap<Integer, String> map = patTitleFinder.getTitle(nodeSet);
        logger.debug("mapSize:"+map.values().size());
        for(Integer key : map.keySet()) {
            logger.debug(key+" "+map.get(key));
            targetStr = targetStr.replaceAll(key.toString(), map.get(key));
        }
        Pattern r = Pattern.compile("[0-9]+["+SEPEATOR+"\"]");
        Matcher m = r.matcher(targetStr);
        if(m.find()){
            System.out.println("empty "+targetStr+" "+map);
            return null;
        }
        logger.debug(targetStr);
        return targetStr;
    }

    protected static String translatePath(String targetStr, HashSet<Integer> nodeSet){
        HashMap<Integer, String> map = titleFinder.getTitle(nodeSet);
        logger.debug("mapSize:"+map.values().size());
        for(Integer key : map.keySet()) {
            logger.debug(key+" "+map.get(key));
            targetStr = targetStr.replaceAll(key.toString(), map.get(key));
        }
        Pattern r = Pattern.compile("[0-9]+["+SEPEATOR+"\"]");
        Matcher m = r.matcher(targetStr);
        if(m.find()){
            return null;
        }
        logger.debug(targetStr);
        return targetStr;
    }
}
