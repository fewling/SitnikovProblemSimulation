package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.math.MathContext;

public class Main extends Application {


    public static final int SCREEN_WIDTH = 1000;
    public static final int SCREEN_HEIGHT = 800;
    private static final double SUBSCENE_WIDTH = 800;
    private static final double SUBSCENE_HEIGHT = 800;
    public static final int FAR_CLIP = 20000;           // Maximum range of camera view
    public static final double NEAR_CLIP = 0.1;         // Minimum range of camera view

    // Initial conditions:
    private static final double TIMESTEP = 0.01;         // Customizable
    private static final double GM = 40000;             // Customizable, Gravitational Constant (M1 &2)
    private static final double Gm = 0;                 // Customizable, Gravitational Constant (M3)
    private static final double LARGE_RADIUS = 20;      // Radius for M1 & M2
    private static final double SMALL_RADIUS = 5;       // Radius for M3
    private double xOfSphere1 = -300, yOfSphere1 = 0;
    private double xOfSphere2 = 300, yOfSphere2 = 0;
    private double vXOfSphere1 = 0, vYOfSphere1 = 5;
    private double vXOfSphere2 = 0, vYOfSphere2 = -5;
    private final double xOfSphere3 = 0;
    private final double yOfSphere3 = 0;
    private double zOfSphere3 = 0;                      // Customizable
    private double vZOfSphere3 = 3;                     // Customizable, but depends on zOfSphere, max when zOfSphere == 0


    private final Sphere sphere1 = new Sphere();
    private final Sphere sphere2 = new Sphere();
    private final Sphere sphere3 = new Sphere();


    private double lastX1, lastY1, lastX2, lastY2, lastZ3;  // Recording variables to draw tracks
    private Rotate rotateX, rotateY, rotateZ;
    public static Camera camera = new PerspectiveCamera(true); // true --> eye fixed on (0, 0, 0)
    private Translate translate;
    private Pane displayPane;

    private final Label currentSphere1XLabel = new Label();
    private final Label currentSphere1YLabel = new Label();
    private final Label currentSphere2XLabel = new Label();
    private final Label currentSphere2YLabel = new Label();
    private final Label currentSphere3ZLabel = new Label();

    private double minX1 = xOfSphere1, maxX1;
    private double minX2, maxX2 = xOfSphere2;
    private double maxZ3 = zOfSphere3;
    private final Label minSphere1XLabel = new Label();
    private final Label maxSphere1XLabel = new Label();
    private final Label minSphere2XLabel = new Label();
    private final Label maxSphere2XLabel = new Label();
    private final Label maxSphere3ZLabel = new Label();

    private int cycleCount = 0, yCount = 0;
    private final Label cycleCountLabel = new Label();
    private Timeline animation;
    private boolean touchedYAxis = true;

    @Override
    public void start(Stage primaryStage) {

        // Create the bodies:
        sphere1.setRadius(LARGE_RADIUS);
        sphere2.setRadius(LARGE_RADIUS);
        sphere3.setRadius(SMALL_RADIUS);

        // Setting the appearances:
        PhongMaterial material1 = new PhongMaterial();
        material1.setDiffuseColor(Color.ORANGE);
        material1.setSpecularColor(Color.BLACK);
        sphere1.setMaterial(material1);

        PhongMaterial material2 = new PhongMaterial();
        material2.setDiffuseColor(Color.DARKCYAN);
        material2.setSpecularColor(Color.BLACK);
        sphere2.setMaterial(material2);

        PhongMaterial material3 = new PhongMaterial();
        material3.setDiffuseColor(Color.SEAGREEN);
        material3.setSpecularColor(Color.BLACK);
        sphere3.setMaterial(material3);

        // Setting the animation
        animation = new Timeline(new KeyFrame(Duration.millis(1), e -> calculate()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        // Create pane to display the spheres
        displayPane = new Pane();
        displayPane.getChildren().add(sphere1);
        displayPane.getChildren().add(sphere2);
        displayPane.getChildren().add(sphere3);
        displayPane.getChildren().add(new Line(SUBSCENE_WIDTH, 0, -1 * SUBSCENE_WIDTH, 0));
        displayPane.getChildren().add(new Line(0, SUBSCENE_HEIGHT, 0, -1 * SUBSCENE_HEIGHT));

        // Create subscene to hold the displayPane and attach camera
        SubScene subScene = new SubScene(displayPane, SUBSCENE_WIDTH, SUBSCENE_HEIGHT);
        subScene.setCamera(camera);

        // Create splitPane to hold both controls and the subscene with displayPane
        SplitPane splitPane = new SplitPane();
        VBox leftControl = new VBox();

        Button topViewBtn = new Button("Top View");
        Button tiltViewBtn = new Button("Tilt View");   // default view
        topViewBtn.setDisable(false);
        tiltViewBtn.setDisable(true);

        topViewBtn.setOnAction(actionEvent -> {
            camera.getTransforms().addAll(
                    rotateX = new Rotate(290, Rotate.X_AXIS),
                    rotateY = new Rotate(0, Rotate.Y_AXIS),
                    rotateZ = new Rotate(0, Rotate.Z_AXIS),
                    translate = new Translate(-60, -935, -1030));
            topViewBtn.setDisable(true);
            tiltViewBtn.setDisable(false);
        });

        tiltViewBtn.setOnAction(actionEvent -> {
            camera.getTransforms().addAll(
                    rotateX = new Rotate(70, Rotate.X_AXIS),
                    rotateY = new Rotate(0, Rotate.Y_AXIS),
                    rotateZ = new Rotate(0, Rotate.Z_AXIS),
                    translate = new Translate(60, 935, -1030));
            topViewBtn.setDisable(false);
            tiltViewBtn.setDisable(true);
        });

        leftControl.getChildren().add(topViewBtn);
        leftControl.getChildren().add(tiltViewBtn);
        leftControl.getChildren().add(currentSphere1XLabel);
        leftControl.getChildren().add(currentSphere1YLabel);
        leftControl.getChildren().add(currentSphere2XLabel);
        leftControl.getChildren().add(currentSphere2YLabel);
        leftControl.getChildren().add(currentSphere3ZLabel);
        leftControl.getChildren().add(minSphere1XLabel);
        leftControl.getChildren().add(maxSphere1XLabel);
        leftControl.getChildren().add(maxSphere2XLabel);
        leftControl.getChildren().add(minSphere2XLabel);
        leftControl.getChildren().add(maxSphere3ZLabel);
        leftControl.getChildren().add(cycleCountLabel);
        splitPane.getItems().addAll(leftControl, subScene);

        // Create a main scene to hold the splitPane
        Scene scene = new Scene(splitPane, SCREEN_WIDTH, SCREEN_HEIGHT);
        scene.setFill(Color.SILVER);

        // For timestep = 0.75:
        // TranslateX: 0.0, TranslateY: 935.0, TranslateZ: -655.0,
        // RotateX: 70.0, RotateY: 0.0, RotateZ: 0.0

        // MOVE CAMERA
        camera.getTransforms().addAll(
                rotateX = new Rotate(70, Rotate.X_AXIS),
                rotateY = new Rotate(0, Rotate.Y_AXIS),
                rotateZ = new Rotate(0, Rotate.Z_AXIS),
                translate = new Translate(60, 935, -1030));

        // Move camera to a good view
        camera.translateZProperty().set(-1000);

        // Set the clipping planes
        camera.setNearClip(NEAR_CLIP);
        camera.setFarClip(FAR_CLIP);
        ((PerspectiveCamera) camera).setFieldOfView(35);

        // Camera controls:
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case A -> translate.setX(translate.getX() - 5);
                case D -> translate.setX(translate.getX() + 5);
                case W -> translate.setY(translate.getY() + 5);
                case S -> translate.setY(translate.getY() - 5);
                case LEFT -> rotateY.setAngle(rotateY.getAngle() + 5);
                case RIGHT -> rotateY.setAngle(rotateY.getAngle() - 5);
                case UP -> rotateX.setAngle(rotateX.getAngle() - 5);
                case DOWN -> rotateX.setAngle(rotateX.getAngle() + 5);
                case I -> translate.setZ(translate.getZ() + 5);
                case O -> translate.setZ(translate.getZ() - 5);
            }
        });

        primaryStage.setTitle("Sitnikov's Problem Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void calculate() {

        // record current locations:
        lastX1 = xOfSphere1;
        lastY1 = yOfSphere1;
        lastX2 = xOfSphere2;
        lastY2 = yOfSphere2;
        lastZ3 = zOfSphere3;


        // record current velocities:
        double lastVXOfSphere1 = vXOfSphere1;
        double lastVYOfSphere1 = vYOfSphere1;
        double lastVXOfSphere2 = vXOfSphere2;
        double lastVYOfSphere2 = vYOfSphere2;
        double lastVZOfSphere3 = vZOfSphere3;

        // Just separating the calculation steps for M1
        double powX1 = Math.pow(xOfSphere1, 2);
        double powY1 = Math.pow(yOfSphere1, 2);
        double powZ3 = Math.pow(zOfSphere3, 2);
        double powXY = Math.pow(powX1 + powY1, 1.5);
        double powXYZ = Math.pow(powX1 + powY1 + powZ3, 1.5);

        // Calculate and update velocities and locations (M1):
        vXOfSphere1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * xOfSphere1 * TIMESTEP;
        xOfSphere1 += lastVXOfSphere1 * TIMESTEP;

        vYOfSphere1 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * yOfSphere1 * TIMESTEP;
        yOfSphere1 += lastVYOfSphere1 * TIMESTEP;


        // Just separating the calculation steps for M2
        double powX2 = Math.pow(xOfSphere2, 2);
        double powY2 = Math.pow(yOfSphere2, 2);
        powXY = Math.pow(powX2 + powY2, 1.5);
        powXYZ = Math.pow(powX1 + powY1 + powZ3, 1.5);

        // Calculate and update velocities and locations (M2):
        vXOfSphere2 = vXOfSphere2 + (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * xOfSphere2 * TIMESTEP;
        xOfSphere2 += lastVXOfSphere2 * TIMESTEP;

        vYOfSphere2 += (((2 * Gm) / powXYZ) - GM / (2 * powXY)) * yOfSphere2 * TIMESTEP;
        yOfSphere2 += lastVYOfSphere2 * TIMESTEP;


        // Just separating the calculation steps for M3
        double powXDiff = Math.pow(xOfSphere2 - xOfSphere1, 2);
        double powYDiff = Math.pow(yOfSphere2 - yOfSphere1, 2);
        powXY = Math.pow(powXDiff + powYDiff + powZ3, 1.5);

        // Calculate and update velocities and locations (M3):
        vZOfSphere3 += (-1) * ((4 * GM) / powXY) * zOfSphere3 * TIMESTEP;
        zOfSphere3 += lastVZOfSphere3 * TIMESTEP;

        // Calculate number of cycles
        if (Math.round(yOfSphere1) == 0 && !touchedYAxis) {
            yCount++;
            touchedYAxis = true;
            if (yCount == 2) {
                yCount = 0;
                cycleCount++;
            }
        } else if (Math.round(yOfSphere1) != 0 && touchedYAxis == true) {
            touchedYAxis = false;
        }

        move(xOfSphere1, yOfSphere1, xOfSphere2, yOfSphere2, zOfSphere3);
    }

    private void move(double x1, double y1, double x2, double y2, double z) {

        // draw tracks of M1 & M2:
//        displayPane.getChildren().add(new Line(lastX1, lastY1, x1, y1));
//        displayPane.getChildren().add(new Line(lastX2, lastY2, x2, y2));

        // Move M1
        sphere1.translateXProperty().set(x1);
        sphere1.translateYProperty().set(y1);

        // Move M2
        sphere2.translateXProperty().set(x2);
        sphere2.translateYProperty().set(y2);

        // Move M3
        sphere3.translateXProperty().set(xOfSphere3);
        sphere3.translateYProperty().set(yOfSphere3);
        sphere3.translateZProperty().set(z);

        // Update the labels:
        // Multiply Y and Z component by (-1) to match our views
        currentSphere1XLabel.setText("Current X of M1: " + Round(xOfSphere1));
        currentSphere1YLabel.setText("Current Y of M1: " + Round(yOfSphere1) * -1);
        currentSphere2XLabel.setText("Current X of M2: " + Round(xOfSphere2));
        currentSphere2YLabel.setText("Current Y of M2: " + Round(yOfSphere2) * -1);
        currentSphere3ZLabel.setText("Current Z of M3: " + Round(zOfSphere3) * -1);

        minX1 = Math.min(minX1, xOfSphere1);
        maxX1 = Math.max(maxX1, xOfSphere1);
        minX2 = Math.min(minX2, xOfSphere2);
        maxX2 = Math.max(maxX2, xOfSphere2);
        maxZ3 = Math.max(maxZ3, zOfSphere3);
        minSphere1XLabel.setText("Min x of M1: " + Round(minX1));
        maxSphere1XLabel.setText("Max x of M1: " + Round(maxX1));
        minSphere2XLabel.setText("Min x of M2: " + Round(minX2));
        maxSphere2XLabel.setText("Max x of M2: " + Round(maxX2));
        maxSphere3ZLabel.setText("Max z of M3: " + Round(maxZ3));
        cycleCountLabel.setText("Cycle count: " + cycleCount);

        // Check camera properties:
//        System.out.print("TranslateX: " + translate.getX());
//        System.out.print(", TranslateY: " + translate.getY());
//        System.out.print(", TranslateZ: " + translate.getZ());
//        System.out.print(", RotateX: " + rotateX.getAngle());
//        System.out.print(", RotateY: " + rotateY.getAngle());
//        System.out.println(", RotateZ: " + rotateZ.getAngle());
    }

    private double Round(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.round(new MathContext(3));
        return bigDecimal.doubleValue();
    }

    public static void main(String[] args) {
        launch(args);
    }
}