package Simulation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.math.MathContext;

public class LeftController {

    @FXML
    private Label X1Label;

    @FXML
    private Label X2Label;

    @FXML
    private Label VX1Label;

    @FXML
    private Label VX2Label;

    @FXML
    private Label Y1Label;

    @FXML
    private Label Y2Label;

    @FXML
    private Label VY1Label;

    @FXML
    private Label VY2Label;

    @FXML
    private Label Z3Label;

    @FXML
    private Label VZ3Label;

    @FXML
    private Label cycleCountLabel;

    public void showData(double x1, double y1,
                         double x2, double y2, double z3,
                         double vX1, double vY1,
                         double vX2, double vY2, double vZ3,
                         int cycleCount) {
        X1Label.setText(String.valueOf(roundToThreeSig(x1)));
        Y1Label.setText(String.valueOf(roundToThreeSig(y1)));
        X2Label.setText(String.valueOf(roundToThreeSig(x2)));
        Y2Label.setText(String.valueOf(roundToThreeSig(y2)));
        Z3Label.setText(String.valueOf(roundToThreeSig(z3)));
        VX1Label.setText(String.valueOf(roundToThreeSig(vX1)));
        VY1Label.setText(String.valueOf(roundToThreeSig(vY1)));
        VX2Label.setText(String.valueOf(roundToThreeSig(vX2)));
        VY2Label.setText(String.valueOf(roundToThreeSig(vY2)));
        VZ3Label.setText(String.valueOf(roundToThreeSig(vZ3)));
        cycleCountLabel.setText(String.valueOf(cycleCount));
    }

    private double roundToThreeSig(double value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.round(new MathContext(5));
        return bigDecimal.doubleValue();
    }
}
