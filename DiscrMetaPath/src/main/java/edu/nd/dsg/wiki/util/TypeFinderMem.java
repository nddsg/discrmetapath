package edu.nd.dsg.wiki.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import edu.nd.dsg.util.ConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class TypeFinderMem extends TypeFinder {

    private static final int TOTAL_CATE = 70680147;

    private static final Logger logger = LogManager.getLogger(TypeFinderSQL.class.getName());

    private static TypeFinderMem instance = null;
    private static Multimap<Integer, Integer> typeMap = ArrayListMultimap.create();

    public static TypeFinderMem getInstance() {
        if (instance == null) {
            instance = new TypeFinderMem();
        }
        return instance;
    }

    protected TypeFinderMem() {
        ConnectionPool connectionPool = null;
        Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            connectionPool = ConnectionPool.getInstance();
            conn = connectionPool.getConnection();
            st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            st.setFetchSize(Integer.MIN_VALUE);
            rs = st.executeQuery("SELECT id_from, id_to FROM wikipedia.bscategorylinks LIMIT 0," + TOTAL_CATE + ";");

            int count = 0;

            while (rs.next()) {
                count++;
                typeMap.put(rs.getInt("id_from"), rs.getInt("id_to"));
                if (count % 10000 == 0) {
                    logger.info("Loading type info, " + count + "/" + TOTAL_CATE);
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public LinkedHashSet<Integer> getTypeVector(int node) {
        return getTypeVector(node, null);
    }

    @Override
    public LinkedHashSet<Integer> getTypeVector(String node) {
        return getTypeVector(Integer.parseInt(node), null);
    }

    @Override
    public LinkedHashSet<Integer> getTypeVector(String node, Set<Integer> ignoreSet) {
        return getTypeVector(Integer.parseInt(node), ignoreSet);
    }

    @Override
    public LinkedHashSet<Integer> getTypeVector(int node, Set<Integer> ignoreSet) {
        LinkedHashSet<Integer> typeVector = new LinkedHashSet<Integer>();
        PriorityQueue<Integer> frontier = new PriorityQueue<Integer>();
        frontier.add(node);
        Collection<Integer> typeCollection;
        while (frontier.size() > 0) {
            int type = frontier.poll();
            typeCollection = typeMap.get(type);
            for (int cate : typeCollection) {
                if (cate != 0 && (ignoreSet == null || !ignoreSet.contains(cate))) {
                    if (typeVector.add(cate)) {
                        frontier.add(cate);
                    }
                }
            }
        }
        return typeVector;
    }

    public int size() {
        return typeMap.size();
    }
}
