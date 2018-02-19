package com.pechen.matchmaker.server;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by pechen on 17.02.2018.
 */
public class MatchPackerRegistrationSubscriber implements IEventListener {

    ExecutorService packerService = Executors.newSingleThreadExecutor();

    @Override
    public void update(EventType eventType, UserInMatchQueue newUser) {
        System.out.println("new user registration notification recieved");
        for (Long teamId : DataSource.getInstance().getTeamIdsInProcessing()){
            Team currentTeam = DataSource.getInstance().getTeamById(teamId);
            if (currentTeam == null) continue;

            List<UserInMatchQueue> teamUsersSet = DataSource.getInstance().getUsersInMatchQueueByTeamId(teamId);

            if (teamUsersSet.size() > 0) {
                if (isUserAppropriateToTeam(newUser, teamUsersSet)){
                    System.out.println("newUserappropriate to team id: " + teamId);
                    if (DataSource.getInstance().addUserIfTeamNotChanged(teamId, currentTeam.getUsersCount(), newUser)) return;
                }
            }
        }

        Boolean isTeamCreated = DataSource.getInstance().createNewTeamWithSingleUser(newUser);
        if (!isTeamCreated){
            //return exception to user to try again
            System.out.println("cannot add new user");
        }
    }

    public void start(){
        packerService.submit(new Runnable() {
            public void run() {
                while (true){
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    repackSingleUsersIntoTeam();
                }
            }
        });
    };

    public void repackSingleUsersIntoTeam(){
        System.out.println("repacking started.");
        for (Long singleTeamId : DataSource.getInstance().getSingleTeamIdsInProcessing()){
            for (Long teamId : DataSource.getInstance().getTeamIdsInProcessing()){
                if (singleTeamId.equals(teamId)) continue;

                Team currentSingleTeam = DataSource.getInstance().getTeamById(singleTeamId);
                if (currentSingleTeam == null || currentSingleTeam.getUsersCount() != 1) break;

                Team currentTeam = DataSource.getInstance().getTeamById(teamId);
                if (currentTeam == null) continue;

                List<UserInMatchQueue> teamUsersSet = DataSource.getInstance().getUsersInMatchQueueByTeamId(teamId);
                List<UserInMatchQueue> singleUserSet = DataSource.getInstance().getUsersInMatchQueueByTeamId(singleTeamId);

                if (singleUserSet.size() == 1 && teamUsersSet.size() > 0) {
                    UserInMatchQueue singleUser = singleUserSet.get(0);
                    if (isUserAppropriateToTeam(singleUser, teamUsersSet)){
                        System.out.println("user id " + singleUser.getUserId() + " appropriate to team id " + teamId);
                        DataSource.getInstance().addUserIfTeamNotChanged(teamId, currentTeam.getUsersCount(), singleUser);
                    }
                }
            }
        }
        System.out.println("repacking end.");
    }

    private boolean isUserAppropriateToTeam(UserInMatchQueue user, List<UserInMatchQueue> teamUsers){
        for (UserInMatchQueue teamUser : teamUsers){
            if (!isUsersAppropriate(teamUser, user)) {
                return false;
            }
        }
        return true;
    }

    private boolean isUsersAppropriate(UserInMatchQueue user1, UserInMatchQueue user2){
        return Math.abs(user1.getRank() - user2.getRank())
                <= appropriateWaiting(user1.getRegistrationTime(), user2.getRegistrationTime());
    }

    private double appropriateWaiting(long user1RegistrationTime, long user2RegistrationTime){
        long currentTime = (new Date()).getTime();
        return (currentTime - user1RegistrationTime) / 5000 + (currentTime - user2RegistrationTime) / 5000;
    }
}
