/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2010 and Ownership of code is shared by:
 * Qmino bvba - Romeinsestraat 18 - 3001 Heverlee  (http://www.qmino.com)
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.demo1.services;

import org.geolatte.common.transformer.*;
import org.geolatte.demo1.transformers.PlaceToTransferObject;
import org.geolatte.demo1.domain.Place;
import org.geolatte.demo1.transformers.*;
import org.geolatte.demo1.util.HibernateUtil;
import org.geolatte.geom.Geometry;
import org.hibernate.StatelessSession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * The main service entry point of the demo app.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
@Path("/rest/flood")
public class FloodingService {

    @Path("/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlaceTo> getEndangeredCities(@QueryParam("x") final float x, @QueryParam("y") final float y) {

        StatelessSession session = HibernateUtil.getSessionFactory().openStatelessSession();
        try {
            // Begin unit of work
            session.beginTransaction();

            // Create the transformer chain
            SimpleTransformerSink<PlaceTo> sink = new SimpleTransformerSink<PlaceTo>();
            ClosedTransformerChain chain =
                    TransformerChainFactory.<Geometry, PlaceTo>newChain()
                    .add(new RiverSegmentSource(x, y, session))  // Get flooded waterway-segments  \ Geometry)
                    .add(new Buffer())                           // Expand the segments            \ Geometry -> Geometry
                    .add(new GetCitiesWithinBounds(session))     // Get the cities within segments \ Geometry -> [Place]
                    .addFilter(new FilterDuplicates<Place>())    // Filter out duplicates          \ Place    -> Place
                    .add(new PlaceToTransferObject())            // Prepare for serialisation      \ Place    -> PlaceTo
                    .last(sink);                                 // collect results

            // Listen for errors
            chain.addTransformerEventListener(new TransformerEventListener() {
                @Override
                public void ErrorOccurred(TransformerErrorEvent event) {
                    System.err.println("Transformer error : " + event.getNestedEvent());
                }
            });

            // Run the chain
            chain.run();

            session.getTransaction().commit();
            return sink.getCollectedOutput();

        } catch (Exception ex) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
        } finally {
            session.close();
        }

        return Collections.emptyList();
    }
}
