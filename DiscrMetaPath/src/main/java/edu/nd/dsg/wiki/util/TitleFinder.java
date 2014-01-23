package edu.nd.dsg.wiki.util;

import edu.nd.dsg.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class TitleFinder {
    private static TitleFinder instance = null;
    private static ConnectionPool connectionPool = null;
    private static final Logger logger = LogManager.getLogger(TitleFinder.class.getName());

    protected TitleFinder(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            }
        }
    }

    public static TitleFinder getInstance(){
        if(instance == null) {
            instance = new TitleFinder();
        }

        return instance;
    }

    private String getNodeSql(LinkedList<Integer> pathList) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT page_id, page_title FROM wikipedia.page WHERE page_id IN (");
        Iterator<Integer> nodeIter = pathList.iterator();
        while(nodeIter.hasNext()){
            stringBuilder.append(nodeIter.next());
            if(nodeIter.hasNext()){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(");");
        logger.trace("Generate SQL statment "+stringBuilder.toString());

        return stringBuilder.toString();
    }

    public HashMap<Integer, String> getTitle(LinkedList<Integer> pathList){
        String sql = getNodeSql(pathList);
        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;

        HashMap<Integer, String> titleHashMap = new HashMap<Integer, String>();

        try{
            conn = connectionPool.getConnection();
            st = conn.createStatement();

            rs = st.executeQuery(sql);

            while(rs.next()){
                titleHashMap.put(rs.getInt("page_id"), rs.getString("page_title"));
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

        return titleHashMap;

    }

    static private void printSQLException(SQLException e){
        logger.error("Got SQL error "+e.getMessage()+"\n" +
                " SQL statement is \n" +
                e.getSQLState()+
                " Stack trace are \n"+e.getStackTrace().toString());
    }


}
