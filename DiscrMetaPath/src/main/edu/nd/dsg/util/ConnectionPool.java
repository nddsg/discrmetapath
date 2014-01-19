package edu.nd.dsg.util;

import com.jolbox.bonecp.BoneCPDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static ConnectionPool instance = null;
    private static BoneCPDataSource ds = null;
    private static final String URL = "jdbc:mysql://dsg1.crc.nd.edu";

    protected ConnectionPool() throws ClassNotFoundException{
        Class.forName("com.mysql.jdbc.Driver");
        ds = new BoneCPDataSource();
        ds.setJdbcUrl(URL);
        ds.setUsername("bshi");
        ds.setPassword("passwd");
        ds.setIdleConnectionTestPeriodInMinutes(15);
        ds.setIdleMaxAgeInMinutes(4);
        ds.setMaxConnectionsPerPartition(30);
        ds.setMinConnectionsPerPartition(1);
        ds.setPoolAvailabilityThreshold(5);
        ds.setPartitionCount(1);
        ds.setAcquireIncrement(3);
        ds.setStatementsCacheSize(50);
        ds.setLazyInit(true);
    }

    public static ConnectionPool getInstance() throws ClassNotFoundException{
        if(instance == null){
            instance = new ConnectionPool();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException{
        return ds.getConnection();
    }


}
