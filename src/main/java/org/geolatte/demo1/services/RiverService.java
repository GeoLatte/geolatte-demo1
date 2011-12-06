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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.geolatte.common.transformer.ClosedTransformerChain;
import org.geolatte.common.transformer.SimpleTransformerSink;
import org.geolatte.common.transformer.TransformerChainFactory;
import org.geolatte.demo1.domain.Place;
import org.geolatte.demo1.domain.Waterway;
import org.geolatte.demo1.geo.LocatablePointAdapter;
import org.geolatte.demo1.transformers.Buffer;
import org.geolatte.demo1.transformers.FilterDuplicates;
import org.geolatte.demo1.transformers.GetCitiesWithinBounds;
import org.geolatte.demo1.transformers.RiverSegmentSource;
import org.geolatte.demo1.util.HibernateUtil;
import org.hibernate.Session;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * No comment provided yet for this class.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 * @since SDK1.5
 */
@Path("/rest/river")
public class RiverService {


    @Path("/")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<RiverTo> getRivers() {

        List<RiverTo> result = null;

        try {
            // Begin unit of work
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();

            result = session.createCriteria(Waterway.class).list();

            // End unit of work
            session.getTransaction().commit();
        } catch (Exception ex) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
        }


        return result;
    }

    @Path("/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Place> getEndangeredCities(@QueryParam("x") final float x, @QueryParam("y") final float y) {

        try {
            // Begin unit of work
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();

            SimpleTransformerSink<Place> sink = new SimpleTransformerSink<Place>();

            ClosedTransformerChain chain = TransformerChainFactory.<Geometry, Place>newChain()
                    .add(new RiverSegmentSource(new LocatablePointAdapter((float)x, (float)y), session))
                    .add(new Buffer())
                    .add(new GetCitiesWithinBounds(session))
                    .addFilter(new FilterDuplicates<Point>())
                    .last(sink);

            chain.run();

            return sink.getCollectedOutput();
        } catch (Exception ex) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
        }

        return Collections.emptyList();
    }
}
