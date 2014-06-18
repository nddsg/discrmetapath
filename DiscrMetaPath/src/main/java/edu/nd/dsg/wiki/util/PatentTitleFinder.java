package edu.nd.dsg.wiki.util;

import edu.nd.dsg.util.ConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PatentTitleFinder extends Finder {
    private static PatentTitleFinder instance = null;
    private static ConnectionPool connectionPool = null;

    protected PatentTitleFinder(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            }
        }
    }

    public static PatentTitleFinder getInstance(){
        if(instance == null) {
            instance = new PatentTitleFinder();
        }

        return instance;
    }

    private String getNodeSql(Set<Integer> pathList) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT patent, title FROM patents.titles WHERE patent IN (");
        Iterator<Integer> nodeIter = pathList.iterator();
        while(nodeIter.hasNext()){
            stringBuilder.append(nodeIter.next());
            if(nodeIter.hasNext()){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(");");
        logger.trace("Generate SQL statement "+stringBuilder.toString());

        return stringBuilder.toString();
    }

    private String getNodeClassSql(Set<Integer> pathList) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT title, patent FROM patents.Pat63_99, patents.Classes WHERE patent IN (");
        Iterator<Integer> nodeIter = pathList.iterator();
        while(nodeIter.hasNext()){
            stringBuilder.append(nodeIter.next());
            if(nodeIter.hasNext()){
                stringBuilder.append(",");
            }
        }
        stringBuilder.append(") AND Pat63_99.nclass = Classes.class;");
        logger.trace("Generate SQL statement "+stringBuilder.toString());

        return stringBuilder.toString();
    }

    public HashMap<Integer, String> getTitle(Set<Integer> pathList){
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
                titleHashMap.put(rs.getInt("patent"), rs.getString("title"));
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

        HashSet<Integer> missingNodes = new HashSet<Integer>();

        for(int node : pathList){
            if(!titleHashMap.containsKey(node)){
                missingNodes.add(node);
            }
        }

        if(missingNodes.size()>0){
            try{
                sql = getNodeClassSql(missingNodes);
                conn = connectionPool.getConnection();
                st = conn.createStatement();

                rs = st.executeQuery(sql);

                while(rs.next()){
                    titleHashMap.put(rs.getInt("patent"), rs.getString("title").replaceAll("[^A-Za-z0-9 ]", ""));
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

        return titleHashMap;

    }
}
