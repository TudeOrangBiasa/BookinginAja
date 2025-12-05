package com.example.opp.controller;

import com.example.opp.model.Booking;
import com.example.opp.model.BookingStatus;
import com.example.opp.service.BookingService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingsController {

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> codeCol;
    @FXML private TableColumn<Booking, String> guestCol;
    @FXML private TableColumn<Booking, String> roomCol;
    @FXML private TableColumn<Booking, String> checkInCol;
    @FXML private TableColumn<Booking, String> checkOutCol;
    @FXML private TableColumn<Booking, String> nightsCol;
    @FXML private TableColumn<Booking, String> totalCol;
    @FXML private TableColumn<Booking, String> statusCol;
    @FXML private TableColumn<Booking, Void> actionCol;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private final BookingService bookingService = new BookingService();
    private ObservableList<Booking> bookingsList = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredList;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        setupFilters();
        setupTable();
        loadBookings();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
            "Semua Status", "Menunggu", "Dikonfirmasi", "Checked In", "Checked Out", "Dibatalkan"
        ));
        statusFilter.setValue("Semua Status");
        statusFilter.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
    }

    private void setupTable() {
        codeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBookingCode()));
        guestCol.setCellValueFactory(c -> {
            var guest = c.getValue().getGuest();
            return new SimpleStringProperty(guest != null ? guest.getFullName() : "-");
        });
        roomCol.setCellValueFactory(c -> {
            var room = c.getValue().getRoom();
            return new SimpleStringProperty(room != null ? room.getRoomNumber() : "-");
        });
        checkInCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckInDate().format(DATE_FMT)));
        checkOutCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCheckOutDate().format(DATE_FMT)));
        nightsCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTotalNights())));
        totalCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormattedTotal()));
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(formatStatus(c.getValue().getStatus())));
        
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("status-badge", getStatusBadgeClass(item));
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
                    setGraphic(createActionButtons(getTableView().getItems().get(getIndex())));
                }
            }
        });
    }

    private HBox createActionButtons(Booking booking) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);
        BookingStatus status = booking.getStatus();

        if (status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING) {
            Button checkIn = new Button("Check In");
            checkIn.getStyleClass().add("btn-success-sm");
            checkIn.setOnAction(e -> handleCheckIn(booking));

            Button cancel = new Button("Batal");
            cancel.getStyleClass().add("btn-danger-sm");
            cancel.setOnAction(e -> handleCancel(booking));

            box.getChildren().addAll(checkIn, cancel);
        } else if (status == BookingStatus.CHECKED_IN) {
            Button checkOut = new Button("Check Out");
            checkOut.getStyleClass().add("btn-warning-sm");
            checkOut.setOnAction(e -> handleCheckOut(booking));
            box.getChildren().add(checkOut);
        }
        return box;
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = bookingService.getActiveBookings();
            bookingsList.setAll(bookings);
            filteredList = new FilteredList<>(bookingsList, p -> true);
            bookingsTable.setItems(filteredList);
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data booking: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (filteredList == null) return;
        String search = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();

        filteredList.setPredicate(booking -> {
            boolean matchSearch = search.isEmpty() ||
                booking.getBookingCode().toLowerCase().contains(search) ||
                (booking.getGuest() != null && booking.getGuest().getFullName().toLowerCase().contains(search));
            boolean matchStatus = "Semua Status".equals(status) ||
                formatStatus(booking.getStatus()).equalsIgnoreCase(status);
            return matchSearch && matchStatus;
        });
    }

    private void handleCheckIn(Booking booking) {
        String kamar = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";
        if (DialogUtil.konfirmasiCheckIn(booking.getBookingCode(), kamar)) {
            try {
                if (bookingService.checkIn(booking.getId())) {
                    loadBookings();
                    DialogUtil.sukses("Check-in berhasil untuk booking " + booking.getBookingCode());
                }
            } catch (SQLException e) {
                DialogUtil.error("Check-in gagal: " + e.getMessage());
            }
        }
    }

    private void handleCheckOut(Booking booking) {
        String kamar = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";
        if (DialogUtil.konfirmasiCheckOut(booking.getBookingCode(), kamar)) {
            try {
                if (bookingService.checkOut(booking.getId())) {
                    loadBookings();
                    DialogUtil.sukses("Check-out berhasil untuk booking " + booking.getBookingCode());
                }
            } catch (SQLException e) {
                DialogUtil.error("Check-out gagal: " + e.getMessage());
            }
        }
    }

    private void handleCancel(Booking booking) {
        if (DialogUtil.konfirmasiBatalBooking(booking.getBookingCode())) {
            try {
                if (bookingService.cancelBooking(booking.getId())) {
                    loadBookings();
                    DialogUtil.sukses("Booking " + booking.getBookingCode() + " berhasil dibatalkan");
                }
            } catch (SQLException e) {
                DialogUtil.error("Pembatalan gagal: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNewBooking() {
        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("Booking Baru");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Simpan Booking", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // Guest selection
        ComboBox<com.example.opp.model.Guest> guestCombo = new ComboBox<>();
        com.example.opp.service.GuestService guestService = new com.example.opp.service.GuestService();
        try {
            guestCombo.setItems(FXCollections.observableArrayList(guestService.getAllGuests()));
            guestCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(com.example.opp.model.Guest g) {
                    return g != null ? g.getFullName() + " (" + g.getIdNumber() + ")" : "";
                }
                @Override
                public com.example.opp.model.Guest fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data tamu");
        }

        // Room selection
        ComboBox<com.example.opp.model.Room> roomCombo = new ComboBox<>();
        com.example.opp.service.RoomService roomService = new com.example.opp.service.RoomService();
        try {
            roomCombo.setItems(FXCollections.observableArrayList(roomService.getAvailableRooms()));
            roomCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(com.example.opp.model.Room r) {
                    if (r == null) return "";
                    String type = r.getRoomType() != null ? r.getRoomType().getName() : "Standard";
                    String price = r.getRoomType() != null ? r.getRoomType().getFormattedPrice() : "-";
                    return r.getRoomNumber() + " - " + type + " (" + price + "/malam)";
                }
                @Override
                public com.example.opp.model.Room fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data kamar");
        }

        DatePicker checkIn = new DatePicker(java.time.LocalDate.now());
        DatePicker checkOut = new DatePicker(java.time.LocalDate.now().plusDays(1));

        Label totalLabel = new Label("Rp 0");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0194F3;");

        Runnable calculateTotal = () -> {
            com.example.opp.model.Room room = roomCombo.getValue();
            java.time.LocalDate in = checkIn.getValue();
            java.time.LocalDate out = checkOut.getValue();
            if (room != null && room.getRoomType() != null && in != null && out != null && out.isAfter(in)) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(in, out);
                java.math.BigDecimal total = room.getRoomType().getBasePrice().multiply(java.math.BigDecimal.valueOf(nights));
                totalLabel.setText(String.format("Rp %,.0f (%d malam)", total, nights));
            }
        };

        roomCombo.setOnAction(e -> calculateTotal.run());
        checkIn.setOnAction(e -> calculateTotal.run());
        checkOut.setOnAction(e -> calculateTotal.run());

        TextArea notes = new TextArea();
        notes.setPromptText("Catatan atau permintaan khusus...");
        notes.setPrefRowCount(2);

        grid.add(new Label("Tamu:"), 0, 0);
        grid.add(guestCombo, 1, 0);
        grid.add(new Label("Kamar:"), 0, 1);
        grid.add(roomCombo, 1, 1);
        grid.add(new Label("Tanggal Check-in:"), 0, 2);
        grid.add(checkIn, 1, 2);
        grid.add(new Label("Tanggal Check-out:"), 0, 3);
        grid.add(checkOut, 1, 3);
        grid.add(new Label("Total Biaya:"), 0, 4);
        grid.add(totalLabel, 1, 4);
        grid.add(new Label("Catatan:"), 0, 5);
        grid.add(notes, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(450);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (guestCombo.getValue() == null || roomCombo.getValue() == null) {
                    DialogUtil.peringatan("Silakan pilih tamu dan kamar");
                    return null;
                }
                if (checkOut.getValue() == null || !checkOut.getValue().isAfter(checkIn.getValue())) {
                    DialogUtil.peringatan("Tanggal check-out harus setelah tanggal check-in");
                    return null;
                }
                Booking b = new Booking();
                b.setGuestId(guestCombo.getValue().getId());
                b.setRoomId(roomCombo.getValue().getId());
                b.setCheckInDate(checkIn.getValue());
                b.setCheckOutDate(checkOut.getValue());
                b.setNotes(notes.getText());
                return b;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            if (DialogUtil.konfirmasiSimpan("booking baru")) {
                try {
                    Long userId = com.example.opp.service.SessionManager.getInstance().getCurrentUserId();
                    if (userId == null) userId = 1L;
                    bookingService.createBooking(booking.getGuestId(), booking.getRoomId(),
                        booking.getCheckInDate(), booking.getCheckOutDate(), userId);
                    loadBookings();
                    DialogUtil.sukses("Booking berhasil dibuat!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal membuat booking: " + e.getMessage());
                } catch (IllegalStateException e) {
                    DialogUtil.error(e.getMessage());
                }
            }
        });
    }

    private String formatStatus(BookingStatus status) {
        return switch (status) {
            case PENDING -> "Menunggu";
            case CONFIRMED -> "Dikonfirmasi";
            case CHECKED_IN -> "Checked In";
            case CHECKED_OUT -> "Checked Out";
            case CANCELLED -> "Dibatalkan";
        };
    }

    private String getStatusBadgeClass(String status) {
        return switch (status.toLowerCase()) {
            case "menunggu" -> "badge-gray";
            case "dikonfirmasi" -> "badge-blue";
            case "checked in" -> "badge-green";
            case "checked out" -> "badge-purple";
            case "dibatalkan" -> "badge-red";
            default -> "badge-gray";
        };
    }
}
