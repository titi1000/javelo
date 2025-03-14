package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a simple route, i.e. from a starting point to an ending point, with no intermediate points.
 *
 * @author Quentin Chappuis (339517)
 */
public final class SingleRoute implements Route {
    private final List<Edge> edges;
    private final double[] route;
    private final List<PointCh> points = new ArrayList<>();

    /**
     * Constructor that initializes the edges list and the route for the dichotomous search.
     *
     * @param edges a list containing all edges
     * @throws IllegalArgumentException if the edges list is empty
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());

        this.edges = List.copyOf(edges);
        route = new double[edges.size() + 1];
        route[0] = 0;

        for (int i = 1; i < edges.size() + 1; i++) {
            route[i] = edges.get(i - 1).length() + route[i - 1];
        }

        points.add(edges.get(0).fromPoint());
        for (Edge edge : edges) points.add(edge.toPoint());
    }

    /**
     * Returns the segment index at the given position (in metres).
     *
     * @param position the given position
     * @return the segment index
     */
    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * Returns the length of the route, in metres.
     *
     * @return the length
     */
    @Override
    public double length() {
        return route[route.length - 1];
    }

    /**
     * Returns all the edges of the route.
     *
     * @return all the edges
     */
    @Override
    public List<Edge> edges() {
        return edges;
    }

    /**
     * Returns all the points located at the ends of the edges of the route.
     *
     * @return all the points
     */
    @Override
    public List<PointCh> points() {
        return List.copyOf(points);
    }

    /**
     * Dichotomously searches for the position in a list of lengths.
     *
     * @param position the given position
     * @return the index
     */
    private int dichotomousSearch(double position) {
        int res = Arrays.binarySearch(route, position);
        if (res == edges().size()) return res - 1;

        return res < 0 ? -(res + 2) : res;
    }

    /**
     * Returns the point at the given position along the route.
     *
     * @param position the given position
     * @return the point
     */
    @Override
    public PointCh pointAt(double position) {
        position = Math2.clamp(0, position, length());
        int index = dichotomousSearch(position);
        position -= route[index];

        return edges.get(index).pointAt(position);
    }

    /**
     * Returns the altitude at the given position along the route.
     *
     * @param position the given position
     * @return the altitude
     */
    @Override
    public double elevationAt(double position) {
        position = Math2.clamp(0, position, length());
        int index = dichotomousSearch(position);
        position -= route[index];

        return edges.get(index).elevationAt(position);
    }

    /**
     * Returns the identity of the node belonging to the route and being closest to the given position.
     *
     * @param position the given position
     * @return the identity of the closest node
     */
    @Override
    public int nodeClosestTo(double position) {
        position = Math2.clamp(0, position, length());
        int index = dichotomousSearch(position);
        position -= route[index];

        return position < edges.get(index).length() / 2 ? edges.get(index).fromNodeId() : edges.get(index).toNodeId();
    }

    /**
     * Returns the point on the route that is closest to the given reference point.
     *
     * @param point the given point
     * @return the closest point
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint res = RoutePoint.NONE;
        int index = 0;
        PointCh currentEdgePointAt;
        for (Edge edge : edges) {
            double position = Math2.clamp(0, edge.positionClosestTo(point), edge.length());
            currentEdgePointAt = edge.pointAt(position);
            double thatPosition = route[index] + position;
            double thatDistanceToReference = currentEdgePointAt.distanceTo(point);
            res = res.min(currentEdgePointAt, thatPosition, thatDistanceToReference);
            ++index;
        }
        return res;
    }
}
