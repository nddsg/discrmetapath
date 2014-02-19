package edu.nd.dsg.wiki.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;

public class IDFOkapiCalculator implements IDFCalculator {

    private static IDFOkapiCalculator instance = null;
    private static HashMap<String, Integer> df = null;
    private static final int TOTAL_DOC = 10276554;
    private static final int PAT_DOC = 2064267;
    private static final double WIKIAVG = 524.7;
    private static final double PATAVG = 2924.897;

    public static Double getAvgDocLength(boolean isWiki) {
        if(isWiki) {
            return WIKIAVG;
        }else{
            return PATAVG;
        }
    }

    private static Double AVG_DOC_LEN = null;
    private boolean isWiki;

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
            for (Integer freq : df.values()) {
                tmp += freq;
                if (tmp >= TOTAL_DOC) {
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

    protected IDFOkapiCalculator(String dfPath, boolean isWiki) throws FileNotFoundException {
        this.isWiki = isWiki;
        try {
            Gson gson = new Gson();
            FileInputStream fileInputStream = new FileInputStream(new File(dfPath));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String jsonStr = bufferedReader.readLine();
            df = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Integer>>() {
            }.getType());
            double len = 0;
            long tmp = 0;
            for (Integer freq : df.values()) {
                tmp += freq;
                if (tmp >= TOTAL_DOC) {
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

    public static IDFOkapiCalculator getInstance() throws FileNotFoundException {
        if (instance == null) {
            instance = new IDFOkapiCalculator();
        }
        return instance;
    }

    public static IDFOkapiCalculator getInstance(String dfPath, boolean isWiki) throws FileNotFoundException {
        if (instance == null) {
            instance = new IDFOkapiCalculator(dfPath, isWiki);
        }
        return instance;
    }

    public double getIDFSum(Collection<String> termSet) {
        double sum = 0;
        for (String term : termSet) {
            double temp = getIDF(term);
            if (temp == 0) {
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
        if (isWiki) {
            return Math.log(((double) TOTAL_DOC - (double) df.get(term) + 0.5) / (df.get(term) + 0.5));
        } else {
            return Math.log(((double) PAT_DOC - (double) df.get(term) + 0.5) / (df.get(term) + 0.5));
        }
    }
}
