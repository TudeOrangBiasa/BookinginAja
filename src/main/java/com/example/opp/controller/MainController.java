package com.example.opp.controller;

import com.example.opp.model.Role;
import com.example.opp.model.User;
import com.example.opp.service.SessionManager;
import com.example.opp.util.Constants;
import com.example.opp.view.ViewManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;
    @FXML private Label userEmailLabel;
    @FXML private Label userRoleLabel;

    @FXML private HBox navDashboard;
    @FXML private HBox navRooms;
    @FXML private HBox navBookings;
    @FXML private HBox navGuests;
    @FXML private HBox navReports;
    @FXML private HBox navUsers;

    private HBox activeNav;

    @FXML
    public void initialize() {
        loadUserInfo();
        setupRoleBasedMenu();
        activeNav = navDashboard;
        showDashboard();
    }

    private void loadUserInfo() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getFullName());
            userEmailLabel.setText(user.getEmail());
            String roleDisplay = user.isAdmin() ? "Administrator" : "Resepsionis";
            userRoleLabel.setText(roleDisplay);
        }
    }

    @FXML private Label adminSeparator;

    private void setupRoleBasedMenu() {
        User user = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = user != null && user.isAdmin();

        // Separator Admin
        if (adminSeparator != null) {
            adminSeparator.setVisible(isAdmin);
            adminSeparator.setManaged(isAdmin);
        }

        // Menu Laporan - hanya Admin
        if (navReports != null) {
            navReports.setVisible(isAdmin);
            navReports.setManaged(isAdmin);
        }

        // Menu Kelola User - hanya Admin
        if (navUsers != null) {
            navUsers.setVisible(isAdmin);
            navUsers.setManaged(isAdmin);
        }
    }

    public static boolean isCurrentUserAdmin() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && user.isAdmin();
    }

    @FXML
    public void showDashboard() {
        setActiveNav(navDashboard);
        loadContent("/com/example/opp/fxml/dashboard-content.fxml");
    }

    @FXML
    public void showRooms() {
        setActiveNav(navRooms);
        loadContent("/com/example/opp/fxml/rooms-view.fxml");
    }

    @FXML
    public void showBookings() {
        setActiveNav(navBookings);
        loadContent("/com/example/opp/fxml/bookings-view.fxml");
    }

    @FXML
    public void showGuests() {
        setActiveNav(navGuests);
        loadContent("/com/example/opp/fxml/guests-view.fxml");
    }

    @FXML
    public void showReports() {
        if (!isCurrentUserAdmin()) {
            com.example.opp.util.DialogUtil.peringatan("Akses ditolak. Hanya Admin yang dapat mengakses menu ini.");
            return;
        }
        setActiveNav(navReports);
        loadContent("/com/example/opp/fxml/reports-view.fxml");
    }

    @FXML
    public void showUsers() {
        if (!isCurrentUserAdmin()) {
            com.example.opp.util.DialogUtil.peringatan("Akses ditolak. Hanya Admin yang dapat mengakses menu ini.");
            return;
        }
        setActiveNav(navUsers);
        loadContent("/com/example/opp/fxml/users-view.fxml");
    }

    @FXML
    private void handleLogout() {
        if (com.example.opp.util.DialogUtil.konfirmasiLogout()) {
            SessionManager.getInstance().logout();
            try {
                ViewManager.switchScene(Constants.LOGIN_VIEW, Constants.APP_TITLE);
            } catch (IOException e) {
                com.example.opp.util.DialogUtil.error("Gagal keluar: " + e.getMessage());
            }
        }
    }

    private void setActiveNav(HBox nav) {
        if (activeNav != null) {
            activeNav.getStyleClass().remove("nav-item-active");
        }
        nav.getStyleClass().add("nav-item-active");
        activeNav = nav;
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentArea.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
