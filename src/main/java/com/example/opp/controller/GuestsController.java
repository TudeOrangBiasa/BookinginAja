package com.example.opp.controller;

import com.example.opp.model.Guest;
import com.example.opp.service.GuestService;
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
import java.util.Optional;

public class GuestsController {

    @FXML private TableView<Guest> guestsTable;
    @FXML private TableColumn<Guest, String> idNumberCol;
    @FXML private TableColumn<Guest, String> idTypeCol;
    @FXML private TableColumn<Guest, String> nameCol;
    @FXML private TableColumn<Guest, String> phoneCol;
    @FXML private TableColumn<Guest, String> emailCol;
    @FXML private TableColumn<Guest, Void> actionCol;
    @FXML private TextField searchField;

    private final GuestService guestService = new GuestService();
    private ObservableList<Guest> guestsList = FXCollections.observableArrayList();
    private FilteredList<Guest> filteredList;

    private boolean isAdmin() {
        return SessionManager.getInstance().isAdmin();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadGuests();
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void setupTable() {
        idNumberCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getIdNumber()));
        idTypeCol.setCellValueFactory(c -> new SimpleStringProperty(formatIdType(c.getValue().getIdType())));
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        phoneCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone() != null ? c.getValue().getPhone() : "-"));
        emailCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : "-"));

        actionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Guest guest = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(6);
                    box.setAlignment(Pos.CENTER);

                    Button editBtn = new Button("Edit");
                    editBtn.getStyleClass().add("btn-secondary-sm");
                    editBtn.setOnAction(e -> handleEditGuest(guest));
                    box.getChildren().add(editBtn);

                    // Tombol hapus hanya untuk Admin
                    if (isAdmin()) {
                        Button deleteBtn = new Button("Hapus");
                        deleteBtn.getStyleClass().add("btn-danger-sm");
                        deleteBtn.setOnAction(e -> handleDeleteGuest(guest));
                        box.getChildren().add(deleteBtn);
                    }

                    setGraphic(box);
                }
            }
        });
    }

    private String formatIdType(Guest.IdType type) {
        return switch (type) {
            case KTP -> "KTP";
            case PASSPORT -> "Paspor";
            case SIM -> "SIM";
        };
    }

    private void loadGuests() {
        try {
            List<Guest> guests = guestService.getAllGuests();
            guestsList.setAll(guests);
            filteredList = new FilteredList<>(guestsList, p -> true);
            guestsTable.setItems(filteredList);
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data tamu: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String search = searchField.getText().toLowerCase();
        filteredList.setPredicate(guest ->
            search.isEmpty() ||
            guest.getFullName().toLowerCase().contains(search) ||
            guest.getIdNumber().toLowerCase().contains(search)
        );
    }

    @FXML
    private void handleAddGuest() {
        showGuestDialog(null).ifPresent(guest -> {
            if (DialogUtil.konfirmasiSimpan("data tamu baru")) {
                try {
                    guestService.createGuest(guest);
                    loadGuests();
                    DialogUtil.sukses("Tamu " + guest.getFullName() + " berhasil ditambahkan!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal menambah tamu: " + e.getMessage());
                } catch (IllegalStateException e) {
                    DialogUtil.error(e.getMessage());
                }
            }
        });
    }

    private void handleEditGuest(Guest guest) {
        showGuestDialog(guest).ifPresent(updated -> {
            if (DialogUtil.konfirmasiSimpan("perubahan data tamu")) {
                try {
                    updated.setId(guest.getId());
                    guestService.updateGuest(updated);
                    loadGuests();
                    DialogUtil.sukses("Data tamu berhasil diperbarui!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal memperbarui data tamu: " + e.getMessage());
                }
            }
        });
    }

    private void handleDeleteGuest(Guest guest) {
        if (DialogUtil.konfirmasiHapus("tamu " + guest.getFullName())) {
            try {
                guestService.deleteGuest(guest.getId());
                loadGuests();
                DialogUtil.sukses("Tamu " + guest.getFullName() + " berhasil dihapus!");
            } catch (SQLException e) {
                DialogUtil.error("Gagal menghapus tamu: " + e.getMessage());
            }
        }
    }

    private Optional<Guest> showGuestDialog(Guest existing) {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Tambah Tamu Baru" : "Edit Data Tamu");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField idNumber = new TextField();
        idNumber.setPromptText("Nomor Identitas");

        ComboBox<Guest.IdType> idType = new ComboBox<>(FXCollections.observableArrayList(Guest.IdType.values()));
        idType.setValue(Guest.IdType.KTP);
        idType.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Guest.IdType t) {
                return t != null ? formatIdType(t) : "";
            }
            @Override
            public Guest.IdType fromString(String s) { return null; }
        });

        TextField fullName = new TextField();
        fullName.setPromptText("Nama Lengkap");

        TextField phone = new TextField();
        phone.setPromptText("Nomor Telepon");

        TextField email = new TextField();
        email.setPromptText("Alamat Email");

        TextArea address = new TextArea();
        address.setPromptText("Alamat");
        address.setPrefRowCount(2);

        if (existing != null) {
            idNumber.setText(existing.getIdNumber());
            idType.setValue(existing.getIdType());
            fullName.setText(existing.getFullName());
            phone.setText(existing.getPhone());
            email.setText(existing.getEmail());
            address.setText(existing.getAddress());
        }

        grid.add(new Label("Nomor Identitas:"), 0, 0);
        grid.add(idNumber, 1, 0);
        grid.add(new Label("Jenis Identitas:"), 0, 1);
        grid.add(idType, 1, 1);
        grid.add(new Label("Nama Lengkap:"), 0, 2);
        grid.add(fullName, 1, 2);
        grid.add(new Label("Telepon:"), 0, 3);
        grid.add(phone, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(email, 1, 4);
        grid.add(new Label("Alamat:"), 0, 5);
        grid.add(address, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(400);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (idNumber.getText().isEmpty() || fullName.getText().isEmpty()) {
                    DialogUtil.peringatan("Nomor identitas dan nama lengkap wajib diisi");
                    return null;
                }
                Guest guest = new Guest();
                guest.setIdNumber(idNumber.getText().trim());
                guest.setIdType(idType.getValue());
                guest.setFullName(fullName.getText().trim());
                guest.setPhone(phone.getText().trim());
                guest.setEmail(email.getText().trim());
                guest.setAddress(address.getText().trim());
                return guest;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
