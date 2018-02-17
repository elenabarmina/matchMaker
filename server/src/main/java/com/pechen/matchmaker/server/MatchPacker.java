package com.pechen.matchmaker.server;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.*;

/**
 * Created by pechen on 17.02.2018.
 */
public class MatchPacker {

    @Inject
    ConcurrentSkipListSet<User> usersSet;
    @Inject
    ConcurrentSkipListMap<Long, ArrayList<Long>> matchMap;

    ExecutorService packerService = Executors.newSingleThreadExecutor();

    public void start(){
        packerService.submit(new Runnable() {
            public void run() {
                while (true){
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //check if user suitable to team
                    //yes: add (to form teams (check if users == 8) use separate thread?)
                    //no: add in "single team" (what time check if single team can be added to other?)
                }
            }
        });
    };

    public void packUserIntoMatch(User user){

    }

    private double appropriateWaiting(long user1RegistrationTime, long user2RegistrationTime){
        long currentTime = (new Date()).getTime();
        return (currentTime - user1RegistrationTime) / 5000 + (currentTime - user2RegistrationTime) / 5000;
    }

}
