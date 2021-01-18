package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private final Sphere sphere1 = new Sphere();
    private final Sphere sphere2 = new Sphere();
    private final Sphere sphere3 = new Sphere();
    private final double timeStep = 0.75;
    private final int GM = 40000;
    private double powerOfPosition; // Just making the calculation easier to read

    // initial condition:
    private double xOfSphere1 = -300, yOfSphere1 = 0;
    private double xOfSphere2 = 300, yOfSphere2 = 0;
    private double vxOfSphere1 = 0, vyOfSphere1 = 5;
    private double vxOfSphere2 = 0, vyOfSphere2 = -5;
    private final double xOfSphere3 = 0;
    private final double yOfSphere3 = 0;
    private double zOfSphere3 = 0; // Customizable
    private double vzOfSphere3 = 3;

    private final int largeRadius = 20;
    private final int smallRadius = 5;

    private double powOfXOfSphere1;
    private double powOfYOfSphere1;
    private double powOfXOfSphere2;
    private double powOfYOfSphere2;
    private double powZOfSphere3;
    private double lastX1, lastY1, lastX2, lastY2;
    private Rotate rotateX, rotateY, rotateZ;
    public static Camera camera = new PerspectiveCamera(true);
    private Translate translate;
    private Line line;
    private Pane pane;

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Create the bodies:
        sphere1.setRadius(largeRadius);
        sphere2.setRadius(largeRadius);
        sphere3.setRadius(smallRadius);
        line = new Line();

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
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(20), e -> calculate()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        // Create pane to hold the spheres
        pane = new Pane();
        pane.getChildren().add(sphere1);
        pane.getChildren().add(sphere2);
        pane.getChildren().add(sphere3);
        pane.getChildren().add(line);

        // Create scene to hold the pane and attach Camera
        Scene scene = new Scene(pane, 800, 800);
        scene.setFill(Color.SILVER);
        scene.setCamera(camera);

        // TranslateX: 0.0, TranslateY: 935.0, TranslateZ: -655.0,
        // RotateX: 70.0, RotateY: 0.0, RotateZ: 0.0

        // MOVE CAMERA
        camera.getTransforms().addAll(
                rotateX = new Rotate(70, Rotate.X_AXIS),
                rotateY = new Rotate(0, Rotate.Y_AXIS),
                rotateZ = new Rotate(0, Rotate.Z_AXIS),
                translate = new Translate(0, 935, -655));

        // Move camera to a good view
        camera.translateZProperty().set(-1000);

        // Set the clipping planes
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        ((PerspectiveCamera) camera).setFieldOfView(35);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case A:
                    translate.setX(translate.getX() - 5);
                    break;
                case D:
                    translate.setX(translate.getX() + 5);
                    break;
                case W:
                    translate.setY(translate.getY() + 5);
                    break;
                case S:
                    translate.setY(translate.getY() - 5);
                    break;
                case LEFT:
                    rotateZ.setAngle(rotateY.getAngle() + 5);
                    break;
                case RIGHT:
                    rotateY.setAngle(rotateY.getAngle() - 5);
                    break;
                case UP:
                    rotateX.setAngle(rotateX.getAngle() - 5);
                    break;
                case DOWN:
                    rotateX.setAngle(rotateX.getAngle() + 5);
                    break;
                case Y:
                    translate.setZ(translate.getZ() + 5);
                    System.out.println("hi");
                    break;
                case H:
                    translate.setZ(translate.getZ() - 5);
                    break;
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

        powOfXOfSphere1 = Math.pow(xOfSphere1, 2);
        powOfYOfSphere1 = Math.pow(yOfSphere1, 2);
        powerOfPosition = Math.pow(powOfXOfSphere1 + powOfYOfSphere1, 1.5);

        // Calculate v for M1:
        vxOfSphere1 = vxOfSphere1 + ((-1) * GM / (2 * powerOfPosition)) * xOfSphere1 * timeStep;
        xOfSphere1 = xOfSphere1 + vxOfSphere1;

        // Calculate coordinates for M1:
        vyOfSphere1 = vyOfSphere1 + ((-1) * GM / (2 * powerOfPosition)) * yOfSphere1 * timeStep;
        yOfSphere1 = yOfSphere1 + vyOfSphere1;

        /**********************************************/

        powOfXOfSphere2 = Math.pow(xOfSphere2, 2);
        powOfYOfSphere2 = Math.pow(yOfSphere2, 2);
        powerOfPosition = Math.pow(powOfXOfSphere2 + powOfYOfSphere2, 1.5);

        // Calculate v for M2:
        vxOfSphere2 = vxOfSphere2 + ((-1) * GM / (2 * powerOfPosition)) * xOfSphere2 * timeStep;
        xOfSphere2 = xOfSphere2 + vxOfSphere2;

        // Calculate coordinates for M2:
        vyOfSphere2 = vyOfSphere2 + ((-1) * GM / (2 * powerOfPosition)) * yOfSphere2 * timeStep;
        yOfSphere2 = yOfSphere2 + vyOfSphere2;

        /**********************************************/

        double powX = Math.pow(xOfSphere2 - xOfSphere1, 2);
        double powY = Math.pow(yOfSphere2 - yOfSphere1, 2);
        powZOfSphere3 = Math.pow(zOfSphere3, 2);
        powerOfPosition = Math.pow(powX + powY + powZOfSphere3, 1.5);

        // Calculate v for M3:
        vzOfSphere3 = vzOfSphere3 + (-1) * ((4 * GM) / powerOfPosition) * zOfSphere3 * timeStep;

        // Calculate coordinates for M3:
        zOfSphere3 = zOfSphere3 + vzOfSphere3;

        move(xOfSphere1, yOfSphere1, xOfSphere2, yOfSphere2, zOfSphere3);
    }

    private void move(double x1, double y1, double x2, double y2, double z) {

        // draw tracks:
        pane.getChildren().add(new Line(lastX1, lastY1, x1, y1));
        pane.getChildren().add(new Line(lastX2, lastY2, x2, y2));

        sphere1.translateXProperty().set(x1);
        sphere1.translateYProperty().set(y1);

        sphere2.translateXProperty().set(x2);
        sphere2.translateYProperty().set(y2);

        sphere3.translateXProperty().set(xOfSphere3);
        sphere3.translateYProperty().set(yOfSphere3);
        sphere3.translateZProperty().set(z);

        System.out.print("TranslateX: " + translate.getX());
        System.out.print(", TranslateY: " + translate.getY());
        System.out.print(", TranslateZ: " + translate.getZ());
        System.out.print(", RotateX: " + rotateX.getAngle());
        System.out.print(", RotateY: " + rotateY.getAngle());
        System.out.println(", RotateZ: " + rotateZ.getAngle());
    }

    public static void main(String[] args) {
        launch(args);
    }
}