package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private Sphere sphere1 = new Sphere();
    private Sphere sphere2 = new Sphere();
    private Sphere sphere3 = new Sphere();
    private double timeStep = 1;
    private int GM = 40000;
    private double powerOfPosition; // Just making the calculation easier to read

    // initial condition:
    private double xOfSphere1 = -300, yOfSphere1 = 0;
    private double xOfSphere2 = 300, yOfSphere2 = 0;
    private double vxOfSphere1 = 0, vyOfSphere1 = 5;
    private double vxOfSphere2 = 0, vyOfSphere2 = -5;

    private int sphereRadius = 20;
    private int offsetX = 400, offsetY = 400;

    private double powOfXOfSphere1;
    private double powOfYOfSphere1;
    private double powOfXOfSphere2;
    private double powOfYOfSphere2;

    @Override
    public void start(Stage primaryStage) throws Exception {
        sphere1.setRadius(sphereRadius);
        sphere2.setRadius(sphereRadius);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.ORANGE);
        material.setSpecularColor(Color.BLACK);
        sphere1.setMaterial(material);

        Timeline animation = new Timeline(new KeyFrame(Duration.millis(20), e -> calculate()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();

        Pane pane = new Pane();
        pane.getChildren().add(sphere1);
        pane.getChildren().add(sphere2);

        Scene scene = new Scene(pane, 800, 800);

        primaryStage.setTitle("Sitnikov's Problem Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void calculate() {

        powOfXOfSphere1 = Math.pow(xOfSphere1, 2);
        powOfYOfSphere1 = Math.pow(yOfSphere1, 2);
        powerOfPosition = Math.pow(powOfXOfSphere1 + powOfYOfSphere1, 1.5);

        // calculate v for M1:
        vxOfSphere1 = vxOfSphere1 + ((-1) * GM / (2 * powerOfPosition)) * xOfSphere1 * timeStep;
        xOfSphere1 = xOfSphere1 + vxOfSphere1;

        // calculate coordinates for M1:
        vyOfSphere1 = vyOfSphere1 + ((-1) * GM / (2 * powerOfPosition)) * yOfSphere1 * timeStep;
        yOfSphere1 = yOfSphere1 + vyOfSphere1;

        /**********************************************/

        powOfXOfSphere2 = Math.pow(xOfSphere2, 2);
        powOfYOfSphere2 = Math.pow(yOfSphere2, 2);
        powerOfPosition = Math.pow(powOfXOfSphere2 + powOfYOfSphere2, 1.5);

        // calculate v for M2:
        vxOfSphere2 = vxOfSphere2 + ((-1) * GM / (2 * powerOfPosition)) * xOfSphere2 * timeStep;
        xOfSphere2 = xOfSphere2 + vxOfSphere2;

        // calculate coordinates for M1:
        vyOfSphere2 = vyOfSphere2 + ((-1) * GM / (2 * powerOfPosition)) * yOfSphere2 * timeStep;
        yOfSphere2 = yOfSphere2 + vyOfSphere2;

        move(xOfSphere1, yOfSphere1, xOfSphere2, yOfSphere2);
    }

    private void move(double x1, double y1, double x2, double y2) {

        sphere1.setTranslateX(offsetX + x1);
        sphere1.setTranslateY(offsetY + y1);

        sphere2.setTranslateX(offsetX + x2);
        sphere2.setTranslateY(offsetY + y2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
