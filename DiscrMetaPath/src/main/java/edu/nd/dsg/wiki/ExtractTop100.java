package edu.nd.dsg.wiki;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.util.HashMap;

public class ExtractTop100 {

    static void main(int argc, char[] argv){
        HashMap<String, Integer> df = new HashMap<String, Integer>();
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

        for(String key : df.keySet()){
            if(df.containsKey(key) && ((key.length()<=1))){
                df.remove(key);
            }
        }




    }

}
