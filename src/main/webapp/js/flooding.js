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

    OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
        defaultHandlerOptions: {
            'single': true,
            'double': false,
            'pixelTolerance': 0,
            'stopSingle': false,
            'stopDouble': false
        },

        initialize: function(options) {
            this.handlerOptions = OpenLayers.Util.extend(
                    {}, this.defaultHandlerOptions
            );
            OpenLayers.Control.prototype.initialize.apply(
                    this, arguments
            );
            this.handler = new OpenLayers.Handler.Click(
                    this, {
                        'click': this.trigger
                    }, this.handlerOptions
            );
        },

        trigger: function(e) {
            var lonlat = map.getLonLatFromViewPortPx(e.xy);
            //alert("You clicked near " + lonlat.lat + " N, " +
            //                          + lonlat.lon + " E");
            var point = new OpenLayers.Geometry.Point(lonlat.lon, lonlat.lat);
            doQuery(lonlat.lat, lonlat.lon);
        }

    });

    var click = new OpenLayers.Control.Click();
    map.addControl(click);
    click.activate();


    // Create a layer where we will flooded cities

    var floodingLayer = new OpenLayers.Layer.Vector("flooding");
    map.addLayer(floodingLayer);

    // Layer style

    var style_red = OpenLayers.Util.extend({}, OpenLayers.Feature.Vector.style['default']);
    style_red.strokeColor = "red";
    style_red.strokeWidth = 5;
    style_red.strokeLinecap = "round";


    var geoJsonParser = new OpenLayers.Format.GeoJSON();

    /*
     $( "button").button().click(function() {

     doQuery($('#search').val());
     });
     */

    var doQuery = function (x, y) {

        floodingLayer.removeAllFeatures();

        $.ajax({
            type: 'GET',
            url: 'rest/flood/cities',
            accepts: {json : "application/json"},
            dataType: 'json',
            data: {
                x: x,
                y: y
            },
            success: function(data) {

                jQuery.each(data, function (indexInArray, feature) {

                    feature.location.type = 'Point';
                    var cityGeometry = geoJsonParser.read(feature.location, 'Geometry');
                    var cityFeature = new OpenLayers.Feature.Vector(cityGeometry, null, style_red);
                    floodingLayer.addFeatures(cityFeature);
                });
            },
            error: function() {
                alert("error");
            }//,
            //dataType: 'json'
        });
    }

});