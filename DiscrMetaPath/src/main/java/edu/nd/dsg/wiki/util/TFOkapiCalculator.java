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

public class TFOkapiCalculator extends Finder implements TFCalculator{

    private static TFOkapiCalculator instance = null;
    private static ConnectionPool connectionPool = null;
    private IDFOkapiCalculator idfCalculator = null;
    private boolean isWiki;

    private static final double K1 = 1.5;
    private static final double B = 0.75;

    protected TFOkapiCalculator(String path, boolean isWiki){
        this.isWiki = isWiki;
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
                idfCalculator = IDFOkapiCalculator.getInstance(path, isWiki);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static TFOkapiCalculator getInstance(String path, boolean isWiki){
        if(instance == null) {
            instance = new TFOkapiCalculator(path, isWiki);
        }

        return instance;
    }

    private Integer getDocLength(LinkedList<Integer> path, HashMap<Integer, HashMap<String, Integer>> termMap) {
        int sum = 0;

        for(Integer node : path) {
            if(!termMap.containsKey(node)){
                return -1;
            }
            HashMap<String, Integer> map = termMap.get(node);
            for(Integer count : map.values()) {
                sum += count;
            }
        }
        return sum;
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

    private double getBM25Score(HashMap<String, Double> x, int xLen, HashMap<String, Double> y){
        HashSet<String> termSet = new HashSet<String>();
        termSet.addAll(y.keySet());
        double result=0;
        for(String term : termSet) {
            double termFreq = x.containsKey(term) ? x.get(term) : 0;
            double res = idfCalculator.getIDF(term) * (termFreq*(K1+1)) / (termFreq + K1 * (1 - B + B * xLen / idfCalculator.getAvgDocLength(isWiki)));
            if(res < 0) res = 0;
            result += res;
            if(result == Double.NaN || idfCalculator.getIDF(term) !=idfCalculator.getIDF(term)){
                System.out.println("IDF"+idfCalculator.getIDF(term)+" TF "+(termFreq*(K1+1)) / (termFreq + K1 * (1 - B + B * xLen / idfCalculator.getAvgDocLength(isWiki))));
                System.out.println("term "+term+" termFreq"+termFreq);
                System.exit(0);
            }
        }
        return result;
    }

    public LinkedList<Double> getSeqNodeTF(LinkedList<Integer> path){
        LinkedList<Double> res = new LinkedList<Double>();
        HashSet<Integer> nodeSet = new HashSet<Integer>();
        nodeSet.addAll(path);
        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap = isWiki ? getTermFreqMap(nodeSet) : getPatTermFreqMap(nodeSet);

        for(int i = 0; i < path.size()-2; i++){
            LinkedList<Integer> doc = new LinkedList<Integer>();
            LinkedList<Integer> query = new LinkedList<Integer>();
            doc.add(path.get(i));
            query.add(path.get(i+1));
            System.out.println(doc+"  "+query);
            HashMap<String, Double> xTermFreq = getTermFreq(doc, nodeTermFreqMap);
            HashMap<String, Double> yTermFreq = getTermFreq(query, nodeTermFreqMap);
            if(xTermFreq.size() == 0 || yTermFreq.size() == 0) {
                throw new NullPointerException();
            }
            double t = getBM25Score(xTermFreq,getDocLength(doc, nodeTermFreqMap), yTermFreq);
            if(t<0){
                t = Double.MAX_VALUE;
            }
            res.add(t);
        }
        return res;
    }

    @Override
    public LinkedList<Double> getaccumulatedTF(LinkedList<Integer> path) throws NullPointerException{
        LinkedList<Double> res = new LinkedList<Double>();
        HashSet<Integer> nodeSet = new HashSet<Integer>();
        nodeSet.addAll(path);
        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap = isWiki ? getTermFreqMap(nodeSet) : getPatTermFreqMap(nodeSet);
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
                System.out.println("x"+accumulatePath.toString()+" y"+nextNode.toString());
                throw new NullPointerException();
            }
            res.add(getBM25Score(xTermFreq,getDocLength(accumulatePath, nodeTermFreqMap), yTermFreq));

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

    @Override
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

    @Override
    public double getTF(LinkedList<Integer> x, LinkedList<Integer> y) {
        HashSet<Integer> nodeSet = new HashSet<Integer>();
        double result;

        nodeSet.addAll(x);
        nodeSet.addAll(y);

        HashMap<Integer, HashMap<String, Integer>> nodeTermFreqMap = isWiki ? getTermFreqMap(nodeSet) : getPatTermFreqMap(nodeSet);
        HashMap<String, Double> xTermFreq = getTermFreq(x, nodeTermFreqMap);
        HashMap<String, Double> yTermFreq = getTermFreq(y, nodeTermFreqMap);
        result = getBM25Score(xTermFreq, getDocLength(x, nodeTermFreqMap), yTermFreq);

        return result;

    }

}
