package com.pechen.matchmaker.server;

/**
 * Created by pechen on 15.02.2018.
 */
public class User {
    Long id;
    Integer rank;
    Long registrationTime;

    public User(Long id, Integer rank, Long registrationTime) {
        this.id = id;
        this.rank = rank;
        this.registrationTime = registrationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
