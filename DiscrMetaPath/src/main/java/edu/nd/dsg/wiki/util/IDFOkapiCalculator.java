package edu.nd.dsg.wiki.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class IDFOkapiCalculator implements IDFCalculator {

    private static IDFOkapiCalculator instance = null;
    private static HashMap<String, Integer> df = null;
    private static final int TOTAL_DOC = 10276554;

    public static Double getAvgDocLength() {
        return AVG_DOC_LEN;
    }

    private static Double AVG_DOC_LEN = null;

    protected IDFOkapiCalculator() throws FileNotFoundException {
        try {
            Gson gson = new Gson();
            FileInputStream fileInputStream = new FileInputStream(new File("./data/df.json"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String jsonStr = bufferedReader.readLine();
            df = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Integer>>() {
            }.getType());
            double len = 0;
            long tmp = 0;
            for(Integer freq : df.values()) {
                tmp += freq;
                if(tmp >= TOTAL_DOC){
                    len++;
                    tmp -= TOTAL_DOC;
                }
            }
            len += tmp / TOTAL_DOC;
            AVG_DOC_LEN = len;

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected IDFOkapiCalculator(String dfPath) throws FileNotFoundException {
        Gson gson = new Gson();
        FileInputStream fileInputStream = new FileInputStream(new File(dfPath));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        df = gson.fromJson(bufferedReader, HashMap.class);
    }

    public static IDFOkapiCalculator getInstance() throws FileNotFoundException {
        if (instance == null) {
            instance = new IDFOkapiCalculator();
        }
        return instance;
    }

    public static IDFOkapiCalculator getInstance(String dfPath) throws FileNotFoundException {
        if (instance == null) {
            instance = new IDFOkapiCalculator(dfPath);
        }
        return instance;
    }

    public double getIDFSum(Collection<String> termSet){
        double sum = 0;
        for(String term : termSet) {
            double temp = getIDF(term);
            if(temp == 0){
                return 0;
            }
            sum += temp;
        }
        return sum;
    }

    @Override
    public double getIDF(String term) {
        if (!df.containsKey(term)) {
            return 0;
        }
        return Math.log(((double) TOTAL_DOC - df.get(term) + 0.5) / (df.get(term)+0.5));
    }
}
