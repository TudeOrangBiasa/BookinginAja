package com.example.opp.controller;

import com.example.opp.model.Booking;
import com.example.opp.model.Guest;
import com.example.opp.model.Room;
import com.example.opp.service.BookingService;
import com.example.opp.service.GuestService;
import com.example.opp.service.RoomService;
import com.example.opp.service.SessionManager;
import com.example.opp.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class DashboardContentController {

    @FXML private Label dateLabel;
    @FXML private Label availableCount;
    @FXML private Label occupiedCount;
    @FXML private Label reservedCount;
    @FXML private Label maintenanceCount;
    @FXML private Label checkInCountLabel;
    @FXML private Label checkOutCountLabel;
    @FXML private VBox checkInList;
    @FXML private VBox checkOutList;

    private final RoomService roomService = new RoomService();
    private final BookingService bookingService = new BookingService();

    @FXML
    public void initialize() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        dateLabel.setText(LocalDate.now().format(fmt));
        loadStats();
        loadTodayActivity();
    }

    private void loadStats() {
        try {
            availableCount.setText(String.valueOf(roomService.getAvailableCount()));
            occupiedCount.setText(String.valueOf(roomService.getOccupiedCount()));
            reservedCount.setText(String.valueOf(roomService.getReservedCount()));
            maintenanceCount.setText(String.valueOf(roomService.getMaintenanceCount()));
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat statistik: " + e.getMessage());
        }
    }

    private void loadTodayActivity() {
        try {
            List<Booking> checkIns = bookingService.getTodayCheckIns();
            List<Booking> checkOuts = bookingService.getTodayCheckOuts();

            checkInCountLabel.setText(String.valueOf(checkIns.size()));
            checkOutCountLabel.setText(String.valueOf(checkOuts.size()));

            checkInList.getChildren().clear();
            checkOutList.getChildren().clear();

            if (checkIns.isEmpty()) {
                checkInList.getChildren().add(createEmptyState("Tidak ada check-in hari ini"));
            } else {
                for (Booking b : checkIns) {
                    checkInList.getChildren().add(createBookingCard(b, true));
                }
            }

            if (checkOuts.isEmpty()) {
                checkOutList.getChildren().add(createEmptyState("Tidak ada check-out hari ini"));
            } else {
                for (Booking b : checkOuts) {
                    checkOutList.getChildren().add(createBookingCard(b, false));
                }
            }
        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat aktivitas: " + e.getMessage());
        }
    }

    private HBox createBookingCard(Booking booking, boolean isCheckIn) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("activity-card");
        card.setPadding(new Insets(12));

        VBox info = new VBox(4);
        String guestName = booking.getGuest() != null ? booking.getGuest().getFullName() : "Tamu";
        String roomNum = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";

        Label nameLabel = new Label(guestName);
        nameLabel.getStyleClass().add("activity-name");

        Label detailLabel = new Label("Kamar " + roomNum + " • " + booking.getBookingCode());
        detailLabel.getStyleClass().add("activity-detail");

        info.getChildren().addAll(nameLabel, detailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionBtn = new Button(isCheckIn ? "Check In" : "Check Out");
        actionBtn.getStyleClass().add(isCheckIn ? "btn-success-sm" : "btn-warning-sm");
        actionBtn.setOnAction(e -> handleAction(booking, isCheckIn));

        card.getChildren().addAll(info, spacer, actionBtn);
        return card;
    }

    private Label createEmptyState(String message) {
        Label label = new Label(message);
        label.getStyleClass().add("empty-state");
        return label;
    }

    private void handleAction(Booking booking, boolean isCheckIn) {
        String kamar = booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-";
        boolean confirmed = isCheckIn 
            ? DialogUtil.konfirmasiCheckIn(booking.getBookingCode(), kamar)
            : DialogUtil.konfirmasiCheckOut(booking.getBookingCode(), kamar);

        if (confirmed) {
            try {
                boolean success = isCheckIn 
                    ? bookingService.checkIn(booking.getId())
                    : bookingService.checkOut(booking.getId());
                if (success) {
                    loadStats();
                    loadTodayActivity();
                    String msg = isCheckIn ? "Check-in berhasil!" : "Check-out berhasil!";
                    DialogUtil.sukses(msg);
                }
            } catch (SQLException e) {
                DialogUtil.error("Operasi gagal: " + e.getMessage());
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
        ComboBox<Guest> guestCombo = new ComboBox<>();
        GuestService guestService = new GuestService();
        try {
            guestCombo.setItems(FXCollections.observableArrayList(guestService.getAllGuests()));
            guestCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Guest g) {
                    return g != null ? g.getFullName() + " (" + g.getIdNumber() + ")" : "";
                }
                @Override
                public Guest fromString(String s) { return null; }
            });
        } catch (SQLException ignored) {}

        // Room selection
        ComboBox<Room> roomCombo = new ComboBox<>();
        try {
            roomCombo.setItems(FXCollections.observableArrayList(roomService.getAvailableRooms()));
            roomCombo.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Room r) {
                    if (r == null) return "";
                    String type = r.getRoomType() != null ? r.getRoomType().getName() : "Standard";
                    String price = r.getRoomType() != null ? r.getRoomType().getFormattedPrice() : "-";
                    return r.getRoomNumber() + " - " + type + " (" + price + "/malam)";
                }
                @Override
                public Room fromString(String s) { return null; }
            });
        } catch (SQLException ignored) {}

        DatePicker checkIn = new DatePicker(LocalDate.now());
        DatePicker checkOut = new DatePicker(LocalDate.now().plusDays(1));

        Label totalLabel = new Label("Rp 0");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0194F3;");

        Runnable calcTotal = () -> {
            Room room = roomCombo.getValue();
            LocalDate in = checkIn.getValue();
            LocalDate out = checkOut.getValue();
            if (room != null && room.getRoomType() != null && in != null && out != null && out.isAfter(in)) {
                long nights = ChronoUnit.DAYS.between(in, out);
                BigDecimal total = room.getRoomType().getBasePrice().multiply(BigDecimal.valueOf(nights));
                totalLabel.setText(String.format("Rp %,.0f (%d malam)", total, nights));
            }
        };
        roomCombo.setOnAction(e -> calcTotal.run());
        checkIn.setOnAction(e -> calcTotal.run());
        checkOut.setOnAction(e -> calcTotal.run());

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

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(400);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                if (guestCombo.getValue() == null || roomCombo.getValue() == null) {
                    DialogUtil.peringatan("Silakan pilih tamu dan kamar");
                    return null;
                }
                if (checkOut.getValue() == null || !checkOut.getValue().isAfter(checkIn.getValue())) {
                    DialogUtil.peringatan("Tanggal check-out harus setelah check-in");
                    return null;
                }
                Booking b = new Booking();
                b.setGuestId(guestCombo.getValue().getId());
                b.setRoomId(roomCombo.getValue().getId());
                b.setCheckInDate(checkIn.getValue());
                b.setCheckOutDate(checkOut.getValue());
                return b;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            if (DialogUtil.konfirmasiSimpan("booking baru")) {
                try {
                    Long userId = SessionManager.getInstance().getCurrentUserId();
                    if (userId == null) userId = 1L;
                    bookingService.createBooking(booking.getGuestId(), booking.getRoomId(),
                        booking.getCheckInDate(), booking.getCheckOutDate(), userId);
                    loadStats();
                    loadTodayActivity();
                    DialogUtil.sukses("Booking berhasil dibuat!");
                } catch (SQLException e) {
                    DialogUtil.error("Gagal membuat booking: " + e.getMessage());
                } catch (IllegalStateException e) {
                    DialogUtil.error(e.getMessage());
                }
            }
        });
    }
}
