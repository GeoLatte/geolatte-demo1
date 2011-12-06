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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.geolatte.common.transformer.TransformerSource;
import org.geolatte.demo1.domain.Node;
import org.geolatte.demo1.domain.Waterway;
import org.geolatte.demo1.geo.CrsConvertor;
import org.geolatte.demo1.geo.CrsConvertorFactory;
import org.geolatte.demo1.geo.GeoTransformationException;
import org.geolatte.geom.Envelope;
import org.geolatte.graph.*;
import org.geolatte.graph.algorithms.GraphAlgorithm;
import org.geolatte.graph.algorithms.GraphAlgorithms;
import org.hibernate.Session;

import java.util.ArrayList;
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
public class RiverSegmentSource extends TransformerSource<Geometry> {


    private static LocateableGraph<Node, Geometry> graph;
    private Locatable startPoint;
    private static CrsConvertor toSourceConvertoronvertor;

    public RiverSegmentSource(Locatable startPoint, Session session) {

        this.startPoint = startPoint;
        buildGraph(session);
    }

    // Build the waterway network once
    private static void buildGraph(Session session) {

        if (graph != null) { // already build
            return;
        }

        List<Waterway> waterways = null;
        waterways = (List<Waterway>) session.createCriteria(Waterway.class).list();

        try {
            final int sourceSrid = waterways.get(0).getGeometry().getSRID();
            final int targetSrid = 31300;
            CrsConvertor toTargetConvertoronvertor = CrsConvertorFactory.createConvertor(
                    sourceSrid,
                    targetSrid);
            toSourceConvertoronvertor = CrsConvertorFactory.createConvertor(
                    targetSrid,
                    sourceSrid);

            Coordinate[] bbox = toTargetConvertoronvertor.convert(new Coordinate[] {new Coordinate(2.33, 6.6), new Coordinate(49.3, 51.6)});

            GraphBuilder<Node, Geometry> graphBuilder = Graphs.createGridIndexedGraphBuilder(new Envelope(bbox[0].x, bbox[0].y, bbox[1].x, bbox[1].y), 20000);

            for (Waterway waterway : waterways) {

                Geometry convertedGeometry = toTargetConvertoronvertor.convert(waterway.getGeometry());
                waterway.setGeometry(convertedGeometry);

                if (waterway.getBeginNode() != null && waterway.getEndNode() != null) {
                    graphBuilder.addEdge(waterway.getBeginNode(), waterway.getEndNode(), new BasicEdgeWeight(1), waterway.getGeometry());
                }
            }

            graph = graphBuilder.build();

        } catch (BuilderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (GeoTransformationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Iterable<Geometry> output() {

        Node startNode = graph.getClosestNodes(startPoint, 1, 3000).get(0).getWrappedNode();

        GraphAlgorithm<GraphTree<Node, Geometry>> bfsTree = GraphAlgorithms.createBFS(graph, startNode, 200, 0);

        List<Geometry> geometries = new ArrayList<Geometry>();

        bfsTree.execute();
        GraphTreeIterator<Node, Geometry> iterator = bfsTree.getResult().iterator();
        while (iterator.next()) {

            // Some ways do not have a geometry in osm .. ignore these
            if (iterator.getCurrentEdge() == null) {
                continue;
            }

            try {
                geometries.add(toSourceConvertoronvertor.convert(iterator.getCurrentEdge()));
            } catch (GeoTransformationException e) {
                e.printStackTrace();
            }
        }

        return geometries;
    }
}
