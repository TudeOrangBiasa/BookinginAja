package com.example.opp.controller;

import com.example.opp.model.Room;
import com.example.opp.model.RoomStatus;
import com.example.opp.model.RoomType;
import com.example.opp.service.RoomService;
import com.example.opp.service.SessionManager;
import com.example.opp.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class RoomsController {

    @FXML private FlowPane roomGrid;
    @FXML private ComboBox<String> filterCombo;
    @FXML private ComboBox<String> floorCombo;
    @FXML private Button addRoomBtn;

    private final RoomService roomService = new RoomService();
    private List<Room> allRooms;

    @FXML
    public void initialize() {
        setupFilters();
        setupRoleBasedAccess();
        loadRooms();
    }

    private void setupRoleBasedAccess() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        // Tombol tambah kamar hanya untuk Admin
        if (addRoomBtn != null) {
            addRoomBtn.setVisible(isAdmin);
            addRoomBtn.setManaged(isAdmin);
        }
    }

    private boolean isAdmin() {
        return SessionManager.getInstance().isAdmin();
    }

    private void setupFilters() {
        filterCombo.setItems(FXCollections.observableArrayList(
            "Semua Status", "Tersedia", "Terisi", "Dipesan", "Perbaikan"
        ));
        filterCombo.setValue("Semua Status");
        filterCombo.setOnAction(e -> applyFilters());

        floorCombo.setItems(FXCollections.observableArrayList("Semua Lantai", "Lantai 1", "Lantai 2", "Lantai 3"));
        floorCombo.setValue("Semua Lantai");
        floorCombo.setOnAction(e -> applyFilters());
    }

    private void loadRooms() {
        try {
            allRooms = roomService.getAllRooms();
            applyFilters();
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data kamar: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allRooms == null) return;

        String statusFilter = filterCombo.getValue();
        String floorFilter = floorCombo.getValue();

        List<Room> filtered = allRooms.stream()
            .filter(r -> "Semua Status".equals(statusFilter) || formatStatus(r.getStatus()).equals(statusFilter))
            .filter(r -> "Semua Lantai".equals(floorFilter) || ("Lantai " + r.getFloor()).equals(floorFilter))
            .collect(Collectors.toList());

        renderRooms(filtered);
    }

    private void renderRooms(List<Room> rooms) {
        roomGrid.getChildren().clear();
        for (Room room : rooms) {
            roomGrid.getChildren().add(createRoomCard(room));
        }
        if (rooms.isEmpty()) {
            Label empty = new Label("Tidak ada kamar ditemukan");
            empty.getStyleClass().add("empty-state");
            roomGrid.getChildren().add(empty);
        }
    }

    private VBox createRoomCard(Room room) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("room-card", getStatusClass(room.getStatus()));
        card.setPadding(new Insets(0));
        card.setPrefWidth(200);
        card.setAlignment(Pos.TOP_LEFT);

        // Room image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(false);
        imageView.setStyle("-fx-background-radius: 12 12 0 0;");
        
        String imageUrl = room.getRoomType() != null ? room.getRoomType().getImageUrl() : null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image img = new Image(imageUrl, 200, 120, false, true, true);
                imageView.setImage(img);
            } catch (Exception e) {
                // Use placeholder if image fails
                imageView.setStyle("-fx-background-color: #E0E0E0;");
            }
        } else {
            imageView.setStyle("-fx-background-color: #E0E0E0;");
        }

        // Content area
        VBox content = new VBox(6);
        content.setPadding(new Insets(12));

        Label roomNum = new Label(room.getRoomNumber());
        roomNum.getStyleClass().add("room-number");

        String typeName = room.getRoomType() != null ? room.getRoomType().getName() : "Standard";
        Label typeLabel = new Label(typeName);
        typeLabel.getStyleClass().add("room-type");

        Label floorLabel = new Label("Lantai " + room.getFloor());
        floorLabel.getStyleClass().add("room-floor");

        HBox statusBox = new HBox(6);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.getStyleClass().addAll("status-dot", "dot-" + room.getStatus().name().toLowerCase());
        dot.setPrefSize(8, 8);
        Label statusLabel = new Label(formatStatus(room.getStatus()));
        statusLabel.getStyleClass().add("room-status-text");
        statusBox.getChildren().addAll(dot, statusLabel);

        String price = room.getRoomType() != null ? room.getRoomType().getFormattedPrice() : "-";
        Label priceLabel = new Label(price + "/malam");
        priceLabel.getStyleClass().add("room-price");

        content.getChildren().addAll(roomNum, typeLabel, floorLabel, statusBox, priceLabel);
        card.getChildren().addAll(imageView, content);
        card.setOnMouseClicked(e -> showRoomDetails(room));

        return card;
    }

    private String getStatusClass(RoomStatus status) {
        return switch (status) {
            case AVAILABLE -> "room-available";
            case OCCUPIED -> "room-occupied";
            case RESERVED -> "room-reserved";
            case MAINTENANCE -> "room-maintenance";
        };
    }

    private String formatStatus(RoomStatus status) {
        return switch (status) {
            case AVAILABLE -> "Tersedia";
            case OCCUPIED -> "Terisi";
            case RESERVED -> "Dipesan";
            case MAINTENANCE -> "Perbaikan";
        };
    }

    private void showRoomDetails(Room room) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Detail Kamar " + room.getRoomNumber());
        dialog.setHeaderText(null);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(320);

        content.getChildren().addAll(
            createDetailRow("Nomor Kamar", room.getRoomNumber()),
            createDetailRow("Tipe", room.getRoomType() != null ? room.getRoomType().getName() : "-"),
            createDetailRow("Lantai", String.valueOf(room.getFloor())),
            createDetailRow("Status", formatStatus(room.getStatus())),
            createDetailRow("Harga", room.getRoomType() != null ? room.getRoomType().getFormattedPrice() + "/malam" : "-")
        );

        if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
            content.getChildren().add(createDetailRow("Fasilitas", room.getAmenities()));
        }

        dialog.getDialogPane().setContent(content);

        ButtonType tutupBtn = new ButtonType("Tutup", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(tutupBtn);

        // Tombol Ubah Status hanya untuk Admin dan kamar tidak sedang terisi
        if (isAdmin() && room.getStatus() != RoomStatus.OCCUPIED) {
            ButtonType ubahStatus = new ButtonType("Ubah Status", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().add(ubahStatus);

            dialog.setResultConverter(btn -> {
                if (btn == ubahStatus) {
                    showStatusChangeDialog(room);
                }
                return btn;
            });
        }

        dialog.showAndWait();
    }

    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(8);
        Label l = new Label(label + ":");
        l.setStyle("-fx-font-weight: 600; -fx-min-width: 100;");
        Label v = new Label(value);
        v.setWrapText(true);
        row.getChildren().addAll(l, v);
        return row;
    }

    private void showStatusChangeDialog(Room room) {
        // Hanya Admin yang bisa ubah status kamar
        if (!isAdmin()) {
            DialogUtil.peringatan("Akses ditolak. Hanya Admin yang dapat mengubah status kamar.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            formatStatus(room.getStatus()),
            "Tersedia", "Dipesan", "Perbaikan"
        );
        dialog.setTitle("Ubah Status Kamar");
        dialog.setHeaderText("Kamar " + room.getRoomNumber());
        dialog.setContentText("Pilih status baru:");

        dialog.showAndWait().ifPresent(selected -> {
            RoomStatus newStatus = switch (selected) {
                case "Tersedia" -> RoomStatus.AVAILABLE;
                case "Dipesan" -> RoomStatus.RESERVED;
                case "Perbaikan" -> RoomStatus.MAINTENANCE;
                default -> room.getStatus();
            };

            if (DialogUtil.konfirmasiUbahStatus(room.getRoomNumber(), selected)) {
                try {
                    if (roomService.updateRoomStatus(room.getId(), newStatus)) {
                        loadRooms();
                        DialogUtil.sukses("Status kamar " + room.getRoomNumber() + " berhasil diubah menjadi " + selected);
                    }
                } catch (SQLException e) {
                    DialogUtil.error("Gagal mengubah status: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleAddRoom() {
        // Hanya Admin yang bisa tambah kamar
        if (!isAdmin()) {
            DialogUtil.peringatan("Akses ditolak. Hanya Admin yang dapat menambah kamar baru.");
            return;
        }

        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Tambah Kamar Baru");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField roomNumber = new TextField();
        roomNumber.setPromptText("Contoh: 101, 202, D01");

        ComboBox<RoomType> roomType = new ComboBox<>();
        try {
            roomType.setItems(FXCollections.observableArrayList(roomService.getAllRoomTypes()));
            roomType.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(RoomType rt) {
                    return rt != null ? rt.getName() + " - " + rt.getFormattedPrice() : "";
                }
                @Override
                public RoomType fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat tipe kamar");
        }

        Spinner<Integer> floor = new Spinner<>(1, 10, 1);
        floor.setEditable(true);

        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("Tersedia", "Perbaikan"));
        status.setValue("Tersedia");

        TextField amenities = new TextField();
        amenities.setPromptText("AC, TV, WiFi, Mini Bar");

        TextField imageUrl = new TextField();
        imageUrl.setPromptText("https://example.com/image.jpg");

        grid.add(new Label("Nomor Kamar:"), 0, 0);
        grid.add(roomNumber, 1, 0);
        grid.add(new Label("Tipe Kamar:"), 0, 1);
        grid.add(roomType, 1, 1);
        grid.add(new Label("Lantai:"), 0, 2);
        grid.add(floor, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(status, 1, 3);
        grid.add(new Label("Fasilitas:"), 0, 4);
        grid.add(amenities, 1, 4);
        grid.add(new Label("URL Foto:"), 0, 5);
        grid.add(imageUrl, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (roomNumber.getText().isEmpty() || roomType.getValue() == null) {
                    DialogUtil.peringatan("Nomor kamar dan tipe kamar wajib diisi");
                    return null;
                }
                Room room = new Room();
                room.setRoomNumber(roomNumber.getText().trim());
                room.setRoomTypeId(roomType.getValue().getId());
                room.setFloor(floor.getValue());
                room.setStatus("Tersedia".equals(status.getValue()) ? RoomStatus.AVAILABLE : RoomStatus.MAINTENANCE);
                room.setAmenities(amenities.getText().trim());
                return room;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(room -> {
            if (DialogUtil.konfirmasiSimpan("kamar baru " + room.getRoomNumber())) {
                try {
                    roomService.createRoom(room);
                    loadRooms();
                    DialogUtil.sukses("Kamar " + room.getRoomNumber() + " berhasil ditambahkan!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal menambah kamar: " + e.getMessage());
                }
            }
        });
    }
}
