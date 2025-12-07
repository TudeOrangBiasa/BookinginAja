package com.example.opp.controller;

import com.example.opp.model.Role;
import com.example.opp.model.User;
import com.example.opp.repository.UserRepository;
import com.example.opp.service.SessionManager;
import com.example.opp.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;

public class UsersController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> numberCol;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> fullNameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> statusCol;
    @FXML private TableColumn<User, Void> actionCol;
    @FXML private TextField searchField;

    private final UserRepository userRepository = new UserRepository();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredList;

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isAdmin()) {
            DialogUtil.peringatan("Akses ditolak. Halaman ini hanya untuk Admin.");
            return;
        }
        setupTable();
        loadUsers();
        setupSearch();
    }

    private void setupTable() {
        numberCol.setCellValueFactory(c -> new SimpleStringProperty(
            String.valueOf(usersTable.getItems().indexOf(c.getValue()) + 1)
        ));
        usernameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        fullNameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        roleCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().isAdmin() ? "Administrator" : "Resepsionis"
        ));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().isActive() ? "Aktif" : "Nonaktif"
        ));

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("status-badge", 
                        "Aktif".equals(item) ? "badge-green" : "badge-red");
                    setGraphic(badge);
                }
            }
        });

        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    setGraphic(createActionButtons(user));
                }
            }
        });
    }

    private HBox createActionButtons(User user) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-warning-sm");
        editBtn.setOnAction(e -> handleEditUser(user));

        // Tidak bisa nonaktifkan diri sendiri
        Long currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (!user.getId().equals(currentUserId)) {
            Button toggleBtn = new Button(user.isActive() ? "Nonaktifkan" : "Aktifkan");
            toggleBtn.getStyleClass().add(user.isActive() ? "btn-danger-sm" : "btn-success-sm");
            toggleBtn.setOnAction(e -> handleToggleStatus(user));
            box.getChildren().addAll(editBtn, toggleBtn);
        } else {
            box.getChildren().add(editBtn);
        }

        return box;
    }

    private void loadUsers() {
        try {
            List<User> users = userRepository.findAll();
            usersList.setAll(users);
            filteredList = new FilteredList<>(usersList, p -> true);
            usersTable.setItems(filteredList);
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data pengguna: " + e.getMessage());
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            if (filteredList == null) return;
            String search = val.toLowerCase();
            filteredList.setPredicate(user -> 
                search.isEmpty() ||
                user.getUsername().toLowerCase().contains(search) ||
                user.getFullName().toLowerCase().contains(search) ||
                user.getEmail().toLowerCase().contains(search)
            );
        });
    }

    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    private void handleEditUser(User user) {
        showUserDialog(user);
    }

    private void showUserDialog(User existingUser) {
        boolean isEdit = existingUser != null;
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Pengguna" : "Tambah Pengguna Baru");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField username = new TextField(isEdit ? existingUser.getUsername() : "");
        username.setPromptText("Username");
        username.setDisable(isEdit); // Username tidak bisa diubah

        PasswordField password = new PasswordField();
        password.setPromptText(isEdit ? "Kosongkan jika tidak diubah" : "Password");

        TextField fullName = new TextField(isEdit ? existingUser.getFullName() : "");
        fullName.setPromptText("Nama Lengkap");

        TextField email = new TextField(isEdit ? existingUser.getEmail() : "");
        email.setPromptText("Email");

        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Administrator", "Resepsionis"));
        role.setValue(isEdit && existingUser.isAdmin() ? "Administrator" : "Resepsionis");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Nama Lengkap:"), 0, 2);
        grid.add(fullName, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(email, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(role, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(400);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (!isEdit && username.getText().trim().isEmpty()) {
                    DialogUtil.peringatan("Username wajib diisi");
                    return null;
                }
                if (!isEdit && password.getText().isEmpty()) {
                    DialogUtil.peringatan("Password wajib diisi");
                    return null;
                }
                if (fullName.getText().trim().isEmpty() || email.getText().trim().isEmpty()) {
                    DialogUtil.peringatan("Nama lengkap dan email wajib diisi");
                    return null;
                }

                User user = isEdit ? existingUser : new User();
                if (!isEdit) user.setUsername(username.getText().trim());
                if (!password.getText().isEmpty()) user.setPassword(password.getText());
                user.setFullName(fullName.getText().trim());
                user.setEmail(email.getText().trim());
                user.setRole("Administrator".equals(role.getValue()) ? Role.ADMIN : Role.RECEPTIONIST);
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            String action = isEdit ? "perubahan pengguna" : "pengguna baru";
            if (DialogUtil.konfirmasiSimpan(action)) {
                try {
                    if (isEdit) {
                        userRepository.update(user);
                    } else {
                        userRepository.save(user);
                    }
                    loadUsers();
                    DialogUtil.sukses("Pengguna berhasil " + (isEdit ? "diperbarui" : "ditambahkan") + "!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal menyimpan pengguna: " + e.getMessage());
                }
            }
        });
    }

    private void handleToggleStatus(User user) {
        String action = user.isActive() ? "menonaktifkan" : "mengaktifkan";
        String confirm = "Apakah Anda yakin ingin " + action + " pengguna " + user.getUsername() + "?";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi");
        alert.setHeaderText(null);
        alert.setContentText(confirm);
        
        ButtonType yesBtn = new ButtonType("Ya", ButtonBar.ButtonData.YES);
        ButtonType noBtn = new ButtonType("Tidak", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == yesBtn) {
                try {
                    user.setActive(!user.isActive());
                    userRepository.update(user);
                    loadUsers();
                    DialogUtil.sukses("Status pengguna berhasil diubah!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal mengubah status: " + e.getMessage());
                }
            }
        });
    }
}
