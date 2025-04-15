package com.elvecha.util;

import com.elvecha.model.Alternative;
import com.elvecha.model.Criteria;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFExporter {
    private static final DecimalFormat df = new DecimalFormat("#.###");

    public static void exportResults(String filePath, 
                                   List<Alternative> alternatives,
                                   List<Criteria> criteria) throws Exception {
        // Create PDF document
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Add title
        Paragraph title = new Paragraph("Hasil Perhitungan SAW - El Vecha Wedding Organizer")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(16)
            .setBold();
        document.add(title);

        // Add date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
        Paragraph date = new Paragraph("Tanggal: " + dateFormat.format(new Date()))
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(10);
        document.add(date);

        document.add(new Paragraph("\n"));

        // Add criteria information
        document.add(new Paragraph("Kriteria yang Digunakan:").setBold());
        Table criteriaTable = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
        
        // Add criteria headers
        criteriaTable.addCell(new Cell().add(new Paragraph("No").setBold()));
        criteriaTable.addCell(new Cell().add(new Paragraph("Nama Kriteria").setBold()));
        criteriaTable.addCell(new Cell().add(new Paragraph("Bobot").setBold()));
        criteriaTable.addCell(new Cell().add(new Paragraph("Jenis").setBold()));

        // Add criteria data
        int counter = 1;
        for (Criteria crit : criteria) {
            criteriaTable.addCell(new Cell().add(new Paragraph(String.valueOf(counter++))));
            criteriaTable.addCell(new Cell().add(new Paragraph(crit.getName())));
            criteriaTable.addCell(new Cell().add(new Paragraph(df.format(crit.getWeight()))));
            criteriaTable.addCell(new Cell().add(new Paragraph(crit.getType())));
        }
        document.add(criteriaTable);

        document.add(new Paragraph("\n"));

        // Add ranking results
        document.add(new Paragraph("Hasil Peringkat:").setBold());
        Table resultTable = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
        
        // Add result headers
        resultTable.addCell(new Cell().add(new Paragraph("Peringkat").setBold()));
        resultTable.addCell(new Cell().add(new Paragraph("Wedding Organizer").setBold()));
        resultTable.addCell(new Cell().add(new Paragraph("Nilai Akhir").setBold()));
        resultTable.addCell(new Cell().add(new Paragraph("Status").setBold()));

        // Add result data
        counter = 1;
        for (Alternative alt : alternatives) {
            resultTable.addCell(new Cell().add(new Paragraph(String.valueOf(counter++))));
            resultTable.addCell(new Cell().add(new Paragraph(alt.getName())));
            resultTable.addCell(new Cell().add(new Paragraph(df.format(alt.getFinalScore()))));
            
            String status = alt.getFinalScore() >= 0.7 ? "Sangat Direkomendasikan" :
                          alt.getFinalScore() >= 0.5 ? "Direkomendasikan" : 
                          "Kurang Direkomendasikan";
            resultTable.addCell(new Cell().add(new Paragraph(status)));
        }
        document.add(resultTable);

        document.add(new Paragraph("\n"));

        // Add detailed scores
        document.add(new Paragraph("Detail Nilai per Kriteria:").setBold());
        Table detailTable = new Table(UnitValue.createPercentArray(criteria.size() + 1))
            .useAllAvailableWidth();
        
        // Add detail headers
        detailTable.addCell(new Cell().add(new Paragraph("Wedding Organizer").setBold()));
        for (Criteria crit : criteria) {
            detailTable.addCell(new Cell().add(new Paragraph(crit.getName()).setBold()));
        }

        // Add detail data
        for (Alternative alt : alternatives) {
            detailTable.addCell(new Cell().add(new Paragraph(alt.getName())));
            for (Criteria crit : criteria) {
                Double value = alt.getCriteriaValue(crit.getName());
                detailTable.addCell(new Cell().add(
                    new Paragraph(value != null ? df.format(value) : "-")
                ));
            }
        }
        document.add(detailTable);

        // Add footer
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("* Dokumen ini digenerate secara otomatis oleh sistem")
            .setFontSize(8)
            .setItalic());

        // Close document
        document.close();
    }
}
