package edu.nd.dsg.wiki.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

abstract public class Finder {
    protected static final Logger logger = LogManager.getLogger(TitleFinder.class.getName());

    static protected void printSQLException(SQLException e){
        logger.error("Got SQL error "+e.getMessage()+"\n" +
                " SQL statement is \n" +
                e.getSQLState()+
                " Stack trace are \n"+e.getStackTrace().toString());
    }
}
