package edu.nd.dsg.wiki.util;

import com.google.gson.Gson;
import edu.nd.dsg.util.ConnectionPool;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class WordCounter extends Finder {
    private static final int MAX_BATCH_INSERT=1;
    private static WordCounter instance = null;
    private static ConnectionPool connectionPool = null;
    private static final Logger logger = LogManager.getLogger(WordCounter.class.getName());
    private int temporaryInsertion = 0;
    boolean isNew = true;
    private StringBuilder sqlStrBuilder = new StringBuilder();
    public static WordCounter getInstance(){
        if(instance == null){
            instance = new WordCounter();
        }
        return instance;
    }

    protected WordCounter(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
                sqlStrBuilder.append("INSERT INTO `wikipedia`.`bsPageTerm` (`page_id`,`term`,`name_space`) VALUES ");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            }
        }
    }

    public static HashMap<String, Integer> getWordCount(String inputStr){
        String escapedText = StringEscapeUtils.escapeHtml4(inputStr);
        String[] words = escapedText.split("[\\W|\\s]+");
        HashMap<String, Integer> wordCount = new HashMap<String, Integer>();
        for(String word : words) {
            if(!word.equals("")) {
                if(!wordCount.containsKey(word)){
                    wordCount.put(word,0);
                }
                wordCount.put(word, wordCount.get(word)+1);
            }

        }
        return wordCount;
    }

    public int flushTFToDB(){

        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;
        int result=1;

        try{
            conn = connectionPool.getConnection();
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            result = st.executeUpdate(sqlStrBuilder.toString());
        }catch (SQLException e){
            printSQLException(e);
            logger.debug(sqlStrBuilder.toString());
            result = e.getErrorCode();
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
                sqlStrBuilder=new StringBuilder();
                sqlStrBuilder.append("INSERT INTO `wikipedia`.`bsPageTerm` (`page_id`,`term`,`name_space`) VALUES ");
                isNew = true;
                temporaryInsertion = 0;
            }catch (SQLException e){
                printSQLException(e);
                logger.debug(sqlStrBuilder.toString());
                result = e.getErrorCode();
            }

        }
        return result;
    }

    public int saveTFToDB(int page_id, int namespace, Object tfMap){
        Gson gson = new Gson();
        String mapJsonStr = gson.toJson(tfMap);
        int result = 1;
        if(temporaryInsertion%MAX_BATCH_INSERT==0 && temporaryInsertion!=0){
            Statement st = null;
            Connection conn = null;
            ResultSet rs = null;

            try{
                conn = connectionPool.getConnection();
                st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                st.setFetchSize(Integer.MIN_VALUE);
                logger.debug(sqlStrBuilder.toString());
                result = st.executeUpdate(sqlStrBuilder.toString());
            }catch (SQLException e){
                printSQLException(e);
                logger.debug(sqlStrBuilder.toString());
                result = e.getErrorCode();
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
                    sqlStrBuilder=new StringBuilder();
                    sqlStrBuilder.append("INSERT INTO `wikipedia`.`bsPageTerm` (`page_id`,`term`,`name_space`) VALUES ");
                    temporaryInsertion = 0;
                    isNew = true;
                }catch (SQLException e){
                    printSQLException(e);
                    logger.debug(sqlStrBuilder.toString());
                    result = e.getErrorCode();
                }

            }
        }
        if(!isNew){
            sqlStrBuilder.append(" , ");
        }
        sqlStrBuilder.append(" (");
        sqlStrBuilder.append(page_id);
        sqlStrBuilder.append(",'");
        sqlStrBuilder.append(mapJsonStr);
        sqlStrBuilder.append("',");
        sqlStrBuilder.append(namespace);
        sqlStrBuilder.append(") ");
        temporaryInsertion++;
        isNew = false;

        return result;
    }

}
