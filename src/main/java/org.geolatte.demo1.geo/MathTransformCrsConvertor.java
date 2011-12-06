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

import com.vividsolutions.jts.geom.*;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * @author Karel Maesen (original code)
 * @author Yves Vandewoude (modifications)
 * class be.vlaanderen.awv.geo.MathTransformCrsConvertor
 * company <a href="http://www.Qmino.com">Qmino</a>
 * Creation-Date: 10-sep-2009
 * Creation-Time: 14:04:08
 */

/**
 * A generic implementation of the CrsConvertor interface that requires a MathTransform object
 * as its calculation engine. Class is package only, since convertors should be created through
 * the CrsConvertor factory.
 */
class MathTransformCrsConvertor implements CrsConvertor {

    private static final GeometryFactory INSTANCE = new GeometryFactory();
    private final MathTransform transform;
    private final int targetSRID;
    private final int sourceSRID;

    MathTransformCrsConvertor(MathTransform transform, int sourceSRID, int targetSRID) {
        this.transform = transform;
        this.targetSRID = targetSRID;
        this.sourceSRID = sourceSRID;
    }


    /**
     * Converts a given geometry from the source CRS to the destination CRS
     *
     * @param inGeom The geometry that needs to be converted
     * @return geometry that represents the same geometry as the given geometry, but in a different CRS
     * @throws GeoTransformationException If for some reason the transformation failed.
     * @see CrsConvertor#convert(com.vividsolutions.jts.geom.Geometry)
     */
    public Geometry convert(Geometry inGeom)
            throws GeoTransformationException {
        Geometry retGeom;
        if (inGeom instanceof Polygon) {
            retGeom = convertPolygon((Polygon) inGeom);
        } else if (inGeom instanceof Point) {
            retGeom = convertPoint((Point) inGeom);
        } else if (inGeom instanceof LineString) {
            retGeom = convertLineString((LineString) inGeom);
        } else if (inGeom instanceof MultiLineString) {
            retGeom = convertMultiLineString((MultiLineString) inGeom);
        } else if (inGeom instanceof GeometryCollection) {
            retGeom = convertGeometryCollection((GeometryCollection) inGeom);
        } else {
            throw new GeoTransformationException("Unsupported operation: Conversion of " +
                    inGeom.getGeometryType() + " is not yet implemented");
        }
        retGeom.setSRID(targetSRID);
        return retGeom;
    }

    /**
     * Converts a series of coordinates from the source CRS to the destination CRS
     *
     * @param coordinates the coordinates to convert
     * @return An arraylist, where each element is a converted coordinate of the coordinate
     *         at the same index in the original array.
     * @throws GeoTransformationException If for some reason the transformation failed.
     *                                    Currently, this method assumes 2D geometries.
     *                                    TODO: The assumption of 2D geometries should be removed.
     * @see CrsConvertor#convert(com.vividsolutions.jts.geom.Coordinate[])
     */
    public Coordinate[] convert(Coordinate[] coordinates)
            throws GeoTransformationException {
        double[] source = new double[coordinates.length * 2];
        for (int i = 0; i < coordinates.length; i++) {
            source[2 * i] = coordinates[i].x;
            source[2 * i + 1] = coordinates[i].y;
        }
        double[] dest = new double[source.length];
        try {
            this.transform.transform(source, 0, dest, 0, coordinates.length);
        } catch (TransformException e) {
            throw new GeoTransformationException("Transformation failed: ", e);
        }
        Coordinate[] destCoords = new Coordinate[coordinates.length];
        for (int i = 0; i < destCoords.length; i++) {
            destCoords[i] = new Coordinate(dest[2 * i], dest[2 * i + 1]);
        }
        return destCoords;
    }

    /**
     * @return the Spatial Reference Identifier of the source reference system
     */
    public int getSourceSrid() {
        return this.sourceSRID;
    }

    /**
     * @return the Spatial Reference Identifier of the source reference system
     */
    public int getTargetSrid() {
        return this.targetSRID;
    }


    //////////////////////////////
    //  Private Helper Methods
    //////////////////////////////

    private Geometry convertGeometryCollection(GeometryCollection inGeom)
            throws GeoTransformationException {
        Geometry[] geometries = new Geometry[inGeom.getNumGeometries()];
        for (int i = 0; i < inGeom.getNumGeometries(); i++) {
            Geometry part = inGeom.getGeometryN(i);
            geometries[i] = convert(part);
        }
        return INSTANCE.createGeometryCollection(geometries);

    }

    private LineString convertLineString(LineString lineString)
            throws GeoTransformationException {
        Coordinate[] coords = convert(lineString.getCoordinates());
        return INSTANCE.createLineString(coords);
    }

    private MultiLineString convertMultiLineString(MultiLineString mls)
            throws GeoTransformationException {
        LineString[] lines = new LineString[mls.getNumGeometries()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = convertLineString((LineString) mls.getGeometryN(i));
        }
        return INSTANCE.createMultiLineString(lines);
    }

    private Polygon convertPolygon(Polygon polygon)
            throws GeoTransformationException {
        LinearRing shell = convertLinearRing(polygon.getExteriorRing());
        return INSTANCE.createPolygon(shell, new LinearRing[]{});
    }

    private Point convertPoint(Point point)
            throws GeoTransformationException {
        Coordinate[] coords = convert(point.getCoordinates());
        return INSTANCE.createPoint(coords[0]);
    }

    private LinearRing convertLinearRing(LineString ring)
            throws GeoTransformationException {
        Coordinate[] coords = convert(ring.getCoordinates());
        return INSTANCE.createLinearRing(coords);
    }


}
