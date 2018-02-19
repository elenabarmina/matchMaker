package com.pechen.matchmaker.server;

/**
 * Created by pechen on 18.02.2018.
 */
public class Team {
    Long id;
    Integer usersCount;
    Boolean isCompleted;

    public Team(Long id, Integer usersCount) {
        this.id = id;
        this.usersCount = usersCount;
        this.isCompleted = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(Integer usersCount) {
        this.usersCount = usersCount;
    }

    public Boolean getCompleted() {
        return isCompleted;
    }

    public void setCompleted(Boolean completed) {
        isCompleted = completed;
    }
}
