package edu.nd.dsg.wiki;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import edu.nd.dsg.util.ConnectionPool;
import edu.nd.dsg.wiki.util.Finder;
import edu.nd.dsg.wiki.util.PageLoader;
import edu.nd.dsg.wiki.util.WordCounter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class BuildTermFrequencyIndex extends Finder{
    public static void main(String[] args) throws ClassNotFoundException {
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        PageLoader pageLoader = PageLoader.getInstance();
        HashMap<String, Integer> documentFrequency = new HashMap<String, Integer>();
        WordCounter wordCounter = WordCounter.getInstance();

        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;

        logger.info(args);

        for(String arg : args) {

            if(arg.startsWith("-GetPatentAvg")){
                String sql = "SELECT detd FROM patents.bsPageTerm;";
                HashMap<String, Integer> abstMap = new HashMap<String, Integer>();
                HashMap<String, Integer> bsumMap = new HashMap<String, Integer>();
                HashMap<String, Integer> drwdMap = new HashMap<String, Integer>();
                HashMap<String, Integer> detdMap = new HashMap<String, Integer>();
                HashMap<String, Integer> clmsMap = new HashMap<String, Integer>();

                int cnt=0;
                Gson gson = new Gson();
                Long words = 0l;
                double avg = 0;
                try{
                    logger.info("start calc avg len of patent document...");
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        if(cnt % 100 == 0){
                            logger.info("processing "+cnt);
                        }
                        HashMap<String, Integer> terms = gson.fromJson(new String(rs.getBytes("detd"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(Integer v : terms.values()){
                            words += v;
                        }
                        cnt++;
                    }

                    avg  = words / 2064267;

                }catch (SQLException e){
                    printSQLException(e);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {

                    System.out.println("average patent length "+avg);
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
                    }

                }
            }
            if(arg.startsWith("-GetWikiAvg")){
                String sql = "SELECT page_id, term FROM wikipedia.bsPageTerm;";
                int cnt=0;
                Long words = 0l;
                double avg = 0;
                Gson gson = new Gson();
                try{
                    logger.info("start building document frequency...");
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        if(cnt % 100 == 0){
                            logger.info("processing "+cnt);
                        }
                        HashMap<String, Integer> terms = gson.fromJson(new String(rs.getBytes("term"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(Integer v : terms.values()){
                            words +=v;
                        }
                        cnt++;
                    }

                    avg = words / 10276554;

                }catch (SQLException e){
                    printSQLException(e);
                }catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("average wikipedia length:"+avg);
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
                    }

                }
            }

            if(arg.startsWith("-BuildPatentTFRev")){
                String sql = "SELECT * FROM patents.text LIMIT 1032133, 2064267;";
                int pageId;
                try{
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        pageId = rs.getInt("patent");

                        if(pageId % 100==0){
                            System.out.println("processing "+pageId+"/2064267");
                        }

                        HashMap<String, HashMap<String, Integer>> patentWordMap = new HashMap<String, HashMap<String, Integer>>();
                        patentWordMap.put("abst", WordCounter.getWordCount(rs.getString("abst")));
                        patentWordMap.put("bsum",WordCounter.getWordCount(rs.getString("bsum")));
                        patentWordMap.put("drwd",WordCounter.getWordCount(rs.getString("drwd")));
                        patentWordMap.put("detd",WordCounter.getWordCount(rs.getString("detd")));
                        patentWordMap.put("clms",WordCounter.getWordCount(rs.getString("clms")));

                        //save document into database
                        wordCounter.savePatentTFToDB(pageId, patentWordMap);

                    }

                }catch (SQLException e){
                    printSQLException(e);
                }finally {
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
                    }

                }
            }

            if(arg.startsWith("-BuildPatentTF")){
                String sql = "SELECT * FROM patents.text;";
                int pageId;
                try{
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        pageId = rs.getInt("patent");

                        if(pageId % 100==0){
                            System.out.println("processing "+pageId+"/2064267");
                        }

                        HashMap<String, HashMap<String, Integer>> patentWordMap = new HashMap<String, HashMap<String, Integer>>();
                        patentWordMap.put("abst", WordCounter.getWordCount(rs.getString("abst")));
                        patentWordMap.put("bsum",WordCounter.getWordCount(rs.getString("bsum")));
                        patentWordMap.put("drwd",WordCounter.getWordCount(rs.getString("drwd")));
                        patentWordMap.put("detd",WordCounter.getWordCount(rs.getString("detd")));
                        patentWordMap.put("clms",WordCounter.getWordCount(rs.getString("clms")));

                        //save document into database
                        wordCounter.savePatentTFToDB(pageId, patentWordMap);

                    }

                }catch (SQLException e){
                    printSQLException(e);
                }finally {
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
                    }

                }
            }



            if(arg.startsWith("-BuildWikiTF")){
                String sql = "SELECT page_id FROM wikipedia.page WHERE page_namespace=0 AND page.page_id NOT IN (SELECT page_id from wikipedia.bsPageTerm);";
                int pageId;
                try{
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        pageId = rs.getInt("page_id");

                        if(pageId % 100==0){
                            System.out.println("processing "+pageId+"/10276554");
                        }

                        String plainText = pageLoader.getPagePlainTextById(pageId);
                        HashMap<String, Integer> wordMap = WordCounter.getWordCount(plainText);

                        //save document into database
                        wordCounter.saveTFToDB(pageId, 0, wordMap);

                    }

                }catch (SQLException e){
                    printSQLException(e);
                }finally {
                    try{
                        wordCounter.flushTFToDB();
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
                    }

                }
            }

            if(arg.startsWith("-BuildPatentDF")){
                String sql = "SELECT * FROM patents.bsPageTerm;";
                HashMap<String, Integer> abstMap = new HashMap<String, Integer>();
                HashMap<String, Integer> bsumMap = new HashMap<String, Integer>();
                HashMap<String, Integer> drwdMap = new HashMap<String, Integer>();
                HashMap<String, Integer> detdMap = new HashMap<String, Integer>();
                HashMap<String, Integer> clmsMap = new HashMap<String, Integer>();

                int cnt=0;
                Gson gson = new Gson();
                try{
                    logger.info("start building document frequency...");
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        if(cnt % 100 == 0){
                            logger.info("processing "+cnt);
                        }
                        HashMap<String, Integer> terms = gson.fromJson(new String(rs.getBytes("abst"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(String term : terms.keySet()){
                            if(!abstMap.containsKey(term)){
                                abstMap.put(term, 0);
                            }
                            abstMap.put(term, abstMap.get(term)+1);
                        }
                        terms = gson.fromJson(new String(rs.getBytes("bsum"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(String term : terms.keySet()){
                            if(!bsumMap.containsKey(term)){
                                bsumMap.put(term, 0);
                            }
                            bsumMap.put(term, bsumMap.get(term)+1);
                        }
                        terms = gson.fromJson(new String(rs.getBytes("drwd"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(String term : terms.keySet()){
                            if(!drwdMap.containsKey(term)){
                                drwdMap.put(term, 0);
                            }
                            drwdMap.put(term, drwdMap.get(term)+1);
                        }
                        terms = gson.fromJson(new String(rs.getBytes("detd"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(String term : terms.keySet()){
                            if(!detdMap.containsKey(term)){
                                detdMap.put(term, 0);
                            }
                            detdMap.put(term, detdMap.get(term)+1);
                        }
                        terms = gson.fromJson(new String(rs.getBytes("clms"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(String term : terms.keySet()){
                            if(!clmsMap.containsKey(term)){
                                clmsMap.put(term, 0);
                            }
                            clmsMap.put(term, clmsMap.get(term)+1);
                        }
                        cnt++;
                    }
                    PrintWriter writer;
                    String dfJson;

                    dfJson = gson.toJson(abstMap);
                    writer = new PrintWriter("./data/abst.json", "UTF-8");
                    writer.print(dfJson);
                    writer.close();

                    dfJson = gson.toJson(bsumMap);
                    writer = new PrintWriter("./data/bsum.json", "UTF-8");
                    writer.print(dfJson);
                    writer.close();

                    dfJson = gson.toJson(drwdMap);
                    writer = new PrintWriter("./data/drwd.json", "UTF-8");
                    writer.print(dfJson);
                    writer.close();

                    dfJson = gson.toJson(detdMap);
                    writer = new PrintWriter("./data/detd.json", "UTF-8");
                    writer.print(dfJson);
                    writer.close();

                    dfJson = gson.toJson(clmsMap);
                    writer = new PrintWriter("./data/clms.json", "UTF-8");
                    writer.print(dfJson);
                    writer.close();

                }catch (SQLException e){
                    printSQLException(e);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
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
                    }

                }
            }

            if(arg.startsWith("-BuildWikiDF")){
                String sql = "SELECT page_id, term FROM wikipedia.bsPageTerm;";
                int cnt=0;
                Gson gson = new Gson();
                try{
                    logger.info("start building document frequency...");
                    conn = connectionPool.getConnection();
                    st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    st.setFetchSize(Integer.MIN_VALUE);
                    rs = st.executeQuery(sql);
                    while(rs.next()){
                        if(cnt % 100 == 0){
                            logger.info("processing "+cnt);
                        }
                        HashMap<String, Integer> terms = gson.fromJson(new String(rs.getBytes("term"), "UTF-8"), new TypeToken<HashMap<String, Integer>>() {
                        }.getType());
                        for(String term : terms.keySet()){
                            if(!documentFrequency.containsKey(term)){
                                documentFrequency.put(term, 0);
                            }
                            documentFrequency.put(term, documentFrequency.get(term)+1);
                        }
                        cnt++;
                    }

                    String dfJson = gson.toJson(documentFrequency);
                    PrintWriter writer = new PrintWriter("df.json", "UTF-8");
                    writer.print(dfJson);
                    writer.close();

                }catch (SQLException e){
                    printSQLException(e);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
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
                    }

                }
            }
        }


    }

}
