package com.ocp.pdr.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.ocp.pdr.model.AnomalieConsommation;
import com.ocp.pdr.model.ResultatApprovisionnement;
import com.ocp.pdr.model.enums.ModeApprovisionnement;
import com.ocp.pdr.repository.AnomalieConsommationRepository;
import com.ocp.pdr.repository.ResultatApprovisionnementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final ResultatApprovisionnementRepository resultatRepository;
    private final AnomalieConsommationRepository anomalieRepository;

    public ByteArrayInputStream exportResultats() {
        List<ResultatApprovisionnement> allResultats = resultatRepository.findAllWithArticle();
        List<AnomalieConsommation> allAnomalies = anomalieRepository.findAllWithArticle();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            createResultSheet(workbook, "Reporting Consolidé", allResultats, headerStyle);

            List<ResultatApprovisionnement> minMax = allResultats.stream()
                    .filter(r -> r.getMode() == ModeApprovisionnement.MIN_MAX)
                    .collect(Collectors.toList());
            createResultSheet(workbook, "1. Min & Max", minMax, headerStyle);

            List<ResultatApprovisionnement> planifies = allResultats.stream()
                    .filter(r -> r.getMode() == ModeApprovisionnement.PLANIFIE)
                    .collect(Collectors.toList());
            createResultSheet(workbook, "2. Mode Planifié", planifies, headerStyle);

            List<ResultatApprovisionnement> surDemande = allResultats.stream()
                    .filter(r -> r.getMode() == ModeApprovisionnement.SUR_DEMANDE)
                    .collect(Collectors.toList());
            createResultSheet(workbook, "3. Sur Demande", surDemande, headerStyle);

            createAnomalySheet(workbook, "4. Anomalies Conso", allAnomalies, headerStyle);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export Excel: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportMinMax() {
        return exportResultatsParMode(ModeApprovisionnement.MIN_MAX, "1. Min & Max");
    }

    public ByteArrayInputStream exportPlanifie() {
        return exportResultatsParMode(ModeApprovisionnement.PLANIFIE, "2. Mode Planifié");
    }

    public ByteArrayInputStream exportSurDemande() {
        return exportResultatsParMode(ModeApprovisionnement.SUR_DEMANDE, "3. Sur Demande");
    }

    private ByteArrayInputStream exportResultatsParMode(ModeApprovisionnement mode, String sheetName) {
        List<ResultatApprovisionnement> resultats = resultatRepository.findAllWithArticle().stream()
                .filter(r -> r.getMode() == mode)
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            createResultSheet(workbook, sheetName, resultats, headerStyle);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export Excel de la règle " + mode + ": " + e.getMessage());
        }
    }

    private void createResultSheet(Workbook workbook, String sheetName, List<ResultatApprovisionnement> list, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Code SAP", "Description", "UDM", "Quantité demandée", "Catégorie"};

        for (int col = 0; col < columns.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(columns[col]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (ResultatApprovisionnement res : list) {
            Row row = sheet.createRow(rowIdx++);
            var art = res.getArticle();

            row.createCell(0).setCellValue(art != null ? art.getCodeSAP() : "");
            row.createCell(1).setCellValue(art != null && art.getDescription() != null ? art.getDescription() : "");
            row.createCell(2).setCellValue(art != null && art.getUdm() != null ? art.getUdm() : "");
            row.createCell(3).setCellValue(res.getQuantiteALancer() != null ? res.getQuantiteALancer() : 0.0);
            row.createCell(4).setCellValue(art != null && art.getCategorie() != null ? art.getCategorie() : "");
        }

        for (int col = 0; col < columns.length; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    private void createAnomalySheet(Workbook workbook, String sheetName, List<AnomalieConsommation> list, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Code SAP", "Désignation", "Qté Installée", "Conso Mensuelle", "Taux Conso (%)", "Seuil Dépassé", "Statut", "Date Détection"};

        for (int col = 0; col < columns.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(columns[col]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (AnomalieConsommation anom : list) {
            Row row = sheet.createRow(rowIdx++);
            var art = anom.getArticle();

            row.createCell(0).setCellValue(art != null ? art.getCodeSAP() : "");
            row.createCell(1).setCellValue(art != null && art.getDescription() != null ? art.getDescription() : "");
            row.createCell(2).setCellValue(anom.getQuantiteInstallee() != null ? anom.getQuantiteInstallee() : 0.0);
            row.createCell(3).setCellValue(anom.getConsommationMensuelle() != null ? anom.getConsommationMensuelle() : 0.0);
            row.createCell(4).setCellValue(anom.getTauxConsommation() != null ? (anom.getTauxConsommation() * 100) + "%" : "0%");
            row.createCell(5).setCellValue("> 200%");
            row.createCell(6).setCellValue(anom.getStatut() != null ? anom.getStatut() : "DETECTEE");
            row.createCell(7).setCellValue(anom.getDateDetection() != null ? anom.getDateDetection().toString() : "");
        }

        for (int col = 0; col < columns.length; col++) {
            sheet.autoSizeColumn(col);
        }
    }

    public ByteArrayInputStream exportAnomalies() {
        List<AnomalieConsommation> allAnomalies = anomalieRepository.findAllWithArticle();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Style d'en-tête
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            createAnomalySheet(workbook, "Anomalies Consommation", allAnomalies, headerStyle);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export des anomalies: " + e.getMessage());
        }
    }
}