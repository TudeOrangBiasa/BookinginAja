package com.example.opp.controller;

import com.example.opp.model.Booking;
import com.example.opp.model.BookingStatus;
import com.example.opp.repository.BookingRepository;
import com.example.opp.service.RoomService;
import com.example.opp.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportsController {

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Label totalBookings;
    @FXML private Label totalRevenue;
    @FXML private Label avgStay;
    @FXML private Label occupancyRate;
    @FXML private TableView<DailyReport> reportTable;
    @FXML private TableColumn<DailyReport, String> dateCol;
    @FXML private TableColumn<DailyReport, String> bookingsCol;
    @FXML private TableColumn<DailyReport, String> checkInsCol;
    @FXML private TableColumn<DailyReport, String> checkOutsCol;
    @FXML private TableColumn<DailyReport, String> revenueCol;

    private final BookingRepository bookingRepository = new BookingRepository();
    private final RoomService roomService = new RoomService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private List<Booking> currentBookings = new ArrayList<>();

    @FXML
    public void initialize() {
        fromDate.setValue(LocalDate.now().minusMonths(1));
        toDate.setValue(LocalDate.now());
        setupTable();
        handleGenerate();
    }

    private void setupTable() {
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().date()));
        bookingsCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().bookings())));
        checkInsCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().checkIns())));
        checkOutsCol.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().checkOuts())));
        revenueCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().revenue()));
    }


    @FXML
    private void handleGenerate() {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        if (from == null || to == null) {
            DialogUtil.peringatan("Silakan pilih rentang tanggal");
            return;
        }
        if (from.isAfter(to)) {
            DialogUtil.peringatan("Tanggal awal tidak boleh setelah tanggal akhir");
            return;
        }

        try {
            List<Booking> allBookings = bookingRepository.findAll();
            
            // Filter: booking yang check-in atau check-out dalam rentang tanggal
            currentBookings = allBookings.stream()
                .filter(b -> {
                    LocalDate checkIn = b.getCheckInDate();
                    LocalDate checkOut = b.getCheckOutDate();
                    // Include jika check-in atau check-out dalam range
                    return (!checkIn.isBefore(from) && !checkIn.isAfter(to)) ||
                           (!checkOut.isBefore(from) && !checkOut.isAfter(to)) ||
                           (checkIn.isBefore(from) && checkOut.isAfter(to));
                })
                .toList();

            calculateStats(currentBookings);
            generateDailyReport(from, to, allBookings);

        } catch (SQLException e) {
            DialogUtil.error("Gagal memuat data laporan: " + e.getMessage());
        }
    }

    private void calculateStats(List<Booking> bookings) throws SQLException {
        // Total booking (exclude cancelled)
        long totalBookingCount = bookings.stream()
            .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
            .count();
        totalBookings.setText(String.valueOf(totalBookingCount));

        // Total pendapatan (CHECKED_IN + CHECKED_OUT = sudah bayar)
        BigDecimal revenue = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN || b.getStatus() == BookingStatus.CHECKED_OUT)
            .map(Booking::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalRevenue.setText(String.format("Rp %,.0f", revenue));

        // Rata-rata menginap
        double avgNights = bookings.stream()
            .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
            .mapToInt(Booking::getTotalNights)
            .average()
            .orElse(0);
        avgStay.setText(String.format("%.1f malam", avgNights));

        // Tingkat hunian
        int totalRooms = roomService.getTotalRoomCount();
        long occupiedDays = bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN || b.getStatus() == BookingStatus.CHECKED_OUT)
            .mapToInt(Booking::getTotalNights)
            .sum();
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate.getValue(), toDate.getValue()) + 1;
        double occupancy = totalRooms > 0 && daysBetween > 0 
            ? (double) occupiedDays / (totalRooms * daysBetween) * 100 
            : 0;
        occupancyRate.setText(String.format("%.1f%%", Math.min(occupancy, 100)));
    }

    private void generateDailyReport(LocalDate from, LocalDate to, List<Booking> allBookings) {
        ObservableList<DailyReport> reports = FXCollections.observableArrayList();
        
        // Kumpulkan semua tanggal unik dari check-in dan check-out dalam range
        Set<LocalDate> relevantDates = new TreeSet<>();
        
        for (Booking b : allBookings) {
            LocalDate checkIn = b.getCheckInDate();
            LocalDate checkOut = b.getCheckOutDate();
            
            // Tambahkan check-in date jika dalam range
            if (!checkIn.isBefore(from) && !checkIn.isAfter(to)) {
                relevantDates.add(checkIn);
            }
            // Tambahkan check-out date jika dalam range
            if (!checkOut.isBefore(from) && !checkOut.isAfter(to)) {
                relevantDates.add(checkOut);
            }
        }
        
        // Generate report untuk setiap tanggal yang relevan
        for (LocalDate date : relevantDates) {
            // Booking dengan check-in pada tanggal ini
            long checkInsOnDate = allBookings.stream()
                .filter(b -> b.getCheckInDate().equals(date))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();
            
            // Booking dengan check-out pada tanggal ini
            long checkOutsOnDate = allBookings.stream()
                .filter(b -> b.getCheckOutDate().equals(date))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .count();
            
            // Pendapatan: dari booking yang check-in pada tanggal ini
            BigDecimal revenueOnDate = allBookings.stream()
                .filter(b -> b.getCheckInDate().equals(date))
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .map(Booking::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            reports.add(new DailyReport(
                date.format(DATE_FMT),
                (int) checkInsOnDate, // Jumlah booking = check-in
                (int) checkInsOnDate,
                (int) checkOutsOnDate,
                String.format("Rp %,.0f", revenueOnDate)
            ));
        }
        
        // Sort descending (terbaru di atas)
        reports.sort((a, b) -> b.date().compareTo(a.date()));
        reportTable.setItems(reports);
        
        // Jika masih kosong, tampilkan semua booking sebagai list
        if (reports.isEmpty()) {
            generateBookingListReport(allBookings);
        }
    }

    private void generateBookingListReport(List<Booking> allBookings) {
        ObservableList<DailyReport> reports = FXCollections.observableArrayList();
        
        for (Booking b : allBookings) {
            if (b.getStatus() == BookingStatus.CANCELLED) continue;
            
            reports.add(new DailyReport(
                b.getCheckInDate().format(DATE_FMT) + " - " + b.getCheckOutDate().format(DATE_FMT),
                1,
                1,
                b.getStatus() == BookingStatus.CHECKED_OUT ? 1 : 0,
                String.format("Rp %,.0f", b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
            ));
        }
        
        reportTable.setItems(reports);
    }


    @FXML
    private void handleExportPdf() {
        if (reportTable.getItems().isEmpty()) {
            DialogUtil.peringatan("Tidak ada data untuk diekspor. Silakan generate laporan terlebih dahulu.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Laporan");
        fileChooser.setInitialFileName("laporan_hotel_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            exportToFile(file);
        }
    }

    private void exportToFile(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Header
            writer.println("=".repeat(70));
            writer.println("              LAPORAN HOTEL MANAGEMENT SYSTEM");
            writer.println("=".repeat(70));
            writer.println();
            writer.println("Periode: " + fromDate.getValue().format(DATE_FMT) + " - " + toDate.getValue().format(DATE_FMT));
            writer.println("Tanggal Cetak: " + LocalDate.now().format(DATE_FMT));
            writer.println();
            
            // Summary
            writer.println("-".repeat(70));
            writer.println("RINGKASAN");
            writer.println("-".repeat(70));
            writer.println("Total Booking     : " + totalBookings.getText());
            writer.println("Total Pendapatan  : " + totalRevenue.getText());
            writer.println("Rata-rata Menginap: " + avgStay.getText());
            writer.println("Tingkat Hunian    : " + occupancyRate.getText());
            writer.println();
            
            // Table
            writer.println("-".repeat(70));
            writer.println("DETAIL HARIAN");
            writer.println("-".repeat(70));
            writer.printf("%-15s %-10s %-10s %-10s %-20s%n", "Tanggal", "Booking", "Check-in", "Check-out", "Pendapatan");
            writer.println("-".repeat(70));
            
            for (DailyReport report : reportTable.getItems()) {
                writer.printf("%-15s %-10d %-10d %-10d %-20s%n",
                    report.date(),
                    report.bookings(),
                    report.checkIns(),
                    report.checkOuts(),
                    report.revenue()
                );
            }
            
            writer.println("-".repeat(70));
            writer.println();
            writer.println("Dicetak oleh: Hotel Management System");
            
            DialogUtil.sukses("Laporan berhasil diekspor ke:\n" + file.getAbsolutePath());
            
        } catch (Exception e) {
            DialogUtil.error("Gagal mengekspor laporan: " + e.getMessage());
        }
    }

    // Record untuk data tabel
    public record DailyReport(String date, int bookings, int checkIns, int checkOuts, String revenue) {}
}
