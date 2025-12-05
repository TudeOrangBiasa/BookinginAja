package com.example.opp.controller;

import com.example.opp.util.Constants;
import com.example.opp.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to the Dashboard!");
    }

    @FXML
    private void handleLogout() {
        try {
            ViewManager.switchScene(Constants.LOGIN_VIEW, Constants.APP_TITLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
