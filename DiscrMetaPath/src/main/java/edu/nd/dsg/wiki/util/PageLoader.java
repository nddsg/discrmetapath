package edu.nd.dsg.wiki.util;

import edu.nd.dsg.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PageLoader {
    private static PageLoader instance = null;
    private static ConnectionPool connectionPool = null;
    private static final Logger logger = LogManager.getLogger(PageLoader.class.getName());

    public static PageLoader getInstance(){
        if(instance == null){
            instance = new PageLoader();
        }
        return instance;
    }

    protected PageLoader(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            }
        }
    }

    static private void printSQLException(SQLException e){
        logger.error("Got SQL error "+e.getMessage()+"\n" +
                " SQL statement is \n" +
                e.getSQLState()+
                " Stack trace are \n"+e.getStackTrace().toString());
    }

    public String getPageTextById(int page_id){
        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;
        String sql = "select page_id, page_latest, CONVERT(text.old_text using UTF8) as pagetext \n" +
                     "FROM wikipedia.page, wikipedia.text, wikipedia.revision\n" +
                     "WHERE page.page_id="+page_id+" and page.page_latest=revision.rev_id and revision.rev_text_id=text.old_id;";
        String result = null;
        try{
            conn = connectionPool.getConnection();
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            rs = st.executeQuery(sql);
            if(rs.next()){
                result = rs.getString("pagetext");
            }

        }catch (SQLException e){
            printSQLException(e);
            result = null;
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
        return result;
    }

}
