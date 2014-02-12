package edu.nd.dsg.wiki;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.nd.dsg.wiki.util.IdFinder;
import edu.nd.dsg.wiki.util.PathContainer;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;


public class EvaluateQuestionnaire {

    static int _START = 0, _STOP = 1, _PATH = 2, _SRATE = 3, _NORATE = 4, _ORATE=5, _NOMEDIAN =6, _NOMEAN =7, _OMEAN = 8, _OMEDIAN=9,
               _OMAX = 10, _NOMAX = 11, _OMIN = 12, _NOMIN=13;

    static int UNIT_ID = 0;
    static int IS_GOLDEN = 2;
    static int CHOOSE_PATH = 15;
    static int START = 23;
    static int STOP = 17;
    public static void main(String[] args) throws IOException {
        CSVWriter csvWriter=null;

        try {

            //load crowdflower result
            CSVReader csvReader = new CSVReader(new FileReader("./data/f380167.csv"));
            String[] nextLine;
            HashMap<String, PathContainer> pathMap = new HashMap<String, PathContainer>();
            while((nextLine = csvReader.readNext()) != null) {
                if(nextLine[IS_GOLDEN].equals("false")) {
                    //omit golden data
                    String key = nextLine[START].concat(nextLine[STOP]);
                    if(!pathMap.containsKey(key)){
                        pathMap.put(key, new PathContainer(nextLine));
                    }else{
                        pathMap.get(key).put(nextLine);
                    }
                }
            }

            //load path results from wikipedia database
            BufferedReader bufferedReader = new BufferedReader(new FileReader("./data/allpath.txt"));
            String line;
            String type="";
            HashMap<String, HashMap<String, HashMap<String, Double>>> pathDisc = new HashMap<String, HashMap<String, HashMap<String, Double>>>();
            pathDisc.put("non-ordered", new HashMap<String, HashMap<String, Double>>());
            pathDisc.put("ordered", new HashMap<String, HashMap<String, Double>>());
            while((line = bufferedReader.readLine())!=null) {
                if(line.startsWith("non")){
                    type = "non-ordered";
                }else if(line.startsWith("order")){
                    type = "ordered";
                }else if(line.startsWith("[")){
                    String[] n = line.split("],")[0].split(",");
                    String key = n[0]+","+n[n.length-1];
                    key = key.replaceAll("[\\[ ]+", "");
                    if(!pathDisc.get(type).containsKey(key)){
                        pathDisc.get(type).put(key, new HashMap<String, Double>());
                    }
                    pathDisc.get(type).get(key).put(line.split("],")[0].replaceAll("[\\[ ]+", ""), Double.parseDouble(line.split("],")[1]));
                }
            }

            csvWriter = new CSVWriter(new FileWriter("./result_with_zero.csv"));
            String[] header = {
                    "start", "stop",
                    "path", "selection_rate",
                    "non_ordered_ratio",
                    "ordered_ratio",
                    "non_ordered_ratio_median",
                    "non_ordered_ratio_mean",
                    "ordered_ratio_mean",
                    "ordered_ratio_median",
                    "ordered_max",
                    "non_ordered_max",
                    "ordered_min",
                    "non_ordered_min"
            };

            csvWriter.writeNext(header);

            IdFinder idFinder = IdFinder.getInstance();

            for(String key : pathMap.keySet()) {
                LinkedList<String[]> linkedStringArry = new LinkedList<String[]>();
                PathContainer pathContainer = pathMap.get(key);
                for(String p : pathContainer.paths.keySet()){
                    boolean write = true;
                    String[] dataLine = new String[14];
                    dataLine[_START] = pathContainer.start;
                    dataLine[_STOP] = pathContainer.stop;
                    Double result = (double)pathContainer.paths.get(p) / (double)pathContainer.total;
                    dataLine[_SRATE] = result.toString();
                    p = p.replaceAll("&gt;",">");
                    String[] pathNode = p.split("->");
                    String[] pathNO = new String[pathNode.length+2];
                    pathNO[0] = pathContainer.start;
                    int i;
                    for(i = 1; i<=pathNode.length; i++){
                        pathNO[i] = pathNode[i-1];
                    }
                    pathNO[i] = pathContainer.stop;
                    String path = idFinder.getIdStrByTitle(pathNO);
                    String startStop = idFinder.getIdStrByTitle(new String[]{pathNO[0],pathNO[pathNO.length-1]});

                    dataLine[_PATH] = path;
                    if(pathDisc.get("non-ordered").containsKey(startStop)){
                        if(pathDisc.get("non-ordered").get(startStop).containsKey(path)){
                            HashMap<String, Double> data = pathDisc.get("non-ordered").get(startStop);

                            Double median=0d, mean=0d;
                            for(String s : data.keySet()){
                                mean += data.get(s);
                            }
                            mean /= data.keySet().size();

                            Double[] val = data.values().toArray(new Double[data.values().size()]);
                            Arrays.sort(val);

                            if(val.length%2 == 0){
                                median = val[val.length/2]+val[val.length/2-1];
                            }else{
                                median = val[val.length/2];
                            }

                            dataLine[_NOMEAN] = mean.toString();
                            dataLine[_NOMEDIAN] = median.toString();
                            dataLine[_NORATE] = data.get(path).toString();
                            dataLine[_NOMAX] = val[val.length-1].toString();
                            dataLine[_NOMIN] = val[0].toString();
                        }else{
                            write=false;
                        }
                    }else{
                        write = false;
                    }
                    if(pathDisc.get("ordered").containsKey(startStop)){
                        if(pathDisc.get("ordered").get(startStop).containsKey(path)){
                            HashMap<String, Double> data = pathDisc.get("ordered").get(startStop);

                            Double median=0d, mean=0d;
                            for(String s : data.keySet()){
                                mean += data.get(s);
                            }
                            mean /= data.keySet().size();

                            Double[] val = data.values().toArray(new Double[data.values().size()]);
                            Arrays.sort(val);

                            if(val.length%2 == 0){
                                median = val[val.length/2]+val[val.length/2-1];
                            }else{
                                median = val[val.length/2];
                            }

                            dataLine[_OMEAN] = mean.toString();
                            dataLine[_OMEDIAN] = median.toString();
                            dataLine[_ORATE] = data.get(path).toString();
                            dataLine[_OMAX] = val[val.length-1].toString();
                            dataLine[_OMIN] = val[0].toString();
                        }else{
                            write = false;
                        }
                    }else{
                        write = false;
                    }
                    for(String item : dataLine){
                        System.out.print(item+" ");
                    }
                    System.out.println("");
                    if(write&&linkedStringArry!=null){
                        linkedStringArry.add(dataLine);
                    }else{
                        linkedStringArry=null;
                    }
                }
                if(linkedStringArry!=null){
                    csvWriter.writeAll(linkedStringArry);
                }
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(csvWriter!=null)
                csvWriter.close();
        }

    }

}
