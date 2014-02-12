package edu.nd.dsg.wiki;


import au.com.bytecode.opencsv.CSVWriter;
import edu.nd.dsg.wiki.util.TFCalculator;
import sun.awt.image.ImageWatched;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;

public class PathCosSimilarity {
    static LinkedList<LinkedList<Integer>> orderPath = new LinkedList<LinkedList<Integer>>();
    static LinkedList<LinkedList<Integer>> nonorderPath = new LinkedList<LinkedList<Integer>>();
    static TFCalculator tfCalculator = TFCalculator.getInstance();
    public static void main(String[] args) throws IOException {

        txtPathLoader("./data/allpath.txt", true, true, 3);
        CSVWriter csvWriterOrder =  new CSVWriter(new FileWriter("./accumulate_cosine_order.csv"));
        CSVWriter csvWriterNonOrder =  new CSVWriter(new FileWriter("./accumulate_cosine_non_order.csv"));

        getResultLines(csvWriterOrder, orderPath);
        getResultLines(csvWriterNonOrder, nonorderPath);

        csvWriterOrder.close();
        csvWriterNonOrder.close();
    }

    protected static void getResultLines(CSVWriter csvWriter, LinkedList<LinkedList<Integer>> pathList){
        int groupId = 1;
        while(pathList.size()>0){
            LinkedList<LinkedList<Integer>> workList = new LinkedList<LinkedList<Integer>>();
            LinkedList<String[]> lines = new LinkedList<String[]>();
            int len = 0;
            while(len<5){
                workList.add(pathList.pollFirst());
                len++;
            }
            System.out.println("try calculate groupID:"+groupId+" among paths:"+workList);
            try{
                int pathId = 1;
                for(LinkedList<Integer> path : workList){
                    LinkedList<Double> accumulateCosine = tfCalculator.getaccumulatedTF(path);
                    String[] line = new String[2+accumulateCosine.size()];
                    line[0] = Integer.toString(groupId);
                    line[1] = Integer.toString(pathId);
                    for(int pos = 0; pos < accumulateCosine.size(); pos++){
                        line[pos+2] = Double.toString(accumulateCosine.get(pos));
                    }
                    lines.add(line);
                    pathId++;
                }
                csvWriter.writeAll(lines);
            }catch (NullPointerException e){
                //TODO fix redirect page terms now just ignore them...
                System.out.println("Get non-page stuff or redirect page, ignore it, ignore it...");
            }
            groupId++;

        }
    }

    protected static void txtPathLoader(String path, boolean retrieveDistinguishPaths,
                                        boolean retrieveOtherPaths, int otherPathNumber){


        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            String line;
            int minSize = 0;
            LinkedList<LinkedList<Integer>> resPathList = null;
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
                        resPathList = nonorderPath;
                    }else{
                        resPathList = orderPath;
                    }
                    int size = Integer.parseInt(line.split(",")[1]);
                    if(size >= minSize) {
                        HashSet<Integer> nodeSet = new HashSet<Integer>();
                        LinkedList<String> pathList = new LinkedList<String>();
                        StringBuilder stringBuilder = new StringBuilder();
                        while (size > 0){
                            size--;
                            line = bufferedReader.readLine();
                            pathList.add(line.split("],")[0].replace("[",""));
                            if(line.split("],")[0].replace("[","").split(",").length<=2){
                                pathList.pollLast();
                            }

                        }
                        if(pathList.size()>minSize){
                            String[] strArryLast=null;
                            if(retrieveDistinguishPaths){
                                LinkedList<Integer> pList = new LinkedList<Integer>();
                                String[] strArry = pathList.pollFirst().replace(" ","").split(",");
                                strArryLast = pathList.pollLast().replace(" ","").split(",");

                                for(String node : strArry) {
                                    pList.add(Integer.parseInt(node));
                                }
                                resPathList.add(pList);
                            }
                            if(retrieveOtherPaths){
                                int cnt = 1;
                                while(cnt <= otherPathNumber){
                                    LinkedList<Integer> pList = new LinkedList<Integer>();
                                    String[] strArry = pathList.get((pathList.size() - 1) * cnt / otherPathNumber).replace(" ","").split(",");
                                    for(String node : strArry) {
                                        pList.add(Integer.parseInt(node));
                                    }
                                    resPathList.add(pList);
                                    cnt++;
                                }
                            }
                            if(retrieveDistinguishPaths){
                                LinkedList<Integer> pList = new LinkedList<Integer>();
                                for(String node : strArryLast) {
                                    pList.add(Integer.parseInt(node));
                                }
                                resPathList.add(pList);
                            }
                        }

                    }else{
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

}
