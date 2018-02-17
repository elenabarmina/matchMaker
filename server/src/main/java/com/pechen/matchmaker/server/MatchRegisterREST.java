package com.pechen.matchmaker.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * Created by pechen on 15.02.2018.
 */
@Stateless
@Path("register")
public class MatchRegisterREST {

    @Inject
    ConcurrentSkipListSet<User> usersSet;

    final static Logger logger = LoggerFactory.getLogger(MatchRegisterREST.class);

    @GET
    @Path("/{id}/{rank}")
    public Response registerById(@PathParam("id") String id, @PathParam("rank") String rank) {

        long registrationtime = new Date().getTime();
        logger.info("try register user: " + id + ":" + rank + " " + registrationtime);
        usersSet.add(new User(Long.parseLong(id), Integer.parseInt(rank), registrationtime, 0L, false));

        return Response.status(200).build();
    }

}
