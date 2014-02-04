package edu.nd.dsg.wiki.util;

import edu.nd.dsg.util.ConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class IdFinder extends Finder {
    private static IdFinder instance = null;
    private static ConnectionPool connectionPool = null;

    protected IdFinder(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            }
        }
    }

    public static IdFinder getInstance(){
        if(instance == null) {
            instance = new IdFinder();
        }

        return instance;
    }

    public int getIdByTitle(String title){
        String[] str = {title};
        int[] arry = getIdByTitle(str);
        return arry != null ? arry[0] : 0;
    }

    public int[] getIdByTitle(String[] titleArry){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT page_title, page_id FROM wikipedia.page WHERE page_title IN (");
        for(String str : titleArry){
            stringBuilder.append("'");
            stringBuilder.append(str);
            stringBuilder.append("'");
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append(") AND page.page_namespace=0");
        logger.debug("SQL "+stringBuilder.toString());
        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;

        int[] arry = null;
        HashMap<String, Integer> titleIdMap = new HashMap<String, Integer>();

        try{
            conn = connectionPool.getConnection();
            st = conn.createStatement();

            rs = st.executeQuery(stringBuilder.toString());

            while(rs.next()){
                titleIdMap.put(rs.getString("page_title"),rs.getInt("page_id"));
            }
            logger.trace(titleIdMap);

            arry = new int[titleArry.length];
            for(int i = 0; i < titleArry.length; i++){
                logger.trace("i="+i+" title="+titleArry[i]+" id="+titleIdMap.get(titleArry[i]));
                arry[i] = titleIdMap.get(titleArry[i]);
            }

        }catch (SQLException e){
            printSQLException(e);
        }catch (NullPointerException e){
            arry = null;
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
        return arry;
    }

}
