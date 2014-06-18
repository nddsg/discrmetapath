package edu.nd.dsg.wiki.util;

import edu.nd.dsg.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class TypeFinderSQL extends TypeFinder {
    private static TypeFinderSQL instance = null;
    private static ConnectionPool connectionPool = null;

    public static TypeFinderSQL getInstance(){
        if(instance == null){
            instance = new TypeFinderSQL();
        }
        return instance;
    }

    protected TypeFinderSQL(){
        if(connectionPool==null){
            try {
                connectionPool = ConnectionPool.getInstance();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                connectionPool = null;
            }
        }
    }

    /**
     * Generate SQL statement for category querying
     * @param nodes Find all categories that directly connected to **nodes**
     * @return String SQL statement to query
     */
    static private String getTypeVectorSql(HashSet<Integer> nodes){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT " +
                " id_from, id_to " +
                " FROM wikipedia.bscategorylinks " +
                " WHERE id_from IN (");

        Iterator<Integer> nodeIter = nodes.iterator();
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

    static private String getTypeVectorSql(int node){
        HashSet<Integer> nodes = new HashSet<Integer>();
        nodes.add(node);
        return getTypeVectorSql(nodes);
    }

    /**
     * Generate type(category) vector for node
     * @param node start node, this function will find out all categories that connected to **node**
     * @return LinkedHashSet containing all categories that belongs to **node**
     */
    public LinkedHashSet<Integer> getTypeVector(int node){
        return getTypeVector(node, new HashSet<Integer>());
    }

    public LinkedHashSet<Integer> getTypeVector(String node){
        return getTypeVector(Integer.parseInt(node), new HashSet<Integer>());
    }

    public LinkedHashSet<Integer> getTypeVector(String node, Set<Integer> ignoreSet){
        return getTypeVector(Integer.parseInt(node), ignoreSet);
    }

    /**
     * Generate type(category) vector for node
     * @param node start node, this function will find out all categories that connected to **node**
     * @param ignoreSet HashSet that containing all types(categories) that could ignore.
     *                  Usually used for speed up by ignoring duplicate categories.
     * @return LinkedHashSet containing all categories that belongs to **node**
     */
    public LinkedHashSet<Integer> getTypeVector(int node, Set<Integer> ignoreSet){

        Statement st = null;
        Connection conn = null;
        ResultSet rs = null;

        LinkedHashSet<Integer> typeVector = new LinkedHashSet<Integer>();

        try{
            HashSet<Integer> categorySet = new HashSet<Integer>();
            String sql;
            conn = connectionPool.getConnection();
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            categorySet.add(node);

            while(categorySet.size()>0){

                sql = getTypeVectorSql(categorySet);
                rs = st.executeQuery(sql);
                categorySet.clear();

                while(rs.next()){
                    if(rs.getInt("id_to")!=0 && !ignoreSet.contains(rs.getInt("id_to"))){
                        if(typeVector.add(rs.getInt("id_to"))){
                            categorySet.add(rs.getInt("id_to"));
                        }
                    }
                }

                logger.debug("Current typeVector size is "+typeVector.size());
                logger.trace("Current typeVector is "+typeVector.toString());
                logger.debug("Next iteration size " + categorySet.size());
                logger.trace("Next Iteration set "+categorySet.toString());

            }

        }catch (SQLException e){
            printSQLException(e);
            typeVector = null;
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
        return typeVector;
    }

}