package edu.nd.dsg.wiki;

import au.com.bytecode.opencsv.CSVWriter;
import edu.nd.dsg.wiki.util.TFOkapiCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class BM25Distance {
    static TFOkapiCalculator tfCalculator;
    static final String[] header = {"groupId", "pathId", "nodeId", "value"};
    public static void main(String[] args) throws IOException {
        boolean isWiki = true;
        for(String arg : args){
            if(arg.startsWith("-p")){
                isWiki = false;
            }
        }
        System.out.println(isWiki);

        String allpath = isWiki ? "./data/allpath.txt" : "./data/allpatentpath.txt";
        tfCalculator = TFOkapiCalculator.getInstance(isWiki ? "./data/df.json" : "./data/detd.json", isWiki);

        LinkedList<LinkedList<Integer>> orderPath = new LinkedList<LinkedList<Integer>>();
        LinkedList<LinkedList<Integer>> nonorderPath = new LinkedList<LinkedList<Integer>>();
        PathCosSimilarity.txtPathLoader(allpath, true, true, 3, Integer.MAX_VALUE, orderPath, nonorderPath);

        String prefix = isWiki ? "./" : "./patent_";

        for(String arg : args){
            if(arg.startsWith("-ACC")){
                CSVWriter writer = new CSVWriter(new FileWriter(prefix+"accumulate_bm25_order.csv"));
                CSVWriter nonWriter = new CSVWriter(new FileWriter(prefix+"accumulate_bm25_non_order.csv"));
                writer.writeNext(header);
                nonWriter.writeNext(header);
                getAccumResultLines(writer, orderPath);
                getAccumResultLines(nonWriter, nonorderPath);

                writer.close();
                nonWriter.close();
            }
            if(arg.startsWith("-NODE")){
                CSVWriter writer = new CSVWriter(new FileWriter(prefix+"nonorder_bm25.csv"));
                CSVWriter nonWriter = new CSVWriter(new FileWriter(prefix+"order_bm25.csv"));
                writer.writeNext(header);
                nonWriter.writeNext(header);
                getSeqResultLines(writer, nonorderPath);
                getSeqResultLines(nonWriter, orderPath);

                writer.close();
                nonWriter.close();
            }
        }

    }

    protected static void getAccumResultLines(CSVWriter csvWriter, LinkedList<LinkedList<Integer>> pathList){
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
                //a path
                for(LinkedList<Integer> path : workList){
                    LinkedList<Double> accumulateCosine = tfCalculator.getaccumulatedTF(path);
                    for(int nodeId = 0; nodeId < accumulateCosine.size(); nodeId++) {
                        String[] line = new String[header.length];
                        line[0] = Integer.toString(groupId);
                        line[1] = Integer.toString(pathId);
                        line[2] = Integer.toString(nodeId);
                        line[3] = Double.toString(accumulateCosine.get(nodeId));
                        lines.add(line);
                    }
                    pathId++;
                }
                if(csvWriter!=null){
                    csvWriter.writeAll(lines);
                }else{
                    for(String[] line : lines){
                        for(String item : line){
                            System.out.print(item + " ");
                        }
                        System.out.println();
                    }
                }
            }catch (NullPointerException e){
                //TODO fix redirect page terms now just ignore them...
                System.out.println("Get non-page stuff or redirect page, ignore it...");
                e.printStackTrace();
            }
            groupId++;

        }
    }

    protected static void getSeqResultLines(CSVWriter csvWriter, LinkedList<LinkedList<Integer>> pathList){
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
                    LinkedList<Double> accumulateCosine = tfCalculator.getSeqNodeTF(path);
                    for(int nodeId = 0; nodeId < accumulateCosine.size(); nodeId++) {
                        String[] line = new String[header.length];
                        line[0] = Integer.toString(groupId);
                        line[1] = Integer.toString(pathId);
                        line[2] = Integer.toString(nodeId);
                        line[3] = Double.toString(accumulateCosine.get(nodeId));
                        lines.add(line);
                    }
                    pathId++;
                }
                if(csvWriter!=null){
                    csvWriter.writeAll(lines);
                }else{
                    for(String[] line : lines){
                        for(String item : line){
                            System.out.print(item + " ");
                        }
                        System.out.println();
                    }
                }
            }catch (NullPointerException e){
                //TODO fix redirect page terms now just ignore them...
                System.out.println("Get non-page stuff or redirect page, ignore it...");
                e.printStackTrace();
            }
            groupId++;

        }
    }

}
