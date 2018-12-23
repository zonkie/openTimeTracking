package eu.domroese.opentimetracking;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    private static Stage primaryStage; // **Declare static Stage**


    public static void main(String[] args) {
        launch(args);
    }

    private void setPrimaryStage(Stage stage) {
        Main.primaryStage = stage;
    }
    static public Stage getPrimaryStage() {
        return Main.primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        setPrimaryStage(primaryStage); // **Set the Stage**

        Parent root = FXMLLoader.load(getClass().getResource("timeTrackingOverlay.fxml"));
        primaryStage.setTitle("OpenTimeTracking");

        primaryStage.setMinWidth(315);
        primaryStage.setMinHeight(600);

        Scene mainScene = new Scene(root);
        mainScene.getStylesheets().add("eu/domroese/opentimetracking/assets/style.css");

        primaryStage.setScene(mainScene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();


        Controller mainController = new Controller();
        mainController.init(primaryStage);
    }
}
