package com.example.opp.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * View Manager - mengelola perpindahan scene/view
 */
public class ViewManager {
    
    private static Stage primaryStage;
    
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void switchScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static <T> T loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }
}
