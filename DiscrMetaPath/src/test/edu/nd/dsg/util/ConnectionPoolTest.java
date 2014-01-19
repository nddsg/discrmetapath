package edu.nd.dsg.util;

import org.junit.Test;
import org.junit.Assert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class ConnectionPoolTest {

    @Test
    public void testGetInstance() throws Exception {
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        Assert.assertEquals(connectionPool, ConnectionPool.getInstance());
    }

    @Test
    public void testGetConnection() throws Exception {
        Statement statment = null;
        ResultSet rs = null;
        ConnectionPool connectionPool = ConnectionPool.getInstance();
        LinkedHashSet<Connection> connectionHashSet = new LinkedHashSet<Connection>();
        for(int i=0;i<30;i++){
            connectionHashSet.add(connectionPool.getConnection());
        }
        Assert.assertEquals(30, connectionHashSet.size());
        Connection conn = connectionHashSet.iterator().next();

        statment = conn.createStatement();
        rs = statment.executeQuery("SHOW DATABASES;");
        Assert.assertFalse(rs.wasNull());
        conn.close();
        connectionHashSet.remove(conn);
        Assert.assertEquals(29, connectionHashSet.size());
        connectionHashSet.add(connectionPool.getConnection());
        Assert.assertEquals(30, connectionHashSet.size());
    }
}
