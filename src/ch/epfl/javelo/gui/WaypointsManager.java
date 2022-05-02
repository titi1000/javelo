package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;


import java.util.function.Consumer;

public class WaypointsManager {

    private Pane pane = new Pane();
    private ObservableList<Waypoint> waypoints;
    private ObjectProperty<MapViewParameters> parameters;
    private Graph graph;
    private Consumer<String> errorConsumer;
    private final ObjectProperty<Point2D> point2d = new SimpleObjectProperty<>();

    public WaypointsManager(Graph graph, ObjectProperty<MapViewParameters> parameters, ObservableList<Waypoint> waypoints, Consumer<String> errorConsumer) {

        this.waypoints = waypoints;
        this.parameters = parameters;
        this.graph = graph;
        this.errorConsumer = errorConsumer;

        pane.setPrefSize(600, 300);
        pane.setPickOnBounds(false);

        PointCh position = waypoints.get(0).position();
        PointWebMercator pointWeb = PointWebMercator.ofPointCh(position);
        double x = parameters.get().viewX(pointWeb);
        double y = parameters.get().viewY(pointWeb);

        for (int i = 0; i < waypoints.size(); i++) {
            if (i == 0) {
                WaypointCreator("first", x, y);
            } else if (i == waypoints.size() - 1) {
                WaypointCreator("last", x, y);
                break;
            } else {
                WaypointCreator("middle", x, y);
            }
            position = waypoints.get(i + 1).position();
            pointWeb = PointWebMercator.ofPointCh(position);
            x = parameters.get().viewX(pointWeb);
            y = parameters.get().viewY(pointWeb);
        }
    }


    private void WaypointCreator(String status, double x, double y) {
;
        ObjectProperty<Point2D> point2d = new SimpleObjectProperty<>();
        point2d.set(new Point2D(x, y));
        SVGPath outsideBorder = new SVGPath();
        SVGPath insideBorder = new SVGPath();

        outsideBorder.setContent("M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20");
        outsideBorder.getStyleClass().add("pin_outside");
        insideBorder.setContent("M0-23A1 1 0 000-29 1 1 0 000-23");
        insideBorder.getStyleClass().add("pin_inside");

        Group pins = new Group(outsideBorder, insideBorder);

        pins.getStyleClass().add("pin");
        pins.getStyleClass().add(status);

        pins.setLayoutX(x);
        pins.setLayoutY(y);

        pane.setOnMouseDragged(drag -> {
            point2d.set(new Point2D(drag.getX(), drag.getY()));
            pins.setLayoutX(drag.getX());
            pins.setLayoutY(drag.getY());

        });
        pane.getChildren().add(pins);

    }

    public void addWaypoint(double x, double y) {
        PointCh point = new PointCh(x, y);
        waypoints.add(new Waypoint(point, graph.nodeClosestTo(point, 500)));
    }

    public Node pane() {
        return pane;
    }
}