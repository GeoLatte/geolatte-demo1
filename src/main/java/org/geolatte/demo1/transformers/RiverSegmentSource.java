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
import org.geolatte.common.transformer.TransformerSource;
import org.geolatte.demo1.domain.Node;
import org.geolatte.demo1.domain.Waterway;
import org.geolatte.demo1.util.CrsConvertor;
import org.geolatte.demo1.util.CrsConvertorFactory;
import org.geolatte.demo1.util.GeoTransformationException;
import org.geolatte.demo1.util.LocatablePointAdapter;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.jts.JTS;
import org.geolatte.graph.*;
import org.geolatte.graph.algorithms.GraphAlgorithm;
import org.geolatte.graph.algorithms.GraphAlgorithms;
import org.hibernate.StatelessSession;

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
    private static CrsConvertor toSourceConvertor;

    // Build the waterway network once
    private static void buildGraph(StatelessSession session) {

        if (graph != null) { // already build
            return;
        }

        List<Waterway> waterways = null;
        waterways = (List<Waterway>) session.createCriteria(Waterway.class).list();

        try {
            final int sourceSrid = waterways.get(0).getGeometry().getSRID();
            final int targetSrid = 31370;
            CrsConvertor toTargetConvertor = CrsConvertorFactory.createConvertor(
                    sourceSrid,
                    targetSrid);
            toSourceConvertor = CrsConvertorFactory.createConvertor(
                    targetSrid,
                    sourceSrid);

            Coordinate[] bbox = toTargetConvertor.convert(new Coordinate[] {new Coordinate(2.33, 49.3), new Coordinate(6.6, 51.6)});

            Extent extent = new Extent(bbox[0].x, bbox[0].y, bbox[1].x, bbox[1].y);
            GraphBuilder<Node, Geometry> graphBuilder = Graphs.createGridIndexedGraphBuilder(extent, 20000);

            for (Waterway waterway : waterways) {

                // Convert points to lambert
                waterway.setJTSGeometry(toTargetConvertor.convert(waterway.getJTSGeometry()));
                if (waterway.getBeginNode().getJTSLocation().getSRID() != targetSrid) {
                    waterway.getBeginNode().setJTSLocation(toTargetConvertor.convert(waterway.getBeginNode().getJTSLocation()));
                }
                if (waterway.getEndNode().getJTSLocation().getSRID() != targetSrid) {
                    waterway.getEndNode().setJTSLocation(toTargetConvertor.convert(waterway.getEndNode().getJTSLocation()));
                }


                if (waterway.getBeginNode() != null && waterway.getEndNode() != null) {
                    //if (extent.contains(waterway.getBeginNode().getLocation()) &&  extent.contains(waterway.getEndNode().getLocation())) {
                        graphBuilder.addEdge(waterway.getBeginNode(), waterway.getEndNode(), new BasicEdgeWeight(1), waterway.getGeometry());
                    //}
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

    public RiverSegmentSource(float x, float y, StatelessSession session) {

        try {
            // Convet from google to lambert (what we use internally in the graph)
            CrsConvertor convertor = CrsConvertorFactory.createConvertor(900913, 31370);
            Coordinate[] coordinate = convertor.convert(new Coordinate[]{new Coordinate(x, y)});
            this.startPoint = new LocatablePointAdapter((float)coordinate[0].x, (float)coordinate[0].y);
            buildGraph(session);
        } catch (GeoTransformationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    protected Iterable<Geometry> output() {

        // search closest nodes in a range of 1km from the startpoint
        List<InternalNode<Node, Geometry>> nodesFound = graph.getClosestNodes(startPoint, 1, 5000);
        if (nodesFound.size() == 0) { return new ArrayList<Geometry>(); }
        Node startNode = nodesFound.get(0).getWrappedNode();

        // Create and execute breath-first-limited search (find all routes downstream of the startingpoint)
        GraphAlgorithm<GraphTree<Node, Geometry>> bfsTree = GraphAlgorithms.createBFS(graph, startNode, 200, 0);
        List<Geometry> resultGeometries = new ArrayList<Geometry>();
        bfsTree.execute();
        GraphTreeIterator<Node, Geometry> iterator = bfsTree.getResult().iterator();
        while (iterator.next()) {

            // Some ways do not have a geometry in osm .. ignore these
            if (iterator.getCurrentEdge() == null) {
                continue;
            }

            try {
                resultGeometries.add(JTS.from(toSourceConvertor.convert(JTS.to(iterator.getCurrentEdge()))));
            } catch (GeoTransformationException e) {
                e.printStackTrace();
            }
        }

        return resultGeometries;
    }
}
