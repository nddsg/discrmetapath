package edu.nd.dsg.wiki.util;

import java.util.HashMap;

public class PathContainer {

    static int UNIT_ID = 0;
    static int IS_GOLDEN = 2;
    static int CHOOSE_PATH = 15;
    static int PATH_1 = 18;
    static int PATH_2 = 19;
    static int PATH_3 = 20;
    static int PATH_4 = 21;
    static int PATH_5 = 22;
    static int START = 23;
    static int STOP = 17;

    public String start;
    public String stop;
    public int total=0;
    public HashMap<String, Integer> paths = new HashMap<String, Integer>();

    public PathContainer(String[] line){
        start = line[START];
        stop = line[STOP];
        if(!paths.containsKey(line[CHOOSE_PATH])){
            paths.put(line[CHOOSE_PATH], 0);
        }
        if(!paths.containsKey(line[PATH_1])){
            paths.put(line[PATH_1], 0);
        }
        if(!paths.containsKey(line[PATH_2])){
            paths.put(line[PATH_2], 0);
        }
        if(!paths.containsKey(line[PATH_3])){
            paths.put(line[PATH_3], 0);
        }
        if(!paths.containsKey(line[PATH_4])){
            paths.put(line[PATH_4], 0);
        }
        if(!paths.containsKey(line[PATH_5])){
            paths.put(line[PATH_5], 0);
        }
        paths.put(line[CHOOSE_PATH],
                paths.get(line[CHOOSE_PATH])+1);
        total++;
    }

    public void put(String[] line) {
        if(!paths.containsKey(line[CHOOSE_PATH])){
            paths.put(line[CHOOSE_PATH], 0);
        }
        paths.put(line[CHOOSE_PATH],
                paths.get(line[CHOOSE_PATH])+1);
        total++;
    }

}