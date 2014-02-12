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
            if(arg.startsWith("-BuildTF")){
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
            if(arg.startsWith("-BuildDF")){
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
