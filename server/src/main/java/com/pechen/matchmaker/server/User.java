package com.pechen.matchmaker.server;

/**
 * Created by pechen on 15.02.2018.
 */
public class User {
    Long userId;
    Integer rank;
    Long registrationTime;
    Long teamId;
    boolean isMatchCompleted;

    public User(Long userId, Integer rank, Long registrationTime, Long teamId, boolean isMatchCompleted) {
        this.userId = userId;
        this.rank = rank;
        this.registrationTime = registrationTime;
        this.teamId = teamId;
        this.isMatchCompleted = isMatchCompleted;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Long getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Long registrationTime) {
        this.registrationTime = registrationTime;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public boolean isMatchCompleted() {
        return isMatchCompleted;
    }

    public void setMatchCompleted(boolean matchCompleted) {
        isMatchCompleted = matchCompleted;
    }
}
