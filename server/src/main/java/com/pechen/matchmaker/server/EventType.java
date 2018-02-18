package com.pechen.matchmaker.server;

/**
 * Created by pechen on 18.02.2018.
 */
public enum EventType {

    UNKNOWN(0, "Неизвестное событие"),
    REGISTRATION(1, "Регистрация пользоватля на матч");


    private Integer number;
    private String description;

    private EventType(Integer number, String description) {

    }
}
