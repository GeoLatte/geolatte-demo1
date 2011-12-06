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
import org.geolatte.common.Feature;
import org.geolatte.common.dataformats.json.jackson.DefaultFeature;
import org.geolatte.common.transformer.Transformation;
import org.geolatte.common.transformer.TransformationException;
import org.geolatte.demo1.geo.CrsConvertor;
import org.geolatte.demo1.geo.CrsConvertorFactory;


/**
 * <p>
 * Transforms the a feature's geometry to a given CRS.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 * @since SDK1.5
 */
public class FeatureCrsTransformation implements Transformation<Feature, Feature> {

    private int srs = CrsConvertorFactory.GOOGLE_MERCATOR;

    public FeatureCrsTransformation(int srs) {

        this.srs = srs;
    }

    @Override
    public Feature transform(Feature feature) throws TransformationException {

        try {

            CrsConvertor convertor = CrsConvertorFactory.createConvertor(
                    feature.getGeometry().getSRID(),
                    srs);
            Geometry convertedGeometry = convertor.convert(feature.getGeometry());

            // make a copy of the feature and change the geometry to the converted geom.
            DefaultFeature newFeature = new DefaultFeature(feature);
            newFeature.setGeometry(newFeature.getGeometryName(), convertedGeometry);

            return newFeature;

        } catch (Exception e) {
            throw new TransformationException(e);
        }

    }

    private String decapitalize(String inputString) {
        return inputString.length() > 1 ? Character.toLowerCase(inputString.charAt(0)) + inputString.substring(1)
                : inputString.toUpperCase();
    }
}
