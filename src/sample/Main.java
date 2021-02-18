package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;

public class Main extends Application {


    private static final double SCREEN_WIDTH = 1000;
    private static final double SCREEN_HEIGHT = 800;
    private static final double SUBSCENE_WIDTH = 800;
    private static final double SUBSCENE_HEIGHT = 800;
    private static final double FAR_CLIP = 20000;        // Maximum range of camera view
    private static final double NEAR_CLIP = 0.1;         // Minimum range of camera view
    private static final String PATH_NAME = "results.xlsx";

    // Initial conditions:
    private static final double TIMESTEP = 0.0001;      // Customizable
    private static final double GM = 1;                 // Customizable, Gravitational Constant (M1 &2)
    private static final double Gm = 0;                 // Customizable, Gravitational Constant (M3)
    private static final double LARGE_RADIUS = 0.1;     // Radius for M1 & M2
    private static final double SMALL_RADIUS = 0.05;     // Radius for M3
    private static final double ECCENTRICITY = 0.2;
    private static final double SEMI_MAJOR_AXIS = 1;
    private static final double ANOMALY_STEP = 0.1;

    private double x1 = -1, y1 = 0;
    private double x2 = 1, y2 = 0;
    private double vX1 = 0, vY1 = -0.5;
    private double vX2 = 0, vY2 = -0.5;
    private double z3 = 1.5;                     // Customizable
    private double vZ3 = 0;                      // Customizable, but depends on zOfSphere, max when zOfSphere == 0
    private double anomaly1 = 180;
    private double anomaly2 = 180;

    private final Sphere sphere1 = new Sphere();
    private final Sphere sphere2 = new Sphere();
    private final Sphere sphere3 = new Sphere();


    private Rotate rotateX, rotateY, rotateZ;
    public static Camera camera = new PerspectiveCamera(true); // true --> eye fixed on (0, 0, 0)
    private Translate translate;

    private final Label currentSphere1XLabel = new Label();
    private final Label currentSphere1YLabel = new Label();
    private final Label currentSphere2XLabel = new Label();
    private final Label currentSphere2YLabel = new Label();
    private final Label currentSphere3ZLabel = new Label();

    private double minX1 = x1, maxX1;
    private double minX2, maxX2 = x2;
    private double maxZ3 = z3;
    private final Label minSphere1XLabel = new Label();
    private final Label maxSphere1XLabel = new Label();
    private final Label minSphere2XLabel = new Label();
    private final Label maxSphere2XLabel = new Label();
    private final Label maxSphere3ZLabel = new Label();

    private int cycleCount = 0, yCount = 0;
    private final Label cycleCountLabel = new Label();
    private boolean touchedYAxis = true;

    private XSSFWorkbook workbook;
    private int startingRowCount = 2;
    private Pane displayPane;
    private Scene scene;
    private int rowNum = 1;
    private boolean removed = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Load Excel file "Result.xlsx", create one if not found:
        File file = new File(PATH_NAME);
        if (file.exists()) {
            workbook = new XSSFWorkbook(new FileInputStream(PATH_NAME));
        } else {
            workbook = new XSSFWorkbook();
            saveExcel(workbook);
        }


        iniExcelFile();
        setRadius();
        setAppearances();
        setAnimation();  // this starts the calculation, animation and file output
        setupUI();
        setCamera();

        // Camera controls:
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case A -> translate.setX(translate.getX() - 1);
                case D -> translate.setX(translate.getX() + 1);
                case W -> translate.setY(translate.getY() + 1);
                case S -> translate.setY(translate.getY() - 1);
                case LEFT -> rotateY.setAngle(rotateY.getAngle() + 1);
                case RIGHT -> rotateY.setAngle(rotateY.getAngle() - 1);
                case UP -> rotateX.setAngle(rotateX.getAngle() - 1);
                case DOWN -> rotateX.setAngle(rotateX.getAngle() + 1);
                case I -> translate.setZ(translate.getZ() + 10);
                case O -> translate.setZ(translate.getZ() - 10);
            }
        });
        primaryStage.setOnCloseRequest(windowEvent -> saveExcel(workbook));

        primaryStage.setTitle("Sitnikov's Problem Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setRadius() {
        sphere1.setRadius(LARGE_RADIUS);
        sphere2.setRadius(LARGE_RADIUS);
        sphere3.setRadius(SMALL_RADIUS);
    }

    private void setAppearances() {
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
    }

    private void iniExcelFile() {

        Sheet iniSheet, posSheet;

        if (workbook.getSheet("Test") == null) {
            iniSheet = workbook.createSheet("Test");
        } else {
            iniSheet = workbook.getSheet("Test");
        }

        if (workbook.getSheet("current position") == null) {
            posSheet = workbook.createSheet("current position");
        } else {
            posSheet = workbook.getSheet("current position");
        }

        if (!removed) {
            Iterator<Row> rowIte = posSheet.iterator();
            while (rowIte.hasNext()) {
                rowIte.next();
                rowIte.remove();
            }

            rowIte = iniSheet.iterator();
            while (rowIte.hasNext()) {
                rowIte.next();
                rowIte.remove();
            }
            removed = true;
        }

        // Page 1:
        Row row1 = iniSheet.createRow(0);
        row1.createCell(0).setCellValue("Timestep");
        row1.createCell(1).setCellValue(TIMESTEP);
        row1.createCell(3).setCellValue("x1");
        row1.createCell(4).setCellValue("y1");
        row1.createCell(5).setCellValue("vx1");
        row1.createCell(6).setCellValue("vy1");
        row1.createCell(8).setCellValue("x2");
        row1.createCell(9).setCellValue("y2");
        row1.createCell(10).setCellValue("vx2");
        row1.createCell(11).setCellValue("vy2");
        row1.createCell(13).setCellValue("z3");
        row1.createCell(14).setCellValue("vz3");

        Row row2 = iniSheet.createRow(1);
        row2.createCell(0).setCellValue("GM");
        row2.createCell(1).setCellValue(GM);
        row2.createCell(3).setCellValue(x1);
        row2.createCell(4).setCellValue(y1);
        row2.createCell(5).setCellValue(vX1);
        row2.createCell(6).setCellValue(vY1);
        row2.createCell(8).setCellValue(x2);
        row2.createCell(9).setCellValue(y2);
        row2.createCell(10).setCellValue(vX2);
        row2.createCell(11).setCellValue(vY2);
        row2.createCell(13).setCellValue(z3);
        row2.createCell(14).setCellValue(vZ3);

        Row row3 = iniSheet.createRow(2);
        row3.createCell(0).setCellValue("Gm");
        row3.createCell(1).setCellValue(Gm);

        Row row4 = iniSheet.createRow(3);
        row4.createCell(0).setCellValue("Cycle counts");
        row4.createCell(1).setCellValue(cycleCount);


        // Page 2:
        Row p2Row1 = posSheet.createRow(0);
        p2Row1.createCell(0).setCellValue("x1");
        p2Row1.createCell(1).setCellValue("y1");
        p2Row1.createCell(2).setCellValue("x2");
        p2Row1.createCell(3).setCellValue("y2");
        p2Row1.createCell(4).setCellValue("z3");
        p2Row1.createCell(5).setCellValue("vx1");
        p2Row1.createCell(6).setCellValue("vy1");
        p2Row1.createCell(7).setCellValue("vx2");
        p2Row1.createCell(8).setCellValue("vy2");
        p2Row1.createCell(9).setCellValue("vz3");

        saveExcel(workbook);
    }

    private void setAnimation() {
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(1), e -> calculate()));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    private void setupUI() {
        // Create pane to display the spheres and lines
        displayPane = new Pane();
        displayPane.getChildren().add(sphere1);
        displayPane.getChildren().add(sphere2);
        displayPane.getChildren().add(sphere3);
//        displayPane.getChildren().add(new Line(SUBSCENE_WIDTH, 0, -1 * SUBSCENE_WIDTH, 0));
//        displayPane.getChildren().add(new Line(0, SUBSCENE_HEIGHT, 0, -1 * SUBSCENE_HEIGHT));

        // Create subscene to hold the displayPane and attach camera
        SubScene subScene = new SubScene(displayPane, SUBSCENE_WIDTH, SUBSCENE_HEIGHT);
        subScene.setCamera(camera);

        // Create a VBox to hold all widgets
        VBox leftControl = new VBox();

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

        // Create splitPane to hold both controls and the subscene with displayPane
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftControl, subScene);

        // Create a main scene to hold the splitPane
        scene = new Scene(splitPane, SCREEN_WIDTH, SCREEN_HEIGHT);
        scene.setFill(Color.SILVER);
    }

    private void setCamera() {
        // For timestep = 0.75:
        // TranslateX: 60.0, TranslateY: 935.0, TranslateZ: -1030.0,
        // RotateX: 70.0, RotateY: 0.0, RotateZ: 0.0

        // TranslateX: 0.0, TranslateY: 940.0, TranslateZ: 335.0,
        // RotateX: 70.0, RotateY: 0.0, RotateZ: 0.0

        // MOVE CAMERA
        camera.getTransforms().addAll(
                rotateX = new Rotate(70, Rotate.X_AXIS),
                rotateY = new Rotate(0, Rotate.Y_AXIS),
                rotateZ = new Rotate(0, Rotate.Z_AXIS),
                translate = new Translate(0, 940, 335));

        // Move camera to a good view
        camera.translateZProperty().set(-1000);

        // Set the clipping planes
        camera.setNearClip(NEAR_CLIP);
        camera.setFarClip(FAR_CLIP);
        ((PerspectiveCamera) camera).setFieldOfView(35);
    }

    private void saveExcel(XSSFWorkbook workbook) {
        try {
            workbook.write(new FileOutputStream(PATH_NAME));
            // System.out.println("Excel created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void calculate() {
//
//        // record current locations:
//        double lastX1 = x1;
//        double lastY1 = y1;
//        double lastX2 = x2;
//        double lastY2 = y2;
//        double lastZ3 = z3;
//
//
//        // record current velocities:
//        double lastVX1 = vX1;
//        double lastVY1 = vY1;
//        double lastVX2 = vX2;
//        double lastVY2 = vY2;
//        double lastVZ3 = vZ3;
//
//
//        // Calculate and update velocities and locations (M1):
//        double denominatorForM1 = 2 * Math.pow(SEMI_MAJOR_AXIS * (1 - ECCENTRICITY * Math.cos(anomaly1)), 3);
//        vX1 += (-1 / denominatorForM1) * x1 * TIMESTEP;
//        x1 += lastVX1 * TIMESTEP;
//
//        vY1 += (-1 / denominatorForM1) * y1 * TIMESTEP;
//        y1 += lastVY1 * TIMESTEP;
//
//
//        // Calculate and update velocities and locations (M2):
//        double denominatorForM2 = 2 * Math.pow(SEMI_MAJOR_AXIS * (1 - ECCENTRICITY * Math.cos(anomaly2)), 3);
//        vX2 += (-1 / denominatorForM2) * x2 * TIMESTEP;
//        x2 += lastVX2 * TIMESTEP;
//
//        vY2 += (-1 / denominatorForM2) * y2 * TIMESTEP;
//        y2 += lastVY2 * TIMESTEP;
//
//
//        // Calculate and update velocities and locations (M3):
//        double squareOfZ = Math.pow(z3, 2);
//        double squareOfAnomaly = Math.pow(SEMI_MAJOR_AXIS * (1 - ECCENTRICITY * Math.cos(anomaly1)), 2);
//        double denominatorForM3 = Math.pow(squareOfZ + squareOfAnomaly, 1.5);
//        vZ3 += (-1) * ((4 * GM) / denominatorForM3) * z3 * TIMESTEP;
//        z3 += lastVZ3 * TIMESTEP;
//
//        if (anomaly1 >= 2 * Math.PI) {
//            anomaly1 = 0;
//        } else {
//            anomaly1 += ANOMALY_STEP;
//        }
//        if (anomaly2 >= 2 * Math.PI) {
//            anomaly2 = 0;
//        } else {
//            anomaly2 += ANOMALY_STEP;
//        }
//
//        countCycles();
//        move(x1, y1, x2, y2, z3);
//        writeCurrentPos(x1, y1, x2, y2);
////        drawTracks(x1, y1, x2, y2, lastX1, lastY1, lastX2, lastY2);
//    }


    private void calculate() {

        double lastX1 = x1;
        double lastY1 = y1;
        double lastX2 = x2;
        double lastY2 = y2;
        double lastVX1 = vX1;
        double lastVY1 = vY1;
        double lastVX2 = vX2;
        double lastVY2 = vY2;
        double lastVZ3 = vZ3;

        double up = SEMI_MAJOR_AXIS * (1 - Math.pow(ECCENTRICITY, 2));
        double down1 = 1 + ECCENTRICITY * Math.cos(anomaly1);
        double down2 = 1 + ECCENTRICITY * Math.cos(anomaly2);
        double denominator1 = 2 * Math.pow((up / down1), 3);
        double denominator2 = 2 * Math.pow((up / down2), 3);
        double left3 = Math.pow(up / down1, 2);
        double right3 = Math.pow(z3, 2);
        double denominator3 = Math.pow(left3 + right3, 1.5);

//        double denominator1 = 2 * Math.pow((SEMI_MAJOR_AXIS * (1 - Math.pow(ECCENTRICITY, 2)))
//                / (1 + ECCENTRICITY * Math.cos(anomaly1)), 3);
//        double denominator2 = 2 * Math.pow((SEMI_MAJOR_AXIS * (1 - Math.pow(ECCENTRICITY, 2)))
//                / (1 + ECCENTRICITY * Math.cos(anomaly2)), 3);
//        double denominator3 = Math.pow(z3, 2) + Math.pow((SEMI_MAJOR_AXIS * (1 - Math.pow(ECCENTRICITY, 2)))
//                / (1 + ECCENTRICITY * Math.cos(anomaly2)), 2);
//        double powOfDenominator3 = Math.pow(denominator3, 1.5);

        vX1 = lastVX1 + -1 * (GM / denominator1) * x1 * TIMESTEP;
        vY1 = lastVY1 + -1 * (GM / denominator1) * y1 * TIMESTEP;
        x1 = x1 + lastVX1 * TIMESTEP;
        y1 = y1 + lastVY1 * TIMESTEP;

        vX2 = lastVX2 + -1 * (GM / denominator2) * x2 * TIMESTEP;
        vY2 = lastVY2 + -1 * (GM / denominator2) * y2 * TIMESTEP;
        x2 = x2 + lastVX2 * TIMESTEP;
        y2 = y2 + lastVY2 * TIMESTEP;

        vZ3 = lastVZ3 + -1 * ((4 * GM) / denominator3) * z3 * TIMESTEP;
        z3 = z3 + lastVZ3 * TIMESTEP;

        if (anomaly1 >= 360) {
            anomaly1 = 0;
        } else {
            anomaly1 += ANOMALY_STEP;
        }
        if (anomaly2 >= 360) {
            anomaly2 = 0;
        } else {
            anomaly2 += ANOMALY_STEP;
        }

        move(x1, y1, x2, y2, z3);
        writeCurrentPos(x1, y1, x2, y2);
//        drawTracks(x1, y1, x2, y2, lastX1, lastY1, lastX2, lastY2);
//        checkCameraProperties();
    }

    private void countCycles() {
        if (Math.round(y1) == 0 && !touchedYAxis) {

            yCount++;
            touchedYAxis = true;

            if (yCount == 2) {
                // One cycle completes
                yCount = 0;
                cycleCount++;

                // Record the results into "Result.xlsx":
                XSSFSheet sheet = workbook.getSheet("Test");
                Cell cycleCountCell = sheet.getRow(3).getCell(1);

                Row startingRow;
                if (startingRowCount <= 3) {
                    startingRow = sheet.getRow(startingRowCount);
                } else {
                    startingRow = sheet.createRow(startingRowCount);
                }
                startingRow.createCell(3).setCellValue(x1);
                startingRow.createCell(4).setCellValue(y1);
                startingRow.createCell(5).setCellValue(vX1);
                startingRow.createCell(6).setCellValue(vY1);
                startingRow.createCell(8).setCellValue(x2);
                startingRow.createCell(9).setCellValue(y2);
                startingRow.createCell(10).setCellValue(vX2);
                startingRow.createCell(11).setCellValue(vY2);
                startingRow.createCell(13).setCellValue(z3);
                startingRow.createCell(14).setCellValue(vZ3);

                cycleCountCell.setCellValue(cycleCount);
                saveExcel(workbook);

                startingRowCount++;
                System.out.println(startingRowCount);
            }
        } else if (Math.round(y1) != 0 && touchedYAxis) {
            touchedYAxis = false;
        }
    }

    private void writeCurrentPos(double x1, double y1, double x2, double y2) {

        Sheet posSheet;
        if (workbook.getSheet("current position") == null) {
            posSheet = workbook.createSheet("current position");
        } else {
            posSheet = workbook.getSheet("current position");
        }

        Row row = posSheet.createRow(rowNum);
        row.createCell(0).setCellValue(x1);
        row.createCell(1).setCellValue(y1);
        row.createCell(2).setCellValue(x2);
        row.createCell(3).setCellValue(y2);
        rowNum++;
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
        sphere3.translateXProperty().set(x3);
        double y3 = 0;
        sphere3.translateYProperty().set(y3);
        sphere3.translateZProperty().set(z);

        // Update the labels:
        // Multiply Y and Z component by (-1) to match our views
        currentSphere1XLabel.setText("Current X of M1: " + roundToThreeSig(this.x1));
        currentSphere1YLabel.setText("Current Y of M1: " + roundToThreeSig(this.y1) * -1);
        currentSphere2XLabel.setText("Current X of M2: " + roundToThreeSig(this.x2));
        currentSphere2YLabel.setText("Current Y of M2: " + roundToThreeSig(this.y2) * -1);
        currentSphere3ZLabel.setText("Current Z of M3: " + roundToThreeSig(z3) * -1);


        minX1 = Math.min(minX1, this.x1);
        maxX1 = Math.max(maxX1, this.x1);
        minX2 = Math.min(minX2, this.x2);
        maxX2 = Math.max(maxX2, this.x2);
        maxZ3 = Math.max(maxZ3, z3);
        minSphere1XLabel.setText("Min x of M1: " + roundToThreeSig(minX1));
        maxSphere1XLabel.setText("Max x of M1: " + roundToThreeSig(maxX1));
        minSphere2XLabel.setText("Min x of M2: " + roundToThreeSig(minX2));
        maxSphere2XLabel.setText("Max x of M2: " + roundToThreeSig(maxX2));
        maxSphere3ZLabel.setText("Max z of M3: " + roundToThreeSig(maxZ3));
        cycleCountLabel.setText("Cycle count: " + cycleCount);

//        checkCameraProperties();
    }

    private void drawTracks(double x1, double y1, double x2, double y2,
                            double lastX1, double lastY1, double lastX2, double lastY2) {

        Line line1 = new Line(lastX1, lastY1, x1, y1);
        Line line2 = new Line(lastX2, lastY2, x2, y2);
        line1.setStrokeWidth(0.03);
        line2.setStrokeWidth(0.03);

        displayPane.getChildren().add(line1);
        displayPane.getChildren().add(line2);
    }

    private void checkCameraProperties() {
        System.out.print("TranslateX: " + translate.getX());
        System.out.print(", TranslateY: " + translate.getY());
        System.out.print(", TranslateZ: " + translate.getZ());
        System.out.print(", RotateX: " + rotateX.getAngle());
        System.out.print(", RotateY: " + rotateY.getAngle());
        System.out.println(", RotateZ: " + rotateZ.getAngle());
    }

    private double roundToThreeSig(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.round(new MathContext(3));
        return bigDecimal.doubleValue();
    }

}