package com.pechen.matchmaker.server;

import com.pechen.matchmaker.server.entities.UserInMatchQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pechen on 18.02.2018.
 */
public class EventManager {
    Map<EventType, List<IEventListener>> listeners = new HashMap<>();

    public EventManager(EventType... events) {
        for (EventType event : events) {
            this.listeners.put(event, new ArrayList<>());
        }
    }

    public void subscribe(EventType eventType, IEventListener listener) {
        List<IEventListener> users = listeners.get(eventType);
        users.add(listener);
    }

    public void unsubscribe(EventType eventType, IEventListener listener) {
        List<IEventListener> users = listeners.get(eventType);
        int index = users.indexOf(listener);
        users.remove(index);
    }

    public void notify(EventType eventType, UserInMatchQueue newUser) {
        List<IEventListener> users = listeners.get(eventType);
        for (IEventListener listener : users) {
            listener.update(eventType, newUser);
        }
    }
}
