package Simulation;

import WorkBook.ExcelRecord;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Simulation extends Application {

    private static final String IC_SHEET = "Initial Conditions";
    private static final String DATA_SHEET = "Data";
    private static final double SUBSCENE_WIDTH = 800;
    private static final double SUBSCENE_HEIGHT = 800;

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

    private ExcelRecord excelInstance;
    private final HashMap<String, String> dataMap;
    private LinkedList<Double> x1List0, y1List3;
    private LinkedList<Double> x1List2, y1List1;
    private LinkedList<Double> x2List0, y2List3;
    private LinkedList<Double> x2List2, y2List1;
    private LinkedList<Double> z3List0;
    private LinkedList<Double> z3List2;
    private LinkedList<Double> z3List3;
    private LinkedList<Double> z3List1;
    private LinkedList<Double> vy1List0, vx1List1, vy1List2, vx1List3;
    private LinkedList<Double> vy2List0, vx2List1, vy2List2, vx2List3;
    private LinkedList<Double> vz3List0, vz3List1, vz3List2, vz3List3;
    private int cycleCount = 0, xCount = 0, yCount = 0;
    private boolean touchedYAxis = true;
    private boolean touchedXAxis = false;
    private double minY1, maxY1, minY2, maxY2;
    private double minvX1, maxvX1, minvX2, maxvX2;

    private FXMLLoader loader;

    // Constructor receives the input fields
    public Simulation(Map<String, String> inputFieldMap) {

        this.timeStep = Double.parseDouble(inputFieldMap.get("time_step"));
        this.GM = Double.parseDouble(inputFieldMap.get("GM"));
        this.Gm = Double.parseDouble(inputFieldMap.get("Gm"));
        this.x1 = Double.parseDouble(inputFieldMap.get("x1"));
        this.y1 = Double.parseDouble(inputFieldMap.get("y1"));
        this.vX1 = Double.parseDouble(inputFieldMap.get("vX1"));
        this.vY1 = Double.parseDouble(inputFieldMap.get("vY1"));
        this.x2 = Double.parseDouble(inputFieldMap.get("x2"));
        this.y2 = Double.parseDouble(inputFieldMap.get("y2"));
        this.vX2 = Double.parseDouble(inputFieldMap.get("vX2"));
        this.vY2 = Double.parseDouble(inputFieldMap.get("vY2"));
        this.z3 = Double.parseDouble(inputFieldMap.get("z3"));
        this.vZ3 = Double.parseDouble(inputFieldMap.get("vZ3"));

        drawTrack = inputFieldMap.get("draw track").equals("true");

        dataMap = new HashMap<>();
        dataMap.putAll(inputFieldMap);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Init:
        initList();
        initExcel();
        setAppearances();
        setAnimation(); // 1ms per calculation

        Scene scene = setupScene();
        scene.widthProperty().addListener((observableValue,
                                           oldSceneWidth,
                                           newSceneWidth) -> subScene.widthProperty().set(scene.getWidth() - 250));
        scene.heightProperty().addListener((observableValue,
                                            oldSceneHeight,
                                            newSceneHeight) -> subScene.heightProperty().set(scene.getHeight()));
        setCamera(primaryStage);

        primaryStage.setTitle("Sitnikov's Problem Simulator");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST,
                windowEvent -> {
                    writeToExcel();
                    excelInstance.saveFile();
                });
        primaryStage.show();
    }

    private void writeToExcel() {
        excelInstance.writeList(DATA_SHEET, 0, x1List0);
        excelInstance.writeList(DATA_SHEET, 1, y1List1);
        excelInstance.writeList(DATA_SHEET, 2, x1List2);
        excelInstance.writeList(DATA_SHEET, 3, y1List3);

        excelInstance.writeList(DATA_SHEET, 5, x2List0);
        excelInstance.writeList(DATA_SHEET, 6, y2List1);
        excelInstance.writeList(DATA_SHEET, 7, x2List2);
        excelInstance.writeList(DATA_SHEET, 8, y2List3);

        excelInstance.writeList(DATA_SHEET, 10, z3List0);
        excelInstance.writeList(DATA_SHEET, 11, z3List1);
        excelInstance.writeList(DATA_SHEET, 12, z3List2);
        excelInstance.writeList(DATA_SHEET, 13, z3List3);

        excelInstance.writeList(DATA_SHEET, 15, vy1List0);
        excelInstance.writeList(DATA_SHEET, 16, vx1List1);
        excelInstance.writeList(DATA_SHEET, 17, vy1List2);
        excelInstance.writeList(DATA_SHEET, 18, vx1List3);

        excelInstance.writeList(DATA_SHEET, 20, vy2List0);
        excelInstance.writeList(DATA_SHEET, 21, vx2List1);
        excelInstance.writeList(DATA_SHEET, 22, vy2List2);
        excelInstance.writeList(DATA_SHEET, 23, vx2List3);

        excelInstance.writeList(DATA_SHEET, 25, vz3List0);
        excelInstance.writeList(DATA_SHEET, 26, vz3List1);
        excelInstance.writeList(DATA_SHEET, 27, vz3List2);
        excelInstance.writeList(DATA_SHEET, 28, vz3List3);
    }

    private void initList() {
        x1List0 = new LinkedList<>();
        y1List3 = new LinkedList<>();
        x2List0 = new LinkedList<>();
        y2List3 = new LinkedList<>();
        x1List2 = new LinkedList<>();
        y1List1 = new LinkedList<>();
        x2List2 = new LinkedList<>();
        y2List1 = new LinkedList<>();
        z3List0 = new LinkedList<>();
        z3List3 = new LinkedList<>();
        z3List2 = new LinkedList<>();
        z3List1 = new LinkedList<>();
        vy1List0 = new LinkedList<>();
        vx1List1 = new LinkedList<>();
        vy1List2 = new LinkedList<>();
        vx1List3 = new LinkedList<>();
        vy2List0 = new LinkedList<>();
        vx2List1 = new LinkedList<>();
        vy2List2 = new LinkedList<>();
        vx2List3 = new LinkedList<>();
        vz3List0 = new LinkedList<>();
        vz3List1 = new LinkedList<>();
        vz3List2 = new LinkedList<>();
        vz3List3 = new LinkedList<>();
        x1List0.add(x1);
        x2List0.add(x2);
        z3List0.add(z3);
        vy1List0.add(vY1);
        vy2List0.add(vY2);
        vz3List0.add(vZ3);
    }

    private void initExcel() throws IOException {
        excelInstance = ExcelRecord.getInstance();
        excelInstance.createSheet(DATA_SHEET);
        Sheet sheet = excelInstance.getSheet(IC_SHEET);
        excelInstance.writeInitialConditions(sheet, dataMap);
        sheet = excelInstance.getSheet(DATA_SHEET);
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("x1 (0T)");
        row.createCell(1).setCellValue("y1 (1/4T)");
        row.createCell(2).setCellValue("x1 (2/4T)");
        row.createCell(3).setCellValue("y1 (3/4T)");

        row.createCell(5).setCellValue("x2 (0T)");
        row.createCell(6).setCellValue("y2 (1/4T)");
        row.createCell(7).setCellValue("x2 (2/4T)");
        row.createCell(8).setCellValue("y2 (3/4T)");

        row.createCell(10).setCellValue("z3 (0T)");
        row.createCell(11).setCellValue("z3 (1/4T)");
        row.createCell(12).setCellValue("z3 (2/4T)");
        row.createCell(13).setCellValue("z3 (3/4T)");

        row.createCell(15).setCellValue("vy1 (0T)");
        row.createCell(16).setCellValue("vx1 (1/4T)");
        row.createCell(17).setCellValue("vy1 (2/4T)");
        row.createCell(18).setCellValue("vx1 (3/4T)");

        row.createCell(20).setCellValue("vy2 (0T)");
        row.createCell(21).setCellValue("vx2 (1/4T)");
        row.createCell(22).setCellValue("vy2 (2/4T)");
        row.createCell(23).setCellValue("vx2 (3/4T)");

        row.createCell(25).setCellValue("vz3 (0T)");
        row.createCell(26).setCellValue("vz3 (1/4T)");
        row.createCell(27).setCellValue("vz3 (2/4T)");
        row.createCell(28).setCellValue("vz3 (3/4T)");
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
        cameraTranslate = new Translate(2000, 3430, -1470);
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
        sphere1.setRadius(50);
        sphere2.setRadius(50);
        sphere3.setRadius(20);

        xAxis = createCylinderLine(new Point3D(-2000, 0, 0), new Point3D(2000, 0, 0));
        yAxis = createCylinderLine(new Point3D(0, -2000, 0), new Point3D(0, 2000, 0));
        zAxis = createCylinderLine(new Point3D(0, 0, -1000), new Point3D(0, 0, 1000));

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
        Timeline animation = new Timeline(new KeyFrame(Duration.millis(1), e -> {
            try {
                calculate();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    private Scene setupScene() throws IOException {
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
        loader = new FXMLLoader(getClass().getResource("leftPane.fxml"));
        Parent leftPane = loader.load();
//        leftPane.setMinWidth(250);
//        leftPane.setMaxWidth(250);

        // Create splitPane to hold both controls and the subscene with displayPane.
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPane, subScene);

        // Create a main scene to hold the splitPane.
        return new Scene(splitPane);
    }

    private void calculate() throws IOException {

        int cycleCounts = countCycle();
        LeftController leftController = loader.getController();
        leftController.showData(x1, y1, x2, y2, z3, vX1, vY1, vX2, vY2, vZ3, cycleCounts);
        move(x1, y1, x2, y2, z3);

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
    }

    private int countCycle() {
        if (yCount == 0) {

            if (maxY1 < y1)
                maxY1 = y1;

            if (y2 < minY2)
                minY2 = y2;

            if (maxvX1 < vX1)
                maxvX1 = vX1;

            if (minvX2 > vX2)
                minvX2 = vX2;
        } else if (yCount == 1) {

            if (y1 < minY1)
                minY1 = y1;

            if (maxY2 < y2)
                maxY2 = y2;

            if (minvX1 > vX1)
                minvX1 = vX1;

            if (maxvX2 < vX2)
                maxvX2 = vX2;
        }

        if (Math.round(y1) == 0 && !touchedYAxis) {
            yCount++;
            touchedYAxis = true;

            if (yCount == 1) {
                // 2/4T
                leaveMark();

                y1List1.add(maxY1);
                y2List1.add(minY2);
                z3List1.add(null);
                vx1List1.add(maxvX1);
                vx2List1.add(minvX2);
                vz3List1.add(null);

                x1List2.add(x1);
                x2List2.add(x2);
                z3List2.add(z3);
                vy1List2.add(vY1);
                vy2List2.add(vY2);
                vz3List2.add(vZ3);

            } else if (yCount == 2) {
                // One full cycle completes
                yCount = 0;
                cycleCount++;
                leaveMark();
                x1List0.add(x1);
                x2List0.add(x2);
                z3List0.add(z3);
                vy1List0.add(vY1);
                vy2List0.add(vY2);
                vz3List0.add(vZ3);

                y1List3.add(minY1);
                y2List3.add(maxY2);
                z3List3.add(null);
                vx1List3.add(minvX1);
                vx2List3.add(maxvX2);
                vz3List3.add(null);
            }
        } else if (Math.round(y1) != 0 && touchedYAxis) {
            touchedYAxis = false;
        }

        if (Math.round(x1) == 0 && !touchedXAxis) {
            xCount++;
            touchedXAxis = true;
            if (xCount == 1) {
                // 1/4 T (not true)
                leaveMark();
            } else if (xCount == 2) {
                // 3/4 T
                leaveMark();
                xCount = 0;
//                y1List3.add(y1);
//                y2List3.add(y2);
//                z3List3.add(z3);
//                vx1List3.add(vX1);
//                vx2List3.add(vX2);
//                vz3List3.add(vZ3);
            }
        } else if (Math.round(x1) != 0 && touchedXAxis) {
            touchedXAxis = false;
        }
        return cycleCount;
    }

    private void leaveMark() {
        Sphere sphere1 = new Sphere(2);
        sphere1.translateXProperty().set(x1);
        sphere1.translateYProperty().set(y1);

        Sphere sphere2 = new Sphere(2);
        sphere2.translateXProperty().set(x2);
        sphere2.translateYProperty().set(y2);
        displayPane.getChildren().addAll(sphere1, sphere2);
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