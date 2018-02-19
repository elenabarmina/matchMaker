package com.pechen.matchmaker.server;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.crypto.Data;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * Created by pechen on 15.02.2018.
 */
@Stateless
@Path("register")
public class MatchRegisterREST {

    public EventManager events;

    public MatchRegisterREST(){
        this.events = new EventManager(EventType.REGISTRATION);
    }

    @PostConstruct
    public void init(){
        events.subscribe(EventType.REGISTRATION, new MatchPackerRegistrationSubscriber());
    }

    @GET
    @Path("/{id}/{rank}")
    public Response registerById(@PathParam("id") String id, @PathParam("rank") String rank) {

        long registrationtime = new Date().getTime();

        events.notify(EventType.REGISTRATION,
                new UserInMatchQueue(Long.parseLong(id), Integer.parseInt(rank), registrationtime, 0L, false));

        return Response.status(200).build();
    }

}
