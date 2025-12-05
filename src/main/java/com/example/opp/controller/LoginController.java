package com.example.opp.controller;

import com.example.opp.model.User;
import com.example.opp.service.AuthService;
import com.example.opp.service.SessionManager;
import com.example.opp.util.Constants;
import com.example.opp.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox loginForm;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                Thread.sleep(500);
                boolean success = authService.login(username, password);
                
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    if (success) {
                        User user = authService.getCurrentUser();
                        SessionManager.getInstance().setCurrentUser(user);
                        navigateToMain();
                    } else {
                        showError("Invalid username or password");
                        passwordField.clear();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText(null);
        alert.setContentText("Please contact your system administrator to reset your password.");
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
    }

    private void navigateToMain() {
        try {
            ViewManager.switchScene(Constants.MAIN_VIEW, Constants.APP_TITLE);
        } catch (Exception e) {
            showError("Failed to load application");
        }
    }
}
