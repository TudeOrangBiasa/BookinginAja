package com.example.opp.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExporter {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(108, 92, 231);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static void exportReport(File file, LocalDate fromDate, LocalDate toDate,
                                    String totalBookings, String totalRevenue,
                                    String avgStay, String occupancyRate,
                                    List<ReportRow> data) throws Exception {

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header
        Paragraph title = new Paragraph("LAPORAN HOTEL")
                .setFontSize(24)
                .setBold()
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        Paragraph period = new Paragraph("Periode: " + fromDate.format(DATE_FMT) + " - " + toDate.format(DATE_FMT))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(period);

        // Summary
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        summaryTable.addCell(createSummaryCell("Total Booking", totalBookings));
        summaryTable.addCell(createSummaryCell("Total Pendapatan", totalRevenue));
        summaryTable.addCell(createSummaryCell("Rata-rata Menginap", avgStay));
        summaryTable.addCell(createSummaryCell("Tingkat Hunian", occupancyRate));

        document.add(summaryTable);

        // Detail Table
        Paragraph detailTitle = new Paragraph("Ringkasan Booking")
                .setFontSize(16)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(detailTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 2, 2, 2, 3}))
                .useAllAvailableWidth();

        // Header
        table.addHeaderCell(createHeaderCell("No"));
        table.addHeaderCell(createHeaderCell("Tanggal"));
        table.addHeaderCell(createHeaderCell("Booking"));
        table.addHeaderCell(createHeaderCell("Check-in"));
        table.addHeaderCell(createHeaderCell("Check-out"));
        table.addHeaderCell(createHeaderCell("Pendapatan"));

        // Data
        int no = 1;
        for (ReportRow row : data) {
            table.addCell(createDataCell(String.valueOf(no++)));
            table.addCell(createDataCell(row.date()));
            table.addCell(createDataCell(String.valueOf(row.bookings())));
            table.addCell(createDataCell(String.valueOf(row.checkIns())));
            table.addCell(createDataCell(String.valueOf(row.checkOuts())));
            table.addCell(createDataCell(row.revenue()));
        }

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("Dicetak pada: " + LocalDate.now().format(DATE_FMT))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20);
        document.add(footer);

        document.close();
    }

    private static Cell createSummaryCell(String label, String value) {
        Paragraph labelPara = new Paragraph(label)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY);
        Paragraph valuePara = new Paragraph(value)
                .setFontSize(16)
                .setBold();

        return new Cell()
                .add(labelPara)
                .add(valuePara)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10)
                .setBackgroundColor(new DeviceRgb(250, 250, 250));
    }

    private static Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
    }

    private static Cell createDataCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6);
    }

    public record ReportRow(String date, int bookings, int checkIns, int checkOuts, String revenue) {}
}
