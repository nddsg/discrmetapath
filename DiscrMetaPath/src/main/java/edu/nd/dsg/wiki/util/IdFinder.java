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

    public String getIdStrByTitle(String title){
        String[] str = {title};
        int[] arry = getIdByTitle(str);
        return arry != null ? ((Integer)arry[0]).toString() : null;
    }

    public int[] getIdByTitle(String[] titleArry){
        String[] filteredTitleArry;
        int size=0;
        for(String s : titleArry){
            if(!s.equals("") && s != null){
                size++;
            }
        }
        filteredTitleArry = new String[size];
        size=0;
        for(String s : titleArry){
            if(!s.equals("") && s != null){
                filteredTitleArry[size++] = s;
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT DISTINCT page_title, page_id FROM wikipedia.page WHERE page_title IN (");
        for(String str : filteredTitleArry){
            stringBuilder.append("'");
            stringBuilder.append(str.replace("'","\\'"));
            stringBuilder.append("'");
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append(") ORDER BY page_namespace ASC");
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
                if(!titleIdMap.containsKey(rs.getString("page_title"))){
                    titleIdMap.put(rs.getString("page_title"),rs.getInt("page_id"));
                }
            }
            logger.trace(titleIdMap);

            arry = new int[filteredTitleArry.length];
            for(int i = 0; i < filteredTitleArry.length; i++){
                logger.trace("i="+i+" title="+filteredTitleArry[i]+" id="+titleIdMap.get(filteredTitleArry[i]));
                arry[i] = titleIdMap.get(filteredTitleArry[i]);
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
        logger.trace(arry);
        return arry;
    }

    public String getIdStrByTitle(String[] titleArry){
        int[] intArry = getIdByTitle(titleArry);
        StringBuilder stringBuilder = new StringBuilder();
        try{
            for(int id : intArry){
                stringBuilder.append(id);
                stringBuilder.append(",");
            }
        }catch (NullPointerException e){
            return "";
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        return stringBuilder.toString();
    }
}
