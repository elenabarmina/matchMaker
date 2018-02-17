package com.pechen.matchmaker.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pechen on 18.02.2018.
 */
public class DataSource {

    final static Logger logger = LoggerFactory.getLogger(DataSource.class);

    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:~/apTest";

    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection getConnection(){
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            logger.debug("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            return conn;
        } catch(SQLException se) {
            se.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if( conn != null ) conn.close();
            } catch(SQLException se){
                se.printStackTrace();
            }
        }
        return null;
    }

    public List<User> getAllUsersInMatchQueue() throws SQLException {
        String sql = "SELECT * FROM T_MATCH_QUEUE";
        return getUsersListByStatement(sql);
    }

    public List<User> getUsersInMatchQueueByTeamId(Long teamId) throws SQLException {
        String sql = "SELECT * FROM T_MATCH_QUEUE WHERE T_MATCH_QUEUE.TEAM_ID = " + teamId;
        return getUsersListByStatement(sql);
    }

    private List<User> getUsersListByStatement(String sqlStatement) throws SQLException {
        Connection connection = getConnection();
        if (connection == null) return null;

        Statement stmt = null;
        List<User> usersSet = new ArrayList<>();

        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlStatement);

        while(rs.next()) {
            User newUser = new User(rs.getLong("USER_ID"),
                    rs.getInt("RANK"),
                    rs.getLong("LAST_REGISTRATION_TIME"),
                    rs.getLong("TEAM_ID"),
                    rs.getBoolean("IS_COMPLETED"));
            usersSet.add(newUser);
        }
        rs.close();
        connection.close();

        return usersSet;
    }

    public List<Long> getTeamIdsInProcessing() throws SQLException {
        Connection connection = getConnection();
        if (connection == null) return null;

        Statement stmt = null;
        List<Long> matchIds = new ArrayList<>();

        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ID " +
                                                "FROM T_TEAM_LIST " +
                                                "WHERE T_TEAM_LIST.MATCH_CREATION_TIME = 0 " +
                                                "AND T_TEAM_LIST.ID <> 0" +
                                                "ORDER BY T_TEAM_LIST.USERS_COUNT DESC");

        while(rs.next()) {
            matchIds.add(rs.getLong("ID"));
        }
        rs.close();
        connection.close();

        return matchIds;
    }
}
