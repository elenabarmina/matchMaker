package com.pechen.matchmaker.server;

import java.sql.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pechen on 18.02.2018.
 */
public class DataSource {

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
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            return conn;
        } catch(SQLException se) {
            se.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<UserInMatchQueue> getUsersInMatchQueueByTeamId(Long teamId) {

        List<UserInMatchQueue> usersSet = new ArrayList<>();

        try (Connection connection = getConnection();) {
            if (connection == null) return usersSet;

            try (PreparedStatement stmt = connection
                    .prepareStatement("SELECT * FROM T_MATCH_QUEUE WHERE T_MATCH_QUEUE.TEAM_ID = ?")) {

                stmt.setLong(1, teamId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UserInMatchQueue newUser = new UserInMatchQueue(rs.getLong("USER_ID"),
                                rs.getInt("RANK"),
                                rs.getLong("LAST_REGISTRATION_TIME"),
                                rs.getLong("TEAM_ID"),
                                rs.getBoolean("IS_COMPLETED"));
                        usersSet.add(newUser);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("getUsersInMatchQueueByTeamId exception: " + e.getMessage());
        }

        return usersSet;
    }

    public List<Long> getTeamIdsInProcessing() {
        return
                getIdsBySql("SELECT ID " +
                        "FROM T_TEAM_LIST " +
                        "WHERE T_TEAM_LIST.MATCH_CREATION_TIME = 0 " +
                        "AND T_TEAM_LIST.USERS_COUNT > 0 " +
                        "AND T_TEAM_LIST.USERS_COUNT < 8 " +
                        "ORDER BY T_TEAM_LIST.USERS_COUNT DESC", "ID");
    }

    public List<Long> getSingleTeamIdsInProcessing() {
        return
                getIdsBySql("SELECT ID " +
                        "FROM T_TEAM_LIST " +
                        "WHERE T_TEAM_LIST.MATCH_CREATION_TIME = 0 " +
                        "AND T_TEAM_LIST.USERS_COUNT = 1 " +
                        "ORDER BY T_TEAM_LIST.USERS_COUNT ASC", "ID");
    }

    private List<Long> getIdsBySql(String sql, String resultColumnName) {

        List<Long> matchIds = new ArrayList<>();

        try (Connection connection = getConnection();) {
            if (connection == null) return null;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        matchIds.add(rs.getLong(resultColumnName));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("getIdsBySql error: " + e.getMessage());
            return new ArrayList<>();
        }

        return matchIds;
    }

    public Team getTeamById(Long id) {

        try (Connection connection = getConnection();){
            if (connection == null) return null;

            try (PreparedStatement stmt = connection.prepareStatement("SELECT USERS_COUNT " +
                    "FROM T_TEAM_LIST " +
                    "WHERE T_TEAM_LIST.ID = " + id +
                    "AND T_TEAM_LIST.USERS_COUNT < 8")) {

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Integer usersCount = rs.getInt(1);
                        return new Team(id, usersCount);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("getTeamById exception: " + "id=" + id + " " + e.getMessage());
        }

        return null;
    }

    public Boolean createNewTeamWithSingleUser(UserInMatchQueue user) {

        Long newTeamId = null;

        try (Connection connection = getConnection();){
            if (connection == null) return null;
            connection.setAutoCommit(true);

            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO T_TEAM_LIST (USERS_COUNT) VALUES (0)")) {
                int rows = stmt.executeUpdate();
                connection.commit();
            }

            try (PreparedStatement stmt = connection.prepareStatement("SELECT ID FROM T_TEAM_LIST WHERE USERS_COUNT = 0")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        newTeamId = rs.getLong(1);
                    }
                }
            }

            if (newTeamId != null){

                try (PreparedStatement stmt = connection
                        .prepareStatement("INSERT INTO T_MATCH_QUEUE (USER_ID, TEAM_ID, RANK, LAST_REGISTRATION_TIME) VALUES (?,?,?,?)")) {
                    stmt.setLong(1, user.getUserId());
                    stmt.setLong(2, newTeamId);
                    stmt.setInt(3, user.getRank());
                    stmt.setLong(4,  user.getRegistrationTime());
                    int rows = stmt.executeUpdate();
                    connection.commit();
                }

                try (PreparedStatement stmt = connection
                        .prepareStatement("MERGE INTO T_TEAM_LIST (ID, USERS_COUNT) KEY(ID) VALUES (?,?)")) {
                    stmt.setLong(1, newTeamId);
                    stmt.setInt(2, 1);
                    int rows = stmt.executeUpdate();
                    connection.commit();
                }
            }
        } catch (SQLException e){
            System.out.println("createNewTeam exception: "
                    + " " + e.getMessage());
        }

        return newTeamId != null;
    }

    public Boolean addUserIfTeamNotChanged(Long teamId, Integer oldUsersCount, UserInMatchQueue user, Long oldUserTeamId) {

        Integer newUsersCount = 0;

        try (Connection connection = getConnection();){
            if (connection == null) return null;

            boolean isTeamNotChanged = false;

            try (PreparedStatement stmt = connection
                    .prepareStatement("SELECT COUNT(*) RESULT FROM T_TEAM_LIST WHERE ID = ? AND USERS_COUNT = ?")) {
                stmt.setLong(1, teamId);
                stmt.setInt(2, oldUsersCount);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        isTeamNotChanged = rs.getInt("RESULT") > 0;
                    }
                }
            }

            if (isTeamNotChanged) {
                try (PreparedStatement stmt = connection
                        .prepareStatement("MERGE INTO T_MATCH_QUEUE (USER_ID, TEAM_ID, RANK, LAST_REGISTRATION_TIME) KEY(USER_ID) VALUES (?,?,?,?)")) {
                    stmt.setLong(1, user.getUserId());
                    stmt.setLong(2, teamId);
                    stmt.setLong(3, user.getRank());
                    stmt.setLong(4, user.getRegistrationTime());
                    int rows = stmt.executeUpdate();
                    connection.commit();
                }
                System.out.println("user: " + user.getUserId() + " added to team: " + teamId);

                newUsersCount = oldUsersCount + 1;
                try (PreparedStatement stmt = connection
                        .prepareStatement("MERGE INTO T_TEAM_LIST  (ID, USERS_COUNT) KEY(ID) VALUES (?,?)")) {
                    stmt.setLong(1, teamId);
                    stmt.setInt(2, oldUsersCount+1);
                    int rows = stmt.executeUpdate();
                    connection.commit();
                }
                System.out.println(teamId + "members count: " + newUsersCount);

                if (oldUserTeamId != null){
                    try (PreparedStatement stmt = connection
                            .prepareStatement("DELETE FROM T_TEAM_LIST WHERE ID = ?")) {
                        stmt.setLong(1, oldUserTeamId);
                        int rows = stmt.executeUpdate();
                        connection.commit();
                    }
                }
            }
        }catch(SQLException e){
            System.out.println("addUserIfTeamNotChanged exception: "
                    + "teamId=" + teamId
                    + "userId=" + user.getUserId()
                    + " " + e.getMessage());
        }

        if (newUsersCount == 8){
            return makeMatch(teamId);
        }

        return true;
    }

    public Boolean deleteOldTeam(Long teamId) {

        try (Connection connection = getConnection();){
            if (connection == null) return null;

            try (PreparedStatement stmt = connection
                    .prepareStatement("DELETE FROM T_TEAM_LIST WHERE ID = ?")) {
                stmt.setLong(1, teamId);
                int rows = stmt.executeUpdate();
                connection.commit();
            }
        }
        catch(SQLException e){
            System.out.println("deleteOldTeam exception: "
                    + "teamId=" + teamId
                    + " " + e.getMessage());
        }

        System.out.println(teamId + " deleted");

        return true;
    }

    public Boolean makeMatch(Long teamId){
        try (Connection connection = getConnection();){
            if (connection == null) return null;

            Long creationTime = (new Date()).getTime();
            try (PreparedStatement stmt = connection
                    .prepareStatement("MERGE INTO T_TEAM_LIST  (ID, MATCH_CREATION_TIME) KEY(ID) VALUES (?,?)")) {
                stmt.setLong(1, teamId);
                stmt.setLong(2, creationTime);
                int rows = stmt.executeUpdate();
                connection.commit();
            }
            System.out.println("match id: " + teamId + " completed in " + creationTime);

            List<UserInMatchQueue> usersSet = new ArrayList<>();

            System.out.println("users:");
            try (PreparedStatement stmt = connection
                    .prepareStatement("SELECT * FROM T_MATCH_QUEUE WHERE T_MATCH_QUEUE.TEAM_ID = ?")) {

                stmt.setLong(1, teamId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getLong("USER_ID"));
                    }
                }
            }

            try (PreparedStatement stmt = connection
                    .prepareStatement("DELETE FROM T_MATCH_QUEUE WHERE T_MATCH_QUEUE.TEAM_ID = ?")) {
                stmt.setLong(1, teamId);
                int rows = stmt.executeUpdate();
                connection.commit();
            }

            return true;

        }catch(SQLException e){
            System.out.println("makeMatch exception: "
                    + "teamId=" + teamId
                    + " " + e.getMessage());
        }

        return false;
    }
}
