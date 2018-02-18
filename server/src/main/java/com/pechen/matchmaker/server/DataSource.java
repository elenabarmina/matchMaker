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

    public static DataSource getInstance(){
        return new DataSource();
    }

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

    public List<UserInMatchQueue> getAllUsersInMatchQueue() {
        String sql = "SELECT * FROM T_MATCH_QUEUE";
        return getUsersListByStatement(sql);
    }

    public List<UserInMatchQueue> getUsersInMatchQueueByTeamId(Long teamId) {
        String sql = "SELECT * FROM T_MATCH_QUEUE WHERE T_MATCH_QUEUE.TEAM_ID = " + teamId;
        return getUsersListByStatement(sql);
    }

    private List<UserInMatchQueue> getUsersListByStatement(String sqlStatement) {
        Connection connection = getConnection();
        List<UserInMatchQueue> usersSet = new ArrayList<>();

        if (connection == null) return usersSet;

        Statement stmt = null;
        ResultSet rs = null;

        try {
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sqlStatement);

        while(rs.next()) {
            UserInMatchQueue newUser = new UserInMatchQueue(rs.getLong("USER_ID"),
                    rs.getInt("RANK"),
                    rs.getLong("LAST_REGISTRATION_TIME"),
                    rs.getLong("TEAM_ID"),
                    rs.getBoolean("IS_COMPLETED"));
            usersSet.add(newUser);
        }
        rs.close();
        connection.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        try {
            rs.close();
            connection.close();
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return usersSet;
    }

    public List<Long> getTeamIdsInProcessing() {
        return
                getIdsBySql("SELECT ID " +
                        "FROM T_TEAM_LIST " +
                        "WHERE T_TEAM_LIST.MATCH_CREATION_TIME = 0 " +
                        "AND T_TEAM_LIST.USERS_COUNT > 0" +
                        "ORDER BY T_TEAM_LIST.USERS_COUNT DESC", "ID");
    }

    public List<Long> getSingleTeamIdsInProcessing() {
        return
                getIdsBySql("SELECT ID " +
                        "FROM T_TEAM_LIST " +
                        "WHERE T_TEAM_LIST.MATCH_CREATION_TIME = 0 " +
                        "AND T_TEAM_LIST.USERS_COUNT == 1" +
                        "ORDER BY T_TEAM_LIST.USERS_COUNT ASC", "ID");
    }

    public List<Long> getIdsBySql(String sql, String resultColumnName) {
        Connection connection = getConnection();
        if (connection == null) return null;

        Statement stmt = null;
        List<Long> matchIds = new ArrayList<>();

        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()) {
                matchIds.add(rs.getLong(resultColumnName));
            }
        } catch (SQLException e) {
            return new ArrayList<>();
        }

        try {
            rs.close();
            connection.close();
        } catch (SQLException e) {
            return new ArrayList<>();
        }

        return matchIds;
    }

    public Team getTeamById(Long id) {
        Connection connection = getConnection();
        if (connection == null) return null;

        Statement stmt = null;
        List<Long> matchIds = new ArrayList<>();

        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * " +
                    "FROM T_TEAM_LIST " +
                    "WHERE T_TEAM_LIST.ID = " + id);

            while (rs.next()) {
                return new Team(rs.getLong("ID"), rs.getInt(""), rs.getBoolean(""));
            }
        } catch (SQLException e) {
            logger.error("getTeamById exception: " + "id=" + id + " " + e.getMessage());
        }

        try {
            rs.close();
            connection.close();
        } catch (SQLException e) {
            logger.error("getTeamById close connection exception: " + e.getMessage());
        }

        return null;
    }

    public Boolean addUserIfTeamNotChanged(Long teamId, Integer oldUsersCount, UserInMatchQueue user) {
        Connection connection = getConnection();
        if (connection == null) return null;

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = connection.createStatement();
            String sql = "SELECT COUNT(*) RESULT FROM T_TEAM_LIST WHERE ID=" + teamId + "AND USERS_COUNT=" + oldUsersCount;
            rs = stmt.executeQuery(sql);

            boolean isTeamNotChanged = false;
            while (rs.next()) {
                isTeamNotChanged = rs.getInt("RESULT") > 0;
            }

            if (isTeamNotChanged) {
                sql = "MERGE INTO T_MATCH_QUEUE (USER_ID, TEAM_ID) KEY(USER_ID) VALUES (" + user.getUserId() + "," + teamId + ")";
                stmt.executeQuery(sql);
                sql = "MERGE INTO T_TEAM_LIST  (ID, USERS_COUNT) KEY(ID) VALUES (" + teamId + "," + oldUsersCount + 1 + ")";
                stmt.executeQuery(sql);
                return true;
            }
        }catch(SQLException e){
            logger.error("addUserIfTeamNotChanged close connection exception: "
                    + "teamId=" + teamId
                    + "userId=" + user.getUserId()
                    + " " + e.getMessage());
        }

        try {
            rs.close();
            connection.close();
        } catch (SQLException e) {
            logger.error("addUserIfTeamNotChanged close connection exception: " + e.getMessage());
        }

        return false;
    }

    public Long createNewTeam() throws SQLException {
        Connection connection = getConnection();
        if (connection == null) return null;

        Long newTeamId = 0L;

        Statement stmt = null;
        List<Long> matchIds = new ArrayList<>();

        stmt = connection.createStatement();
        String sql = "INSERT INTO T_TEAM_LIST (USERS_COUNT) VALUES (0)";
        stmt.executeQuery(sql);

        sql = "SELECT ID FROM T_TEAM_LIST WHERE USERS_COUNT = 0";

        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            newTeamId = rs.getLong("ID");
        }

        rs.close();
        connection.close();

        return newTeamId;
    }
}
