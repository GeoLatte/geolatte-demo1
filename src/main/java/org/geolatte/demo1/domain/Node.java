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

package org.geolatte.demo1.domain;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.Point;
import org.geolatte.geom.jts.JTS;
import org.geolatte.graph.Locatable;

/**
 * <p>
 * Represents a locatable Node (point) in the waterway routing system.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class Node implements Locatable {

    private long id;
    private com.vividsolutions.jts.geom.Geometry location;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Point getLocation() {
        return (Point)JTS.from(location);
    }

    public void setLocation(Geometry geometry) {
        location = JTS.to(geometry);
    }

    public com.vividsolutions.jts.geom.Geometry getJTSLocation() {
        return location;
    }

    public void setJTSLocation(com.vividsolutions.jts.geom.Geometry geometry) {
        location = geometry;
    }

    /**
     * @return The X-coordinate
     */
    @Override
    public float getX() {
        return (float) ((com.vividsolutions.jts.geom.Point)location).getX();
    }

    /**
     * @return The Y-coordinate
     */
    @Override
    public float getY() {
        return (float) ((com.vividsolutions.jts.geom.Point)location).getY();
    }
}
