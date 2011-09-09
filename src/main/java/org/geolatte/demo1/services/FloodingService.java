package org.geolatte.demo1.services;

import com.vividsolutions.jts.geom.Point;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>
 * A service that provides flooding information
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
@Path("/rest/flooding")
public class FloodingService {

    /**
     * Gets cities that are threatened when a rise in water level is registered the given point.
     * @param origin
     * @return
     */
    @GET
    @Path("/")
    //@Consumes(MediaType.APPLICATION_JSON)
    //@Produces(MediaType.APPLICATION_JSON)
    public Response getEndangeredCities(@QueryParam("floodorigin")String origin) {

        return Response.ok("Test").build();
    }
}
