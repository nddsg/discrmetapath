package edu.nd.dsg.wiki;

import edu.nd.dsg.util.ConnectionPool;
import edu.nd.dsg.wiki.util.Finder;
import edu.nd.dsg.wiki.util.PageLoader;
import edu.nd.dsg.wiki.util.WordCounter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

public class BuildTermFrequencyIndex extends Finder{
    public static void main(String[] args) throws ClassNotFoundException {
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        PageLoader pageLoader = PageLoader.getInstance();
//        HashSet<String> termSet = new HashSet<String>();
//        HashMap<String, Integer> documentFrequency = new HashMap<String, Integer>();
        WordCounter wordCounter = WordCounter.getInstance();

        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;
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
                    System.out.println("processing "+pageId);
                }

                String plainText = pageLoader.getPagePlainTextById(pageId);
                HashMap<String, Integer> wordMap = WordCounter.getWordCount(plainText);

                //add all terms into term set
//                termSet.addAll(wordMap.keySet());

                //add to document frequency
//                for(String term : wordMap.keySet()){
//                    if(!documentFrequency.containsKey(term)){
//                        documentFrequency.put(term, 1);
//                    }else{
//                        documentFrequency.put(term, documentFrequency.get(term)+1);
//                    }
//                }

                //save document into database
                wordCounter.saveTFToDB(pageId, 0, wordMap);

            }

//            wordCounter.saveTFToDB(0,0,documentFrequency);
//            wordCounter.saveTFToDB(1,0,termSet);

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

}
