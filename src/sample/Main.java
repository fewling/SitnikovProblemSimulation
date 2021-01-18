package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private final Sphere sphere1 = new Sphere();
    private final Sphere sphere2 = new Sphere();
    private final Sphere sphere3 = new Sphere();
    private final double timeStep = 1;
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
    private final int offsetX = 400;
    private final int offsetY = 400;

    private double powOfXOfSphere1;
    private double powOfYOfSphere1;
    private double powOfXOfSphere2;
    private double powOfYOfSphere2;
    private double powZOfSphere3;
    private double paneY = 0;
    private double paneX = 0;
    private int rotate;

    @Override
    public void start(Stage primaryStage) throws Exception {

        // Create the bodies:
        sphere1.setRadius(largeRadius);
        sphere2.setRadius(largeRadius);
        sphere3.setRadius(smallRadius);

        // Setting the colors:
        PhongMaterial material1 = new PhongMaterial();
        material1.setDiffuseColor(Color.ORANGE);
        material1.setSpecularColor(Color.BLACK);
        sphere1.setMaterial(material1);

        PhongMaterial material2 = new PhongMaterial();
        material2.setDiffuseColor(Color.DARKCYAN);
        material2.setSpecularColor(Color.BLACK);
        sphere2.setMaterial(material2);

        PhongMaterial material3 = new PhongMaterial();
        material3.setDiffuseColor(Color.HONEYDEW);
        material3.setSpecularColor(Color.BLACK);
        sphere3.setMaterial(material3);

        // Setting the animation
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(20), e -> calculate()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();


        Pane pane = new Pane();
        pane.getChildren().add(sphere1);
        pane.getChildren().add(sphere2);
        pane.getChildren().add(sphere3);

        Scene scene = new Scene(pane, 1200, 850);
        Camera camera = new PerspectiveCamera(true);
        scene.setCamera(camera);

        //Move back a little to get a good view of the sphere
        camera.translateZProperty().set(-800);

        //Set the clipping planes
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        ((PerspectiveCamera) camera).setFieldOfView(35);

        Group cameraGroup = new Group();
        cameraGroup.getChildren().add(camera);
        pane.getChildren().add(cameraGroup);

        primaryStage.setTitle("Sitnikov's Problem Simulator");
        primaryStage.setScene(scene);

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W:
                    camera.translateZProperty().set(camera.getTranslateZ() + 100);
                    System.out.println("Pressed W");
                    break;
                case S:
                    camera.translateZProperty().set(camera.getTranslateZ() - 100);
                    System.out.println("Pressed S");
                    break;

                case UP:
                    rotate =+ 10;
                    camera.setRotate(rotate);
                    break;

                case DOWN:
                    rotate -= 10;
                    camera.setRotate(rotate);
                    break;
            }
        });
        primaryStage.show();
    }

    private void calculate() {

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

        sphere1.setTranslateX(x1);
        sphere1.setTranslateY(y1);

        sphere2.setTranslateX(x2);
        sphere2.setTranslateY(y2);

        sphere3.setTranslateX(xOfSphere3);
        sphere3.setTranslateY(yOfSphere3);
        sphere3.setTranslateZ(z);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
