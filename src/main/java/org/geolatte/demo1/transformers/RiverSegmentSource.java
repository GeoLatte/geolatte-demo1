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
import org.geolatte.graph.*;
import org.geolatte.graph.algorithms.GraphAlgorithm;
import org.geolatte.graph.algorithms.GraphAlgorithms;
import org.hibernate.StatelessSession;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Calculates the downstream path of the waterway network starting from a given starting point.
 * </p>
 *
 * @author Bert Vanhooff
 * @author <a href="http://www.qmino.com">Qmino bvba</a>
 */
public class RiverSegmentSource extends TransformerSource<Geometry> {

    private static LocateableGraph<Node, Geometry> graph;
    private Locatable startPoint;
    private static CrsConvertor toSourceConvertor;

    // Build the waterway network once
    private static void buildGraph(StatelessSession session) {

        if (graph != null) { return; } // already build

        try {
            GraphBuilder<Node, Geometry> graphBuilder = Graphs.createGridIndexedGraphBuilder(new Envelope(3000000, 2000000, 4500000, 3300000), 20000);

            for (Waterway waterway : (List<Waterway>) session.createCriteria(Waterway.class).list()) {
                graphBuilder.addEdge(waterway.getBeginNode(), waterway.getEndNode(), new BasicEdgeWeight(1), waterway.getGeometry());
            }

            graph = graphBuilder.build();

        } catch (BuilderException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RiverSegmentSource(float x, float y, StatelessSession session) {

        try {
            // Convert from google to lambert (what we use internally in the graph)
            CrsConvertor convertor = CrsConvertorFactory.createConvertor(900913, 3035);
            Coordinate[] coordinate = convertor.convert(new Coordinate[]{new Coordinate(y, x)});
            this.startPoint = new LocatablePointAdapter((float) coordinate[0].x, (float) coordinate[0].y);
            buildGraph(session);
        } catch (GeoTransformationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Iterable<Geometry> output() {

        // search closest nodes in a range of 20km from the given startpoint
        List<InternalNode<Node, Geometry>> nodesFound = graph.getClosestNodes(startPoint, 1, 20000);
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

            resultGeometries.add(iterator.getCurrentEdge());
        }

        return resultGeometries;
    }
}
