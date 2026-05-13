package com.medkit.backend.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.medkit.backend.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class PdfGeneratorService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public byte[] generateVisitCertificate(
            Appointment appointment,
            Patient patient,
            Doctor doctor,
            List<AppointmentDiagnosis> diagnoses,
            LocalDate validFrom,
            LocalDate validTo
    ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 50, 40, 50);

        try {
            // Load fonts from classpath as byte arrays with proper embedding strategy
            byte[] fontBytes = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf").readAllBytes();
            byte[] boldFontBytes = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans-Bold.ttf").readAllBytes();

            PdfFont font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            PdfFont boldFont = PdfFontFactory.createFont(boldFontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

            // Header
            document.add(new Paragraph("МЕДИЦИНСКАЯ СПРАВКА")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginBottom(5));

            document.add(new Paragraph("о посещении врача")
                    .setFont(font)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Certificate number and date
            document.add(new Paragraph("№ " + appointment.getIdAppointment() + " от " +
                    LocalDate.now().format(DATE_FORMATTER))
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(25));

            // Main text
            document.add(new Paragraph("Настоящая справка выдана в том, что:")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(15));

            // Patient information
            document.add(new Paragraph("Пациент: " + getPatientFullName(patient))
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setMarginBottom(8));

            document.add(new Paragraph("Дата рождения: " + patient.getBirthdate().format(DATE_FORMATTER))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(5));

            document.add(new Paragraph("СНИЛС: " + formatSnils(patient.getSnils()))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(20));

            // Visit information
            document.add(new Paragraph("действительно находился(ась) на приеме у врача " +
                    appointment.getSlot().getSlotDate().format(DATE_FORMATTER) + " г.")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(15));

            document.add(new Paragraph("Врач: " + getDoctorFullName(doctor))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(5));

            document.add(new Paragraph("Специализация: " + doctor.getSpecialization())
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(20));

            // Diagnosis
            if (!diagnoses.isEmpty()) {
                document.add(new Paragraph("Диагноз:")
                        .setFont(boldFont)
                        .setFontSize(12)
                        .setMarginBottom(8));

                for (AppointmentDiagnosis ad : diagnoses) {
                    if (ad.getIsPrimary() != null && ad.getIsPrimary()) {
                        String diagnosisText = ad.getDiagnosis().getIcdCode() + " - " +
                                ad.getDiagnosis().getIcdName();
                        document.add(new Paragraph(diagnosisText)
                                .setFont(font)
                                .setFontSize(11)
                                .setMarginBottom(5));
                        break;
                    }
                }
            }

            // Recommendations
            if (appointment.getRecommendations() != null && !appointment.getRecommendations().isEmpty()) {
                document.add(new Paragraph("Заключение врача:")
                        .setFont(boldFont)
                        .setFontSize(12)
                        .setMarginTop(15)
                        .setMarginBottom(8));
                document.add(new Paragraph(appointment.getRecommendations())
                        .setFont(font)
                        .setFontSize(11)
                        .setMarginBottom(20));
            }

            // Validity period
            if (validFrom != null && validTo != null) {
                document.add(new Paragraph("Справка действительна с " + validFrom.format(DATE_FORMATTER) +
                        " по " + validTo.format(DATE_FORMATTER) + " г.")
                        .setFont(font)
                        .setFontSize(10)
                        .setMarginTop(20)
                        .setMarginBottom(40)
                        .setItalic());
            } else {
                document.add(new Paragraph("\n\n\n"));
            }

            // Signature section
            Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth()
                    .setMarginTop(30);

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("Лечащий врач:")
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(5));

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("_______________  " + getDoctorShortName(doctor))
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(5));

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("")
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(5));

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("(подпись)")
                            .setFont(font)
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(20));

            document.add(signatureTable);

            // Stamp placeholder
            document.add(new Paragraph("М.П.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginTop(10)
                    .setMarginBottom(5));

            document.add(new Paragraph("(место для печати)")
                    .setFont(font)
                    .setFontSize(8)
                    .setItalic()
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            // Issue date
            document.add(new Paragraph("Дата выдачи: " + LocalDate.now().format(DATE_FORMATTER) + " г.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginTop(10));

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    public byte[] generateWorkStudyCertificate(
            Appointment appointment,
            Patient patient,
            Doctor doctor,
            List<AppointmentDiagnosis> diagnoses,
            LocalDate validFrom,
            LocalDate validTo,
            LocalDate disabilityPeriodFrom,
            LocalDate disabilityPeriodTo,
            String workRestrictions
    ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(40, 50, 40, 50);

        try {
            // Load fonts from classpath as byte arrays
            byte[] fontBytes = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans.ttf").readAllBytes();
            byte[] boldFontBytes = getClass().getClassLoader().getResourceAsStream("fonts/DejaVuSans-Bold.ttf").readAllBytes();

            PdfFont font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);
            PdfFont boldFont = PdfFontFactory.createFont(boldFontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED);

            // Header with form number
            document.add(new Paragraph("МЕДИЦИНСКАЯ СПРАВКА")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setMarginBottom(3));

            document.add(new Paragraph("Форма № 095/у")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5));

            document.add(new Paragraph("для предоставления по месту работы/учебы")
                    .setFont(font)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Certificate number and date
            document.add(new Paragraph("№ " + appointment.getIdAppointment() + " от " +
                    LocalDate.now().format(DATE_FORMATTER) + " г.")
                    .setFont(font)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(25));

            // Main text
            document.add(new Paragraph("Настоящая справка выдана в том, что гражданин(ка):")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(15));

            // Patient information
            document.add(new Paragraph(getPatientFullName(patient))
                    .setFont(boldFont)
                    .setFontSize(12)
                    .setMarginBottom(8));

            document.add(new Paragraph("Дата рождения: " + patient.getBirthdate().format(DATE_FORMATTER) + " г.")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(5));

            document.add(new Paragraph("СНИЛС: " + formatSnils(patient.getSnils()))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(20));

            // Visit information
            document.add(new Paragraph("действительно находился(ась) на приеме в медицинском учреждении " +
                    appointment.getSlot().getSlotDate().format(DATE_FORMATTER) + " г.")
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(15));

            document.add(new Paragraph("Врач: " + getDoctorFullName(doctor))
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(5));

            document.add(new Paragraph("Специализация: " + doctor.getSpecialization())
                    .setFont(font)
                    .setFontSize(11)
                    .setMarginBottom(20));

            // Diagnosis
            if (!diagnoses.isEmpty()) {
                document.add(new Paragraph("Диагноз:")
                        .setFont(boldFont)
                        .setFontSize(12)
                        .setMarginBottom(8));

                for (AppointmentDiagnosis ad : diagnoses) {
                    if (ad.getIsPrimary() != null && ad.getIsPrimary()) {
                        String diagnosisText = ad.getDiagnosis().getIcdCode() + " - " +
                                ad.getDiagnosis().getIcdName();
                        document.add(new Paragraph(diagnosisText)
                                .setFont(font)
                                .setFontSize(11)
                                .setMarginBottom(15));
                        break;
                    }
                }
            }

            // Disability period
            if (disabilityPeriodFrom != null && disabilityPeriodTo != null) {
                document.add(new Paragraph("Период нетрудоспособности:")
                        .setFont(boldFont)
                        .setFontSize(12)
                        .setMarginBottom(8));
                document.add(new Paragraph("с " + disabilityPeriodFrom.format(DATE_FORMATTER) +
                        " г. по " + disabilityPeriodTo.format(DATE_FORMATTER) + " г.")
                        .setFont(font)
                        .setFontSize(11)
                        .setMarginBottom(15));
            }

            // Work restrictions
            if (workRestrictions != null && !workRestrictions.isEmpty()) {
                document.add(new Paragraph("Рекомендации по режиму:")
                        .setFont(boldFont)
                        .setFontSize(12)
                        .setMarginBottom(8));
                document.add(new Paragraph(workRestrictions)
                        .setFont(font)
                        .setFontSize(11)
                        .setMarginBottom(20));
            }

            // Validity period
            if (validFrom != null && validTo != null) {
                document.add(new Paragraph("Справка действительна с " + validFrom.format(DATE_FORMATTER) +
                        " г. по " + validTo.format(DATE_FORMATTER) + " г.")
                        .setFont(font)
                        .setFontSize(10)
                        .setMarginTop(20)
                        .setMarginBottom(40)
                        .setItalic());
            } else {
                document.add(new Paragraph("\n\n\n"));
            }

            // Signature section
            Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .useAllAvailableWidth()
                    .setMarginTop(30);

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("Лечащий врач:")
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(5));

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("_______________  " + getDoctorShortName(doctor))
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(5));

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("")
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(5));

            signatureTable.addCell(new Cell()
                    .add(new Paragraph("(подпись)")
                            .setFont(font)
                            .setFontSize(9)
                            .setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER)
                    .setPaddingBottom(20));

            document.add(signatureTable);

            // Stamp placeholder
            document.add(new Paragraph("М.П.")
                    .setFont(boldFont)
                    .setFontSize(11)
                    .setMarginTop(10)
                    .setMarginBottom(5));

            document.add(new Paragraph("(место для печати медицинского учреждения)")
                    .setFont(font)
                    .setFontSize(8)
                    .setItalic()
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            // Issue date
            document.add(new Paragraph("Дата выдачи: " + LocalDate.now().format(DATE_FORMATTER) + " г.")
                    .setFont(font)
                    .setFontSize(10)
                    .setMarginTop(10));

            // Footer note
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Справка выдана для предъявления по месту требования.")
                    .setFont(font)
                    .setFontSize(9)
                    .setItalic()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(15));

        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private void addInfoRow(Table table, String label, String value, PdfFont font, PdfFont boldFont) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(boldFont).setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(11))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5));
    }

    private String getPatientFullName(Patient patient) {
        StringBuilder name = new StringBuilder();
        name.append(patient.getUser().getLastName()).append(" ");
        name.append(patient.getUser().getFirstName());
        if (patient.getUser().getMiddleName() != null) {
            name.append(" ").append(patient.getUser().getMiddleName());
        }
        return name.toString();
    }

    private String getDoctorFullName(Doctor doctor) {
        StringBuilder name = new StringBuilder();
        name.append(doctor.getUser().getLastName()).append(" ");
        name.append(doctor.getUser().getFirstName());
        if (doctor.getUser().getMiddleName() != null) {
            name.append(" ").append(doctor.getUser().getMiddleName());
        }
        return name.toString();
    }

    private String getDoctorShortName(Doctor doctor) {
        StringBuilder name = new StringBuilder();
        name.append(doctor.getUser().getLastName()).append(" ");
        name.append(doctor.getUser().getFirstName().charAt(0)).append(".");
        if (doctor.getUser().getMiddleName() != null) {
            name.append(doctor.getUser().getMiddleName().charAt(0)).append(".");
        }
        return name.toString();
    }

    private String formatSnils(String snils) {
        if (snils == null || snils.length() != 11) {
            return snils;
        }
        return snils.substring(0, 3) + "-" + snils.substring(3, 6) + "-" +
               snils.substring(6, 9) + " " + snils.substring(9);
    }
}
