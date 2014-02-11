package edu.nd.dsg.wiki.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import edu.nd.dsg.util.ConnectionPool;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class TFCalculator extends Finder {
    private static TFCalculator instance = null;
    private static ConnectionPool connectionPool = null;
    private IDFCalculator idfCalculator = null;

    protected TFCalculator(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
                idfCalculator = IDFCalculator.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static TFCalculator getInstance(){
        if(instance == null) {
            instance = new TFCalculator();
        }

        return instance;
    }

    private HashMap<String, Double> getTermFreq(LinkedList<Integer> path, HashMap<Integer, HashMap<String, Integer>> termMap) {
        HashMap<String, Integer> termFreq = new HashMap<String, Integer>();
        for(int node : path){
            HashMap<String, Integer> map = termMap.get(node);
            for(String key : map.keySet()){
                if(!termFreq.containsKey(key)){
                    termFreq.put(key, 0);
                }
                termFreq.put(key, termFreq.get(key) + map.get(key));
            }
        }

        long total = getTotalWords(termFreq);

        HashMap<String, Double> normalizedTermFreq = new HashMap<String, Double>();

        for(String term : termFreq.keySet()){
            normalizedTermFreq.put(term, ((double)termFreq.get(term)/(double)total) * idfCalculator.getIDF(term));
        }

        return normalizedTermFreq;
    }

    private long getTotalWords(HashMap<String, Integer> termFreq){
        long total=0l;
        for(int num : termFreq.values()){
            total +=num;
        }
        return total;
    }

    private double getCosSimilarity(HashMap<String, Double> x, HashMap<String, Double> y){
        HashSet<String> termSet = new HashSet<String>();
        termSet.addAll(x.keySet());
        termSet.addAll(y.keySet());
        double innerProduct = 0,
             xNormalizeFactor = 0,
             yNormalizeFactor = 0;

        for(String term : termSet) {
            double xTermFreq, yTermFreq;
            if(x.containsKey(term)){
                xTermFreq = x.get(term);
            }else{
                xTermFreq = 0;
            }
            if(y.containsKey(term)){
                yTermFreq = y.get(term);
            }else{
                yTermFreq = 0;
            }

            innerProduct += xTermFreq * yTermFreq;
            xNormalizeFactor += xTermFreq * xTermFreq;
            yNormalizeFactor += yTermFreq * yTermFreq;
        }

        System.out.println("inner product "+innerProduct);
        System.out.println("xNormalizeFactor "+xNormalizeFactor);
        System.out.println("yNormalizeFactor "+yNormalizeFactor);


        return innerProduct/(Math.sqrt(xNormalizeFactor)*Math.sqrt(yNormalizeFactor));
    }

    public double getTF(LinkedList<Integer> x, LinkedList<Integer> y) {
        HashSet<Integer> nodeSet = new HashSet<Integer>();
        nodeSet.addAll(x);
        nodeSet.addAll(y);
        double result=0;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT page_id, term FROM wikipedia.bsPageTerm WHERE page_id IN (");

        for(int node : nodeSet) {
            stringBuilder.append(node);
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append(");");

        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;
        Gson gson = new Gson();
        try{
            conn = connectionPool.getConnection();
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            rs = st.executeQuery(stringBuilder.toString());
            HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap = new HashMap<Integer, HashMap<String, Integer>>();
            while(rs.next()){

                HashMap<String, Integer> termFreq = gson.fromJson(rs.getString("term"), new TypeToken<HashMap<String, Integer>>() {
                }.getType());
                nodeTermFreqMap.put(rs.getInt("page_id"), termFreq);
            }

            HashMap<String, Double> xTermFreq = getTermFreq(x, nodeTermFreqMap);
            HashMap<String, Double> yTermFreq = getTermFreq(y, nodeTermFreqMap);
            result = getCosSimilarity(xTermFreq, yTermFreq);




        }catch (SQLException e){
            printSQLException(e);
            logger.debug(stringBuilder.toString());
            e.getErrorCode();
        } finally {
            try{
                if(rs != null){
                    rs.close();
                }
                if(st != null){
                    st.close();
                }
                if(conn!=null){
                    conn.close();
                }
            }catch (SQLException e){
                printSQLException(e);
                logger.debug(stringBuilder.toString());
                e.getErrorCode();
            }

        }

        return result;

    }

}
