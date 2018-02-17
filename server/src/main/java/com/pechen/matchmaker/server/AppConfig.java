package com.pechen.matchmaker.server;

import javax.ws.rs.core.Application;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by pechen on 15.02.2018.
 */
@javax.ws.rs.ApplicationPath("match")
public class AppConfig extends Application {

    public static Long matchId = 0L;

    public static ConcurrentSkipListSet<User> usersSet = new ConcurrentSkipListSet<>();
    public static ConcurrentSkipListMap<Long, ArrayList<Long>> matchMap = new ConcurrentSkipListMap<>();

    public AppConfig(){

    }

}
