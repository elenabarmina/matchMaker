package com.pechen.matchmaker.server.controllers;

import com.pechen.matchmaker.server.EventManager;
import com.pechen.matchmaker.server.EventType;
import com.pechen.matchmaker.server.MatchPackerRegistrationSubscriber;
import com.pechen.matchmaker.server.entities.UserInMatchQueue;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Date;


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
