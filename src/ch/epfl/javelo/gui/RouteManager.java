package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import javafx.scene.input.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the display of the route and (part of) the interaction with it.
 *
 * @author Quentin Chappuis (339517)
 */
public final class RouteManager {

    private final static int DISK_RADIUS = 5;
    private final static int PREF_WIDTH = 600;
    private final static int PREF_HEIGHT = 300;

    private final Pane pane = new Pane();
    private final RouteBean bean;
    private final ObjectProperty<MapViewParameters> parameters;
    private final Circle disk = new Circle();
    private final Polyline polyline = new Polyline();
    private Route route;
    private final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>();
    private final ObjectProperty<Point2D> pointer = new SimpleObjectProperty<>();

    /**
     * @param bean the route bean
     * @param parameters a JavaFX property, read-only, containing the parameters of the displayed map
     */
    public RouteManager(RouteBean bean, ObjectProperty<MapViewParameters> parameters) {
        this.bean = bean;
        this.parameters = parameters;

        init();
    }

    /**
     *
     */
    private void init() {
        pane.setPrefSize(PREF_WIDTH, PREF_HEIGHT);
        pane.setPickOnBounds(false);

        disk.setRadius(DISK_RADIUS);
        disk.setId("highlight");

        polyline.setId("route");

        pane.getChildren().add(polyline);
        pane.getChildren().add(disk);

        parameters.addListener(change -> {
            update();
        });

        bean.routeProperty().addListener(change -> {
            if(bean.route() != null) {
                update();
            }
        });

//        pane.setOnMousePressed(press -> {
//            System.out.println("test");
//            mousePosition.set(new Point2D(press.getSceneX(), press.getSceneY()));
//            pointer.set(new Point2D(pane.getLayoutX(), pane.getLayoutY()));
//
//        });
//
//        pane.setOnMouseDragged(this::setPolylineOnDrag);

        disk.setOnMouseClicked(click -> {
            PointCh pointCh = parameters.get().pointAt(click.getSceneX(), click.getSceneY()).toPointCh();
            double position = route.pointClosestTo(pointCh).position();
            int closestNode = route.nodeClosestTo(position);
            Waypoint waypoint = new Waypoint(pointCh, closestNode);
            bean.waypoints().add(bean.indexOfNonEmptySegmentAt(position) + 1, waypoint);
        });
    }

    /**
     * Returns the JavaFX panel containing the route line and the highlighting disk.
     *
     * @return the JavaFX panel
     */
    public Pane pane() {
        return pane;
    }

    /**
     *
     */
    private void setDisk() {
        if (route == null) {
            disk.setVisible(false);
            return;
        }

        if (Double.isNaN(bean.highlightedPosition())) return;
        PointCh pointCh = route.pointAt(bean.highlightedPosition());
        PointWebMercator webMercatorPoint = PointWebMercator.ofPointCh(pointCh);
        double x = parameters.get().viewX(webMercatorPoint);
        double y = parameters.get().viewY(webMercatorPoint);

        disk.setLayoutX(x);
        disk.setLayoutY(y);
        disk.setVisible(true);
    }

    /**
     *
     */
    private void setPolyline() {
        if (route == null) {
            polyline.setVisible(false);
        } else {
            List<PointCh> pointsList = route.points();
            Double[] points = new Double[pointsList.size() * 2];
            int index = 0;
            for (PointCh point : pointsList) {
                points[index] = parameters.get().viewX(PointWebMercator.ofPointCh(point));
                points[index + 1] = parameters.get().viewY(PointWebMercator.ofPointCh(point));
                index += 2;
            }
            polyline.getPoints().setAll(points);
            polyline.setVisible(true);
        }
    }

    private void setPolylineOnDrag(MouseEvent drag) {
        double differenceX = mousePosition.get().getX() - pointer.get().getX();
        double differenceY = mousePosition.get().getY() - pointer.get().getY();
        polyline.setLayoutX(drag.getSceneX() - differenceX);
        polyline.setLayoutY(drag.getSceneY() - differenceY);
    }

    /**
     *
     */
    private void update() {
        route = bean.route();
//        pane.setOnMouseDragged(drag -> {
//            System.out.println("yoo");
//            double differenceX = mousePosition.get().getX() - pointer.get().getX();
//            double differenceY = mousePosition.get().getY() - pointer.get().getY();
//
//            pane.setOnMouseReleased(release -> {
//                double newX = release.getSceneX() - differenceX;
//                double newY = release.getSceneY() - differenceY;
//
//                polyline.setLayoutX(newX);
//                polyline.setLayoutY(newY);
//            });
//        });
        setPolyline();
        setDisk();
    }
}
