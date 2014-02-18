package edu.nd.dsg.wiki.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import edu.nd.dsg.util.ConnectionPool;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class TFSimpleCalculator extends Finder implements TFCalculator {
    private static TFSimpleCalculator instance = null;
    private static ConnectionPool connectionPool = null;
    private IDFSimpleCalculator idfCalculator = null;
    private boolean isWiki=true;

    protected TFSimpleCalculator(String path, boolean isWiki){
        this.isWiki = isWiki;
        if(connectionPool==null){
            System.out.println("create TFSC isWiki:"+this.isWiki);
            try {
                connectionPool = ConnectionPool.getInstance();
                idfCalculator = IDFSimpleCalculator.getInstance(path, isWiki);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    protected TFSimpleCalculator(boolean isWiki){
        this.isWiki = isWiki;
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
                idfCalculator = IDFSimpleCalculator.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static TFSimpleCalculator getInstance(boolean isWiki){
        if(instance == null) {
            instance = new TFSimpleCalculator(isWiki);
        }

        return instance;
    }

    public static TFSimpleCalculator getInstance(String path, boolean isWiki){
        System.out.println("TFSimpleCalculator getInst isWiki:"+isWiki);
        if(instance == null) {
            instance = new TFSimpleCalculator(path, isWiki);
        }

        return instance;
    }

    private HashMap<String, Double> getTermFreq(LinkedList<Integer> path, HashMap<Integer, HashMap<String, Integer>> termMap) throws NullPointerException{
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
        long max = getMaxWords(termFreq);

        HashMap<String, Double> normalizedTermFreq = new HashMap<String, Double>();

        for(String term : termFreq.keySet()){
            normalizedTermFreq.put(term, (0.5+(0.5*termFreq.get(term))/max) * idfCalculator.getIDF(term));
            //normalizedTermFreq.put(term, ((double)termFreq.get(term)/(double)total) * idfCalculator.getIDF(term));
        }

        return normalizedTermFreq;
    }

    private long getMaxWords(HashMap<String, Integer> termFreq) {
        long max = 0l;

        for(int num : termFreq.values()) {
            if(max < num){
                max = num;
            }
        }

        return max;

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

    public LinkedList<Double> getaccumulatedTF(LinkedList<Integer> path) throws NullPointerException{
        LinkedList<Double> res = new LinkedList<Double>();
        HashSet<Integer> nodeSet = new HashSet<Integer>();
        nodeSet.addAll(path);
        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap;
        if(isWiki){
            nodeTermFreqMap = getTermFreqMap(nodeSet);
        }else{
            nodeTermFreqMap = getPatTermFreqMap(nodeSet);

        }
        int count = 1;
        while(count<path.size()){
            int acc = 0;
            LinkedList<Integer> accumulatePath = new LinkedList<Integer>();
            LinkedList<Integer> nextNode = new LinkedList<Integer>();
            while(acc < count){
                accumulatePath.add(path.get(acc));
                acc++;
            }
            nextNode.add(path.get(acc));
            System.out.println(accumulatePath+"  "+nextNode);
            HashMap<String, Double> xTermFreq = getTermFreq(accumulatePath, nodeTermFreqMap);
            HashMap<String, Double> yTermFreq = getTermFreq(nextNode, nodeTermFreqMap);
            if(xTermFreq.size() == 0 || yTermFreq.size() == 0) {
                throw new NullPointerException();
            }
            res.add(getCosSimilarity(xTermFreq, yTermFreq));

            count++;
        }
        return res;
    }

    public HashMap<Integer, HashMap<String, Integer>> getPatTermFreqMap(HashSet<Integer> nodeSet) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT patent, detd FROM patents.bsPageTerm WHERE patent IN (");
        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap = new HashMap<Integer, HashMap<String, Integer>>();

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
        System.out.println(stringBuilder.toString());
        try{
            conn = connectionPool.getConnection();
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            rs = st.executeQuery(stringBuilder.toString());
            while(rs.next()){

                HashMap<String, Integer> termFreq = gson.fromJson(new String(rs.getBytes("detd"),"UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                }.getType());
                nodeTermFreqMap.put(rs.getInt("patent"), termFreq);
            }

        }catch (SQLException e){
            printSQLException(e);
            logger.debug(stringBuilder.toString());
            e.getErrorCode();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

        return nodeTermFreqMap;
    }

    public HashMap<Integer, HashMap<String, Integer>> getTermFreqMap(HashSet<Integer> nodeSet) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT page_id, term FROM wikipedia.bsPageTerm WHERE page_id IN (");
        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap = new HashMap<Integer, HashMap<String, Integer>>();

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
            while(rs.next()){

                HashMap<String, Integer> termFreq = gson.fromJson(new String(rs.getBytes("term"),"UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                }.getType());
                nodeTermFreqMap.put(rs.getInt("page_id"), termFreq);
            }

        }catch (SQLException e){
            printSQLException(e);
            logger.debug(stringBuilder.toString());
            e.getErrorCode();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

        return nodeTermFreqMap;
    }

    public double getTF(LinkedList<Integer> x, LinkedList<Integer> y) {
        HashSet<Integer> nodeSet = new HashSet<Integer>();
        double result;
        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap;
        nodeSet.addAll(x);
        nodeSet.addAll(y);
        System.out.println("getTF"+isWiki);
        if(isWiki){
            nodeTermFreqMap = getTermFreqMap(nodeSet);
        }else {
            System.out.println("using patent db");
            nodeTermFreqMap = getPatTermFreqMap(nodeSet);
        }
        HashMap<String, Double> xTermFreq = getTermFreq(x, nodeTermFreqMap);
        HashMap<String, Double> yTermFreq = getTermFreq(y, nodeTermFreqMap);
        result = getCosSimilarity(xTermFreq, yTermFreq);

        return result;

    }

}
