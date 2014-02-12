package edu.nd.dsg.wiki;

import au.com.bytecode.opencsv.CSVWriter;
import edu.nd.dsg.wiki.util.TFOkapiCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class BM25Distance {
    static TFOkapiCalculator tfCalculator = TFOkapiCalculator.getInstance();
    public static void main(String[] args) throws IOException {
        LinkedList<LinkedList<Integer>> orderPath = new LinkedList<LinkedList<Integer>>();
        LinkedList<LinkedList<Integer>> nonorderPath = new LinkedList<LinkedList<Integer>>();
        PathCosSimilarity.txtPathLoader("./data/allpath.txt", true, true, 3, Integer.MAX_VALUE, orderPath, nonorderPath);

        CSVWriter writer = new CSVWriter(new FileWriter("./nonorder_bm25.csv"));
        CSVWriter nonWriter = new CSVWriter(new FileWriter("./order_bm25.csv"));

        getSeqResultLines(writer, nonorderPath);
        getSeqResultLines(nonWriter, orderPath);

        writer.close();
        nonWriter.close();

        writer = new CSVWriter(new FileWriter("./accumulate_bm25_order.csv"));
        nonWriter = new CSVWriter(new FileWriter("./accumulate_bm25_non_order.csv"));

        getAccumResultLines(writer, orderPath);
        getAccumResultLines(nonWriter, nonorderPath);

        writer.close();
        nonWriter.close();

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
                    String[] line = new String[2+accumulateCosine.size()];
                    line[0] = Integer.toString(groupId);
                    line[1] = Integer.toString(pathId);
                    for(int pos = 0; pos < accumulateCosine.size(); pos++){
                        line[pos+2] = Double.toString(accumulateCosine.get(pos));
                    }
                    lines.add(line);
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
