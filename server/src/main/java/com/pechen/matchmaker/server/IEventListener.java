package com.pechen.matchmaker.server;

import com.pechen.matchmaker.server.entities.UserInMatchQueue;

/**
 * Created by pechen on 18.02.2018.
 */
public interface IEventListener {

    public void update(EventType eventType, UserInMatchQueue newUser);

}
