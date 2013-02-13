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

package org.geolatte.demo1.TransferObjects;

import org.geolatte.common.dataformats.json.to.GeoJsonToAssembler;
import org.geolatte.common.transformer.Transformation;
import org.geolatte.common.transformer.TransformationException;
import org.geolatte.demo1.domain.Place;
import org.geolatte.demo1.util.CrsConvertor;
import org.geolatte.demo1.util.CrsConvertorFactory;
import org.geolatte.demo1.util.GeoTransformationException;
import org.geolatte.geom.jts.JTS;

/**
 * <p>
 * No comment provided yet for this class.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 * @since SDK1.5
 */
public class PlaceToTransferObject implements Transformation<Place, PlaceTo> {

    private GeoJsonToAssembler geoJsonToFactory = new GeoJsonToAssembler();
    private static CrsConvertor toTargetConvertor;

    static {
        try {
            toTargetConvertor = CrsConvertorFactory.createConvertor(4326, 900913);
        } catch (GeoTransformationException e) {
            e.printStackTrace();
        }
    }


    @Override
    public PlaceTo transform(Place input) throws TransformationException {

        try {
            return new PlaceTo(input.getName(),geoJsonToFactory.toTransferObject( JTS.from(toTargetConvertor.convert(input.getJTSGeometry()))));
        } catch (GeoTransformationException e) {
            throw new TransformationException(e);
        }
    }
}
