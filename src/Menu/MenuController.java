package Menu;

import Simulation.Simulation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuController {

    @FXML
    private TextField timeStepTextField;
    @FXML
    private TextField GMTextField;
    @FXML
    private TextField GmTextField;
    @FXML
    private TextField m1InitialPosTextField;
    @FXML
    private TextField m3InitialPosTextField;
    @FXML
    private TextField m1InitialVelocityTextField;
    @FXML
    private TextField m3InitialVelocityTextField;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Label errorLabel;
    @FXML
    private CheckBox drawTrackCheckBox;


    private Map<String, String> inputFieldMap;
    private String drawTrack = "false";


    public MenuController() {
    }

    public Map<String, String> getInputFieldMap() {
        return inputFieldMap;
    }

    @FXML
    void onStartButtonClicked() {

        List<TextField> textFieldList = getAllTextFields();
        boolean allCorrectFilled = checkTextField(textFieldList);

        drawTrack = String.valueOf(drawTrackCheckBox.isSelected());

        if (allCorrectFilled) {
            inputFieldMap = getInputFieldData();
            showSimulatorWindow();
        } else {
            errorLabel.setText("Enter valid number!");
        }

    }

    private List<TextField> getAllTextFields() {
        List<TextField> list = new ArrayList<>();
        list.add(timeStepTextField);
        list.add(GMTextField);
        list.add(GmTextField);
        list.add(m1InitialPosTextField);
        list.add(m1InitialVelocityTextField);
        list.add(m3InitialPosTextField);
        list.add(m3InitialVelocityTextField);
        return list;
    }

    private boolean checkTextField(List<TextField> textFieldList) {
        for (TextField textField : textFieldList) {
            if (textField.getText().equals("") ||
                    !NumberUtils.isCreatable(textField.getText())) {
                return false;
            }
        }
        return true;
    }

    private void showSimulatorWindow() {

        Stage stage = new Stage();
        Simulation simulation = new Simulation(getInputFieldMap());
        simulation.start(stage);
    }


    private @NotNull Map<String, String> getInputFieldData() {

        Map<String, String> map = new HashMap<>();
        map.put("time_step", timeStepTextField.getText());
        map.put("GM", GMTextField.getText());
        map.put("Gm", GmTextField.getText());
        map.put("x1", m1InitialPosTextField.getText());
        map.put("y1", "0");
        map.put("vX1", "0");
        map.put("vY1", m1InitialVelocityTextField.getText());
        map.put("x2", "-" + m1InitialPosTextField.getText());
        map.put("y2", "0");
        map.put("vX2", "0");
        map.put("vY2", "-" + m1InitialVelocityTextField.getText());
        map.put("z3", m3InitialPosTextField.getText());
        map.put("vZ3", m3InitialVelocityTextField.getText());
        map.put("draw track", drawTrack);

        return map;
    }

    public void onStopButtonClicked(ActionEvent actionEvent) {
        // stop animation

        startButton.setDisable(false);
        stopButton.setDisable(true);
    }

}
