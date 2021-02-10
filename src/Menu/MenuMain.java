package Menu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MenuMain extends Application {
    private static final double SCREEN_WIDTH = 250;
    private static final double SCREEN_HEIGHT = 400;

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Sitinikov's Problem Simulator Menu");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
