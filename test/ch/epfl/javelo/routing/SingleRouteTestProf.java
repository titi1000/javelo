package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SingleRouteTestProf {
    private static final int ORIGIN_N = 1_200_000;
    private static final int ORIGIN_E = 2_600_000;
    private static final double EDGE_LENGTH = 100.25;

    // Sides of triangle used for "sawtooth" edges (shape: /\/\/\…)
    private static final double TOOTH_EW = 1023;
    private static final double TOOTH_NS = 64;
    private static final double TOOTH_LENGTH = 1025;
    private static final double TOOTH_ELEVATION_GAIN = 100d;
    private static final double TOOTH_SLOPE = TOOTH_ELEVATION_GAIN / TOOTH_LENGTH;

    @Test
    void singleRouteConstructorThrowsOnEmptyEdgeList() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SingleRoute(List.of());
        });
    }

    @Test
    void singleRouteIndexOfSegmentAtAlwaysReturns0() {
        var route = new SingleRoute(verticalEdges(10));
        var rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var p = rng.nextDouble(-100, route.length() + 100);
            assertEquals(0, route.indexOfSegmentAt(p));
        }
    }

    @Test
    void singleRouteLengthReturnsTotalLength() {
        for (int i = 1; i < 10; i += 1) {
            var route = new SingleRoute(verticalEdges(i));
            assertEquals(i * EDGE_LENGTH, route.length());
        }
    }

    @Test
    void singleRouteEdgesAreCopiedToEnsureImmutability() {
        var immutableEdges = verticalEdges(10);
        var mutableEdges = new ArrayList<>(immutableEdges);
        var route = new SingleRoute(mutableEdges);
        mutableEdges.clear();
        assertEquals(immutableEdges, route.edges());
    }

    @Test
    void singleRoutePointsAreNotModifiableFromOutside() {
        var edgesCount = 5;
        var route = new SingleRoute(verticalEdges(edgesCount));
        try {
            route.points().clear();
        } catch (UnsupportedOperationException e) {
            // Nothing to do (the list of points is not modifiable, which is fine).
        }
        assertEquals(edgesCount + 1, route.points().size());
    }

    @Test
    void singleRoutePointsAreCorrect() {
        for (int edgesCount = 1; edgesCount < 10; edgesCount += 1) {
            var edges = verticalEdges(edgesCount);
            var route = new SingleRoute(edges);
            var points = route.points();
            assertEquals(edgesCount + 1, points.size());
            assertEquals(edges.get(0).fromPoint(), points.get(0));
            for (int i = 1; i < points.size(); i += 1)
                assertEquals(edges.get(i - 1).toPoint(), points.get(i));
        }
    }

    @Test
    void singleRoutePointAtWorks() {
        var edgesCount = 4;
        var route = new SingleRoute(sawToothEdges(edgesCount));

        // Outside the range of the route
        assertEquals(sawToothPoint(0), route.pointAt(-1e6));
        assertEquals(sawToothPoint(edgesCount), route.pointAt(+1e6));

        // Edge endpoints
        for (int i = 0; i < edgesCount + 1; i += 1)
            assertEquals(sawToothPoint(i), route.pointAt(i * TOOTH_LENGTH));

        // Points at 1/4, 2/4 and 3/4 of the edges
        for (int i = 0; i < edgesCount; i += 1) {
            for (double p = 0.25; p <= 0.75; p += 0.25) {
                var expectedE = ORIGIN_E + (i + p) * TOOTH_EW;
                var expectedN = (i & 1) == 0
                        ? ORIGIN_N + TOOTH_NS * p
                        : ORIGIN_N + TOOTH_NS * (1 - p);
                assertEquals(
                        new PointCh(expectedE, expectedN),
                        route.pointAt((i + p) * TOOTH_LENGTH));
            }
        }
    }

    @Test
    void singleRouteElevationAtWorks() {
        var edgesCount = 4;
        var route = new SingleRoute(sawToothEdges(edgesCount));
        for (int i = 0; i < edgesCount; i += 1) {
            for (double p = 0; p < 1; p += 0.125) {
                var pos = (i + p) * TOOTH_LENGTH;
                var expectedElevation = (i + p) * TOOTH_ELEVATION_GAIN;
                assertEquals(expectedElevation, route.elevationAt(pos));
            }
        }
        assertEquals(0, route.elevationAt(-1e6));
        assertEquals(edgesCount * TOOTH_ELEVATION_GAIN, route.elevationAt(+1e6));
    }

    @Test
    void singleRouteNodeClosestToWorks() {
        var edgesCount = 4;
        var route = new SingleRoute(sawToothEdges(edgesCount));
        for (int i = 0; i <= edgesCount; i += 1) {
            for (double p = -0.25; p <= 0.25; p += 0.25) {
                var pos = (i + p) * TOOTH_LENGTH;
                assertEquals(i, route.nodeClosestTo(pos));
            }
        }
    }

    @Test
    void singleRoutePointClosestToWorksWithFarAwayPoints() {
        var rng = newRandom();
        var route = new SingleRoute(verticalEdges(1));

        // Points below the route
        var origin = new PointCh(ORIGIN_E, ORIGIN_N);
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var dN = rng.nextDouble(-10_000, -1);
            var dE = rng.nextDouble(-1000, 1000);
            var p = new PointCh(ORIGIN_E + dE, ORIGIN_N + dN);
            var pct = route.pointClosestTo(p);
            assertEquals(origin, pct.point());
            assertEquals(0, pct.position());
            assertEquals(Math.hypot(dE, dN), pct.distanceToReference(), 1e-4);
        }

        // Points above the route
        var end = new PointCh(ORIGIN_E, ORIGIN_N + EDGE_LENGTH);
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var dN = rng.nextDouble(1, 10_000);
            var dE = rng.nextDouble(-1000, 1000);
            var p = new PointCh(ORIGIN_E + dE, ORIGIN_N + EDGE_LENGTH + dN);
            var pct = route.pointClosestTo(p);
            assertEquals(end, pct.point());
            assertEquals(EDGE_LENGTH, pct.position());
            assertEquals(Math.hypot(dE, dN), pct.distanceToReference(), 1e-4);
        }
    }

    @Test
    void singleRoutePointClosestToWorksWithPointsOnRoute() {
        var rng = newRandom();
        var route = new SingleRoute(verticalEdges(20));
        for (int i = 0; i < RANDOM_ITERATIONS; i += 1) {
            var pos = rng.nextDouble(0, route.length());
            var pt = route.pointAt(pos);
            var pct = route.pointClosestTo(pt);
            assertEquals(pt.e(), pct.point().e(), 1e-4);
            assertEquals(pt.n(), pct.point().n(), 1e-4);
            assertEquals(pos, pct.position(), 1e-4);
            assertEquals(0, pct.distanceToReference(), 1e-4);
        }
    }

    @Test
    void singleRoutePointClosestToWorksWithSawtoothPoints() {
        var edgesCount = 4;
        var edges = sawToothEdges(edgesCount);
        var route = new SingleRoute(edges);

        // Points above the sawtooth
        for (int i = 1; i <= edgesCount; i += 2) {
            var p = sawToothPoint(i);
            var dN = i * 500;
            var pAbove = new PointCh(p.e(), p.n() + dN);
            var pct = route.pointClosestTo(pAbove);
            assertEquals(p, pct.point());
            assertEquals(i * TOOTH_LENGTH, pct.position());
            assertEquals(dN, pct.distanceToReference());
        }

        // Points below the sawtooth
        for (int i = 0; i <= edgesCount; i += 2) {
            var p = sawToothPoint(i);
            var dN = i * 500;
            var pBelow = new PointCh(p.e(), p.n() - dN);
            var pct = route.pointClosestTo(pBelow);
            assertEquals(p, pct.point());
            assertEquals(i * TOOTH_LENGTH, pct.position());
            assertEquals(dN, pct.distanceToReference());
        }

        // Points close to the n/8
        var dE = TOOTH_NS / 16d;
        var dN = TOOTH_EW / 16d;
        for (int i = 0; i < edgesCount; i += 1) {
            var upwardEdge = (i & 1) == 0;
            for (double p = 0.125; p <= 0.875; p += 0.125) {
                var pointE = ORIGIN_E + (i + p) * TOOTH_EW;
                var pointN = ORIGIN_N + TOOTH_NS * (upwardEdge ? p : (1 - p));
                var point = new PointCh(pointE, pointN);
                var position = (i + p) * TOOTH_LENGTH;
                var reference = new PointCh(
                        pointE + dE,
                        pointN + (upwardEdge ? -dN : dN));
                var pct = route.pointClosestTo(reference);
                assertEquals(point, pct.point());
                assertEquals(position, pct.position());
                assertEquals(Math.hypot(dE, dN), pct.distanceToReference());
            }
        }
    }

    private static List<Edge> verticalEdges(int edgesCount) {
        var edges = new ArrayList<Edge>(edgesCount);
        for (int i = 0; i < edgesCount; i += 1) {
            var p1 = new PointCh(ORIGIN_E, ORIGIN_N + i * EDGE_LENGTH);
            var p2 = new PointCh(ORIGIN_E, ORIGIN_N + (i + 1) * EDGE_LENGTH);
            edges.add(new Edge(i, i + 1, p1, p2, EDGE_LENGTH, x -> Double.NaN));
        }
        return Collections.unmodifiableList(edges);
    }

    private static List<Edge> sawToothEdges(int edgesCount) {
        var edges = new ArrayList<Edge>(edgesCount);
        for (int i = 0; i < edgesCount; i += 1) {
            var p1 = sawToothPoint(i);
            var p2 = sawToothPoint(i + 1);
            var startingElevation = i * TOOTH_ELEVATION_GAIN;
            edges.add(new Edge(i, i + 1, p1, p2, TOOTH_LENGTH, x -> startingElevation + x * TOOTH_SLOPE));
        }
        return Collections.unmodifiableList(edges);
    }

    private static PointCh sawToothPoint(int i) {
        return new PointCh(
                ORIGIN_E + TOOTH_EW * i,
                ORIGIN_N + ((i & 1) == 0 ? 0 : TOOTH_NS));
    }
}