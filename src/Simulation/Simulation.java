package Simulation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Map;

public class Simulation extends Application {


    private static final double SCREEN_WIDTH = 1000;
    private static final double SCREEN_HEIGHT = 800;
    private static final double SUBSCENE_WIDTH = 800;
    private static final double SUBSCENE_HEIGHT = 800;

    private static final double LARGE_RADIUS = 10;
    private static final double SMALL_RADIUS = 8;

    private final double timeStep;
    private final double GM;
    private final double Gm;
    private double x1, y1, x2, y2, z3;
    private double vX1, vY1, vX2, vY2, vZ3;
    private final Boolean drawTrack;

    private final Sphere sphere1 = new Sphere();
    private final Sphere sphere2 = new Sphere();
    private final Sphere sphere3 = new Sphere();

    private Pane displayPane;
    private SubScene subScene;
    private Cylinder xAxis, yAxis, zAxis;
    private Translate cameraTranslate;
    private Rotate cameraRotateX, cameraRotateY, cameraRotateZ;
    private final int cameraStep = 10;

    private int cycleCount = 0, yCount = 0;
    private boolean touchedYAxis = true;

    // Constructor receives the input fields
    public Simulation(Map<String, String> inputFieldMap) {
        this.timeStep = Double.parseDouble(inputFieldMap.get("time_step"));
        this.GM = Double.parseDouble(inputFieldMap.get("GM"));
        this.Gm = Double.parseDouble(inputFieldMap.get("Gm"));
        this.x1 = Double.parseDouble(inputFieldMap.get("x1"));
        this.y1 = Double.parseDouble(inputFieldMap.get("y1"));
        this.vX1 = Double.parseDouble(inputFieldMap.get("vX1"));
        this.vY1 = Double.parseDouble(inputFieldMap.get("vY1"));
        this.vX2 = Double.parseDouble(inputFieldMap.get("vX2"));
        this.vY2 = Double.parseDouble(inputFieldMap.get("vY2"));
        this.z3 = Double.parseDouble(inputFieldMap.get("z3"));
        this.vZ3 = Double.parseDouble(inputFieldMap.get("vZ3"));
        drawTrack = inputFieldMap.get("draw track").equals("true");

        this.x2 = -1 * x1;
        this.y2 = 0;
    }

    @Override
    public void start(Stage primaryStage) {

        // init:
        setAppearances();
        setAnimation();
        Scene scene = setupScene();
        setCamera(primaryStage);

        primaryStage.setTitle("Sitnikov's Problem Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void checkTransformation() {
        System.out.print("TranslateX: " + cameraTranslate.getX());
        System.out.print(", TranslateY: " + cameraTranslate.getY());
        System.out.print(", TranslateZ: " + cameraTranslate.getZ());
        System.out.print(", RotateX: " + cameraRotateX.getAngle());
        System.out.print(", RotateY: " + cameraRotateY.getAngle());
        System.out.println(", RotateZ: " + cameraRotateZ.getAngle());
    }

    private void setCamera(Stage stage) {
        Camera camera = new PerspectiveCamera(true);
        camera.setFarClip(20000);
        camera.setNearClip(0.1);
        subScene.setCamera(camera);
        cameraTranslate = new Translate(960, 1870, -680);
        cameraRotateX = new Rotate(70, Rotate.X_AXIS);
        cameraRotateY = new Rotate(-25, Rotate.Y_AXIS);
        cameraRotateZ = new Rotate(-10, Rotate.Z_AXIS);
        camera.getTransforms().addAll(cameraTranslate,
                cameraRotateX,
                cameraRotateY,
                cameraRotateZ);

        // Register listener for change in camera transform
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            checkTransformation();
            switch (event.getCode()) {
                case A -> cameraTranslate.setX(cameraTranslate.getX() - cameraStep);
                case D -> cameraTranslate.setX(cameraTranslate.getX() + cameraStep);
                case W -> cameraTranslate.setY(cameraTranslate.getY() + cameraStep);
                case S -> cameraTranslate.setY(cameraTranslate.getY() - cameraStep);
                case LEFT -> cameraRotateY.setAngle(cameraRotateY.getAngle() + 5);
                case RIGHT -> cameraRotateY.setAngle(cameraRotateY.getAngle() - 5);
                case UP -> cameraRotateX.setAngle(cameraRotateX.getAngle() - 5);
                case DOWN -> cameraRotateX.setAngle(cameraRotateX.getAngle() + 5);
                case Q -> cameraRotateZ.setAngle(cameraRotateZ.getAngle() - 5);
                case E -> cameraRotateZ.setAngle(cameraRotateZ.getAngle() + 5);
                case I -> cameraTranslate.setZ(cameraTranslate.getZ() + cameraStep);
                case O -> cameraTranslate.setZ(cameraTranslate.getZ() - cameraStep);
            }
        });
    }

    private void setAppearances() {
        sphere1.setRadius(LARGE_RADIUS);
        sphere2.setRadius(LARGE_RADIUS);
        sphere3.setRadius(SMALL_RADIUS);

        xAxis = createCylinderLine(new Point3D(-500, 0, 0), new Point3D(500, 0, 0));
        yAxis = createCylinderLine(new Point3D(0, -500, 0), new Point3D(0, 500, 0));
        zAxis = createCylinderLine(new Point3D(0, 0, -300), new Point3D(0, 0, 300));

        PhongMaterial material1 = new PhongMaterial();
        material1.setDiffuseColor(Color.ORANGE);
        material1.setSpecularColor(Color.BLACK);

        PhongMaterial material2 = new PhongMaterial();
        material2.setDiffuseColor(Color.DARKCYAN);
        material2.setSpecularColor(Color.BLACK);

        PhongMaterial material3 = new PhongMaterial();
        material3.setDiffuseColor(Color.SEAGREEN);
        material3.setSpecularColor(Color.BLACK);

        sphere1.setMaterial(material1);
        sphere2.setMaterial(material2);
        sphere3.setMaterial(material3);
        xAxis.setMaterial(material1);
        yAxis.setMaterial(material2);
        zAxis.setMaterial(material3);
    }

    private void setAnimation() {
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(1), e -> calculate()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    // TODO: customize left VBox layout with SceneBuilder
    // TODO: may try VBOX = FXMLLoader.load(getClass().getResource("left.fxml"));
    private Scene setupScene() {
        // Create pane to display the animation.
        displayPane = new Pane();

        // Set displayPane as the root of subscene.
        subScene = new SubScene(displayPane, SUBSCENE_WIDTH, SUBSCENE_HEIGHT);
        subScene.setFill(Color.BLACK);

        // Display all items and set background color.
        displayPane.getChildren().addAll(sphere1, sphere2, sphere3, xAxis, yAxis, zAxis);
        BackgroundFill fill = new BackgroundFill(Color.BLACK, null, null);
        displayPane.setBackground(new Background(fill));

        // Create a VBox to hold all widgets.
        VBox leftControl = new VBox();

        // Create splitPane to hold both controls and the subscene with displayPane.
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftControl, subScene);

        // Create a main scene to hold the splitPane.
        return new Scene(splitPane, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    private void calculate() {

        // Recording variables to draw tracks
        double lastX1 = x1;
        double lastY1 = y1;
        double lastX2 = x2;
        double lastY2 = y2;


        // record current velocities:
        double lastVXOfSphere1 = vX1;
        double lastVYOfSphere1 = vY1;
        double lastVXOfSphere2 = vX2;
        double lastVYOfSphere2 = vY2;
        double lastVZOfSphere3 = vZ3;


        // Just separating the calculation steps for M1
        double powX1 = Math.pow(x1, 2);
        double powY1 = Math.pow(y1, 2);
        double powZ3 = Math.pow(z3, 2);
        double powXY = Math.pow(powX1 + powY1, 1.5);
        double powXYZ = Math.pow(powX1 + powY1 + powZ3, 1.5);


        // Calculate and update velocities and locations (M1):
        vX1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * x1 * timeStep;
        x1 += lastVXOfSphere1 * timeStep;

        vY1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * y1 * timeStep;
        y1 += lastVYOfSphere1 * timeStep;


        // Just separating the calculation steps for M2
        double powX2 = Math.pow(x2, 2);
        double powY2 = Math.pow(y2, 2);
        powXY = Math.pow(powX2 + powY2, 1.5);
        powXYZ = Math.pow(powX2 + powY2 + powZ3, 1.5);


        // Calculate and update velocities and locations (M2):
        vX2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * x2 * timeStep;
        x2 += lastVXOfSphere2 * timeStep;

        vY2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * y2 * timeStep;
        y2 += lastVYOfSphere2 * timeStep;


        // Just separating the calculation steps for M3
        double powXDiff = Math.pow(x2 - x1, 2);
        double powYDiff = Math.pow(y2 - y1, 2);
        powXY = Math.pow(powXDiff + powYDiff + powZ3, 1.5);


        // Calculate and update velocities and locations (M3):
        vZ3 += (-1) * ((4 * GM) / powXY) * z3 * timeStep;
        z3 += lastVZOfSphere3 * timeStep;

        if (drawTrack) {
            drawTracks(x1, y1, x2, y2, lastX1, lastY1, lastX2, lastY2);
        }
        countCycle();
        move(x1, y1, x2, y2, z3);
    }

    private void countCycle() {
        if (Math.round(y1) == 0 && !touchedYAxis) {
            yCount++;
            touchedYAxis = true;
            if (yCount == 2) {
                // One cycle completes
                yCount = 0;
                cycleCount++;
                Sphere sphere1 = new Sphere(2);
                sphere1.translateXProperty().set(x1);
                sphere1.translateYProperty().set(y1);

                Sphere sphere2 = new Sphere(2);
                sphere2.translateXProperty().set(x2);
                sphere2.translateYProperty().set(y2);
                displayPane.getChildren().addAll(sphere1, sphere2);
            }

        } else if (Math.round(y1) != 0 && touchedYAxis) {
            touchedYAxis = false;
        }
    }

    private void drawTracks(double x1, double y1, double x2, double y2,
                            double lastX1, double lastY1, double lastX2, double lastY2) {
        Line line1 = new Line(x1, y1, lastX1, lastY1);
        Line line2 = new Line(x2, y2, lastX2, lastY2);
        line1.setStroke(Color.ORANGE);
        line2.setStroke(Color.DARKCYAN);
        displayPane.getChildren().add(line1);
        displayPane.getChildren().add(line2);
    }

    private void move(double x1, double y1, double x2, double y2, double z) {

        // Move M1
        sphere1.translateXProperty().set(x1);
        sphere1.translateYProperty().set(y1);

        // Move M2
        sphere2.translateXProperty().set(x2);
        sphere2.translateYProperty().set(y2);

        // Move M3
        double x3 = 0;
        double y3 = 0;
        sphere3.translateXProperty().set(x3);
        sphere3.translateYProperty().set(y3);
        sphere3.translateZProperty().set(z);

    }


    public Cylinder createCylinderLine(Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(1, height);

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    public static void main(String[] args) {
        launch(args);
    }
}