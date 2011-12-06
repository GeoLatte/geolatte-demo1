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

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * This static factory class creates <code>CRSConvertor</code>s between a source
 * and target Coordinate Reference System.
 *
 * @author Original code: Karel Maesen, Geovise BVBA (http://www.geovise.com/)
 * @author Minor modifications: Yves Vandewoude, Qmino BVBA (http://www.qmino.com/)
 */
public class CrsConvertorFactory {

    private static final Logger LOGGER = Logger.getLogger(CrsConvertorFactory.class);

    public static final int LAMBERT_72 = 31370;
    public static final int GOOGLE_MERCATOR = 900913;

    //since in this version the EPSG for Google CRS is STILL not defined, add it manually.
    private static CoordinateReferenceSystem MERCATOR_CRS;
    private static final String MERCATORDESCRIPTION = "PROJCS[\"Google Mercator\", "
            + "GEOGCS[\"WGS 84\", "
            + "DATUM[\"World Geodetic System 1984\", "
            + "SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], "
            + "AUTHORITY[\"EPSG\",\"6326\"]], "
            + "PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], "
            + "UNIT[\"degree\", 0.017453292519943295], "
            + "AXIS[\"Geodetic latitude\", NORTH], "
            + "AXIS[\"Geodetic longitude\", EAST], "
            + "AUTHORITY[\"EPSG\",\"4326\"]],  "
            + "PROJECTION[\"Mercator (1SP)\", AUTHORITY[\"EPSG\",\"9804\"]], "
            + "PARAMETER[\"semi_major\", 6378137.0], "
            + "PARAMETER[\"semi_minor\", 6378137.0], "
            + "PARAMETER[\"latitude_of_origin\", 0.0], "
            + "PARAMETER[\"central_meridian\", 0.0], "
            + "PARAMETER[\"scale_factor\", 1.0],  "
            + "PARAMETER[\"false_easting\", 0.0],  "
            + "PARAMETER[\"false_northing\", 0.0],  "
            + "UNIT[\"m\", 1.0],  " + "AXIS[\"Easting\", EAST],  "
            + "AXIS[\"Northing\", NORTH], "
            + "AUTHORITY[\"EPSG\",\"900913\"]]";

    private static CoordinateReferenceSystem getGoogleMercatorCrs()
            throws GeoTransformationException {
        if (MERCATOR_CRS == null) {
            try {
                MERCATOR_CRS = CRS.parseWKT(MERCATORDESCRIPTION);
            } catch (FactoryException e) {
                throw new GeoTransformationException("Impossible to create the google mercator crs: ", e);
            }
        }
        return MERCATOR_CRS;
    }

    /**
     * Return a Coordinate Reference System for the given spatial reference identifier
     *
     * @param srid The spatial reference identifier
     * @return The coordinate reference system that corresponds with the given srid.
     * @throws GeoTransformationException - if the CRS creation failed for an other reason.
     */
    private static CoordinateReferenceSystem findCRS(int srid)
            throws GeoTransformationException {
        if (srid == GOOGLE_MERCATOR) {
            return getGoogleMercatorCrs();
        }
        try {
            return CRS.decode("EPSG:" + srid, true);
        } catch (NoSuchAuthorityCodeException e) {
            // Can probably not happen since we specified the authority code. Nevertheless...
            throw new GeoTransformationException("Authority code not understood", e);
        } catch (FactoryException e) {
            throw new GeoTransformationException("CrsDecoding failed for an unknown reason.", e);
        }

    }


    /**
     * Returns an instance of a convertor implementation that transforms between two given
     * coordinate reference systems, represented by their spatial reference identifiers
     *
     * @param source The spatial reference identifier of the original coordinate reference system
     * @param target The spatial reference identifier of the target coordinate reference system
     * @return A convertor capable of transforming geometries between the two given reference systems
     * @throws GeoTransformationException If for some reason the convertor could not be constructed.
     */
    public static CrsConvertor createConvertor(int source, int target) throws GeoTransformationException {
        try {
            CoordinateReferenceSystem sourceCRS = findCRS(source);
            CoordinateReferenceSystem targetCRS = findCRS(target);
            MathTransform t = CRS.findMathTransform(sourceCRS, targetCRS);
            return new MathTransformCrsConvertor(t, source, target);
        } catch (FactoryException e) {
            LOGGER.warn("Couldn't create CRSConvertor with source EPSG "
                    + source + " and target " + target);
            throw new GeoTransformationException("Couldn't get convertor", e);
        }
	}
}