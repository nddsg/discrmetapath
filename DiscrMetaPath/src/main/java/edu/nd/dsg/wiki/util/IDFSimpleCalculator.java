package edu.nd.dsg.wiki.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;

public class IDFSimpleCalculator implements IDFCalculator {
    private static IDFSimpleCalculator instance = null;
    private static HashMap<String, Integer> df = null;
    private static final int TOTAL_DOC = 10276554;

    protected IDFSimpleCalculator() throws FileNotFoundException {
        try {
            Gson gson = new Gson();
            FileInputStream fileInputStream = new FileInputStream(new File("./data/df.json"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String jsonStr = bufferedReader.readLine();
            df = gson.fromJson(jsonStr, new TypeToken<HashMap<String, Integer>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected IDFSimpleCalculator(String dfPath) throws FileNotFoundException {
        Gson gson = new Gson();
        FileInputStream fileInputStream = new FileInputStream(new File(dfPath));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        df = gson.fromJson(bufferedReader, HashMap.class);
    }

    public static IDFSimpleCalculator getInstance() throws FileNotFoundException {
        if (instance == null) {
            instance = new IDFSimpleCalculator();
        }
        return instance;
    }

    public static IDFSimpleCalculator getInstance(String dfPath) throws FileNotFoundException {
        if (instance == null) {
            instance = new IDFSimpleCalculator(dfPath);
        }
        return instance;
    }

    public double getIDF(String term) {
        if (!df.containsKey(term)) {
            return 0;
        }
        return Math.log((double) TOTAL_DOC / (double) df.get(term));
    }

}
