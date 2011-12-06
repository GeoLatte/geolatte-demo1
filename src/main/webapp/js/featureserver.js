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

$(document).ready(function() {

    // Show map, zoom to Belgium

    var map = new OpenLayers.Map("map");
    var mapnik = new OpenLayers.Layer.OSM();
    map.addLayer(mapnik);
    map.setCenter(new OpenLayers.LonLat(4.5703125, 50.86144411058923)// Center of the map
            .transform(
            new OpenLayers.Projection("EPSG:4326"), // transform from WGS 1984
            new OpenLayers.Projection("EPSG:900913") // to Spherical Mercator Projection
    ), 9 // Zoom level
    );

    // Create a layer where we will show rivers

    var riverLayer = new OpenLayers.Layer.Vector("rivers");
    map.addLayer(riverLayer);

    // Layer style

    var style_blue = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
    style_blue.strokeColor = "blue";
    style_blue.strokeWidth = 4;
    style_blue.strokeLinecap = "round";

    // Source data is in LAEA European projection

    Proj4js.defs["EPSG:3035"] = "+proj=laea +lat_0=52 +lon_0=10 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +units=m +no_defs";

    var geoJsonParser = new OpenLayers.Format.GeoJSON();

    $( "button").button().click(function() {

        doQuery($('#search').val());
    });

    var doQuery = function (queryParams) {

        riverLayer.removeAllFeatures();

        $.ajax({
            type: 'GET',
            url: '/featureserver/rest/tables/rivers',
            accepts: {json : "application/json"},
            dataType: 'json',
            data: {
                //int_name ilike 'Scheldt' OR int_name ilike 'Meuse'
                cql: queryParams
            },
            success: function(data) {

                jQuery.each(data.items, function (indexInArray, feature) {

                    var riverGeometry = geoJsonParser.read(feature.geometry, 'Geometry').transform(
                                                                                    new OpenLayers.Projection("EPSG:3035"),
                                                                                    new OpenLayers.Projection("EPSG:900913"));
                    var riverFeature = new OpenLayers.Feature.Vector(riverGeometry, null, style_blue);
                    riverLayer.addFeatures([riverFeature]);
                });
            },
            error: function() {
                alert("error");
            }//,
            //dataType: 'json'
        });
    }

});