package com.pechen.matchmaker.server;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by pechen on 15.02.2018.
 */
@Stateless
@Path("register")
public class MatchRegisterREST {

    @GET
    @Path("/{id}")
    public String registerById(@PathParam("id") String id) {
        return "pong " + id;
    }

}
