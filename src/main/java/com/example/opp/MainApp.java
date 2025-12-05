package com.example.opp;

import com.example.opp.config.AppConfig;
import com.example.opp.database.DatabaseManager;
import com.example.opp.util.Constants;
import com.example.opp.view.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void init() {
        AppConfig.load();
        try {
            DatabaseManager.getInstance().connect();
        } catch (Exception e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Failed to connect to database");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        ViewManager.setPrimaryStage(stage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.LOGIN_VIEW));
        Scene scene = new Scene(loader.load(), Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource(Constants.MAIN_STYLE).toExternalForm());

        stage.setTitle(Constants.APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
