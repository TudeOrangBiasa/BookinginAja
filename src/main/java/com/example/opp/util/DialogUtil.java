package com.example.opp.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class untuk dialog konfirmasi dan alert dalam Bahasa Indonesia
 */
public final class DialogUtil {

    private DialogUtil() {}

    /**
     * Menampilkan dialog konfirmasi Ya/Tidak
     */
    public static boolean konfirmasi(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);

        ButtonType btnYa = new ButtonType("Ya", ButtonBar.ButtonData.YES);
        ButtonType btnTidak = new ButtonType("Tidak", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYa, btnTidak);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYa;
    }

    /**
     * Menampilkan dialog konfirmasi dengan pesan kustom
     */
    public static boolean konfirmasi(String judul, String header, String pesan) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(judul);
        alert.setHeaderText(header);
        alert.setContentText(pesan);

        ButtonType btnYa = new ButtonType("Ya", ButtonBar.ButtonData.YES);
        ButtonType btnTidak = new ButtonType("Tidak", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYa, btnTidak);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYa;
    }

    /**
     * Menampilkan pesan sukses
     */
    public static void sukses(String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Berhasil");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /**
     * Menampilkan pesan sukses dengan judul kustom
     */
    public static void sukses(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /**
     * Menampilkan pesan error
     */
    public static void error(String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /**
     * Menampilkan pesan error dengan judul kustom
     */
    public static void error(String judul, String pesan) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /**
     * Menampilkan pesan peringatan
     */
    public static void peringatan(String pesan) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Peringatan");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    /**
     * Menampilkan pesan informasi
     */
    public static void info(String pesan) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    // ============ Konfirmasi Spesifik ============

    public static boolean konfirmasiCheckIn(String kodeBooking, String namaKamar) {
        return konfirmasi("Konfirmasi Check-In",
            "Apakah Anda yakin ingin melakukan check-in?\n\n" +
            "Kode Booking: " + kodeBooking + "\n" +
            "Kamar: " + namaKamar);
    }

    public static boolean konfirmasiCheckOut(String kodeBooking, String namaKamar) {
        return konfirmasi("Konfirmasi Check-Out",
            "Apakah Anda yakin ingin melakukan check-out?\n\n" +
            "Kode Booking: " + kodeBooking + "\n" +
            "Kamar: " + namaKamar);
    }

    public static boolean konfirmasiBatalBooking(String kodeBooking) {
        return konfirmasi("Konfirmasi Pembatalan",
            "Apakah Anda yakin ingin membatalkan booking ini?\n\n" +
            "Kode Booking: " + kodeBooking + "\n\n" +
            "Tindakan ini tidak dapat dibatalkan!");
    }

    public static boolean konfirmasiHapus(String item) {
        return konfirmasi("Konfirmasi Hapus",
            "Apakah Anda yakin ingin menghapus " + item + "?\n\n" +
            "Tindakan ini tidak dapat dibatalkan!");
    }

    public static boolean konfirmasiSimpan(String item) {
        return konfirmasi("Konfirmasi Simpan",
            "Apakah Anda yakin ingin menyimpan " + item + "?");
    }

    public static boolean konfirmasiLogout() {
        return konfirmasi("Konfirmasi Logout",
            "Apakah Anda yakin ingin keluar dari sistem?");
    }

    public static boolean konfirmasiUbahStatus(String kamar, String statusBaru) {
        return konfirmasi("Konfirmasi Ubah Status",
            "Apakah Anda yakin ingin mengubah status kamar " + kamar + 
            " menjadi " + statusBaru + "?");
    }
}
