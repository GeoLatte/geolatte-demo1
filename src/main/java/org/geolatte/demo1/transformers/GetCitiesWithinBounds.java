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

package org.geolatte.demo1.transformers;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.geolatte.common.transformer.OneToManyTransformation;
import org.geolatte.common.transformer.TransformationException;
import org.geolatte.demo1.domain.Place;
import org.hibernate.Session;
import org.hibernatespatial.criterion.SpatialRestrictions;

import java.util.Iterator;

/**
 * <p>
 * No comment provided yet for this class.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 * @since SDK1.5
 */
public class GetCitiesWithinBounds implements OneToManyTransformation<Geometry, Point> {

    Session session;

    public GetCitiesWithinBounds(Session session) {

        if (session == null) {
            throw new IllegalArgumentException("Must provide a hibernate session.");
        }

        this.session = session;
    }

    public Iterator<Point> transform(Geometry input) throws TransformationException {

        try {

            return session.createCriteria(Place.class).add(SpatialRestrictions.within("geometry", input)).list().iterator();
        } catch (Exception e) {
            throw new TransformationException(e, input);
        }
    }
}