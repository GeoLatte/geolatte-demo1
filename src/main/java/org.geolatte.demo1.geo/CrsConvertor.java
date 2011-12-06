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

package org.geolatte.demo1.geo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This interface specifies a Coordinate Reference System convertor, which knows how to convert
 * geometries and coordinate arrays to a specific coordinate system.
 *
 * @author Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 */
public interface CrsConvertor {
    /**
     * @return the Spatial Reference Identifier of the source reference system
     */
    int getSourceSrid();

    /**
     * @return the Spatial Reference Identifier of the source reference system
     */
    int getTargetSrid();

    /**
     * Converts a given geometry from the source CRS to the destination CRS
     *
     * @param inGeom The geometry that needs to be converted
     * @return geometry that represents the same geometry as the given geometry, but in a different CRS
     * @throws GeoTransformationException If for some reason the transformation failed.
     */
    Geometry convert(Geometry inGeom) throws GeoTransformationException;

    /**
     * Converts a series of coordinates from the source CRS to the destination CRS
     *
     * @param coordinates the coordinates to convert
     * @return An arraylist, where each element is a converted coordinate of the coordinate
     *         at the same index in the original array.
     * @throws GeoTransformationException If for some reason the transformation failed.
     */
    Coordinate[] convert(Coordinate[] coordinates) throws GeoTransformationException;
}