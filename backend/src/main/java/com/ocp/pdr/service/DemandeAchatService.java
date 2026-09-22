package com.ocp.pdr.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocp.pdr.dto.DemandeAchatStatisticsDto;
import com.ocp.pdr.dto.ImportResultDto;
import com.ocp.pdr.model.DemandeAchat;
import com.ocp.pdr.model.HistoriqueDA;
import com.ocp.pdr.model.enums.ResponsableDA;
import com.ocp.pdr.model.enums.SecteurDA;
import com.ocp.pdr.model.enums.StatutDA;
import com.ocp.pdr.model.enums.UdmDA;
import com.ocp.pdr.repository.DemandeAchatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeAchatService {

    private final DemandeAchatRepository repo;

    public List<DemandeAchat> findAll() {
        return repo.findAll();
    }

    public List<DemandeAchat> findAll(String search, StatutDA statut, SecteurDA secteur, ResponsableDA responsable, String fournisseur, String commande, String sort) {
        return sortReportings(filterReportings(repo.findAll(), search, statut, secteur, responsable, fournisseur, commande), sort);
    }

    public String export(String search, StatutDA statut, SecteurDA secteur, ResponsableDA responsable, String fournisseur, String commande, String sort) {
        List<DemandeAchat> reportings = findAll(search, statut, secteur, responsable, fournisseur, commande, sort);
        String header = "N°,Code Oracle,Code SAP,Description,UDM,Quantité,Secteur,CMD,Fournisseur,% Livraison,Délai,Statut,Responsable,Commentaire";
        String rows = reportings.stream().map(this::toCsvLine).collect(Collectors.joining("\n"));
        return header + (rows.isEmpty() ? "" : "\n" + rows);
    }

    public byte[] exportToExcel(String search, StatutDA statut, SecteurDA secteur, ResponsableDA responsable, String fournisseur, String commande, String sort) throws IOException {
        List<DemandeAchat> reportings = findAll(search, statut, secteur, responsable, fournisseur, commande, sort);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("DemandesAchat");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"N°", "Code Oracle", "Code SAP", "Description", "UDM", "Quantité", "Secteur", "CMD", "Fournisseur", "% Livraison", "Délai", "Statut", "Responsable", "Commentaire"};

            XSSFCellStyle headerStyle = (XSSFCellStyle) workbook.createCellStyle();
            XSSFFont headerFont = (XSSFFont) workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (DemandeAchat r : reportings) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getNumero() != null ? r.getNumero() : "");
                row.createCell(1).setCellValue(r.getCodeOracle() != null ? r.getCodeOracle() : "");
                row.createCell(2).setCellValue(r.getCodeSAP() != null ? r.getCodeSAP() : "");
                row.createCell(3).setCellValue(r.getDescription() != null ? r.getDescription() : "");
                row.createCell(4).setCellValue(r.getUniteDeMesure() != null ? r.getUniteDeMesure().name() : "");
                row.createCell(5).setCellValue(r.getQuantite() != null ? r.getQuantite() : 0.0);
                row.createCell(6).setCellValue(r.getSecteur() != null ? r.getSecteur().name() : "");
                row.createCell(7).setCellValue(r.getCommande() != null ? r.getCommande() : "");
                row.createCell(8).setCellValue(r.getFournisseur() != null ? r.getFournisseur() : "");
                row.createCell(9).setCellValue(r.getPourcentageLivraison() != null ? r.getPourcentageLivraison() : 0);
                row.createCell(10).setCellValue(r.getDelaiLivraison() != null ? r.getDelaiLivraison() : 0);
                row.createCell(11).setCellValue(r.getStatut() != null ? r.getStatut().name() : "");
                row.createCell(12).setCellValue(r.getResponsable() != null ? r.getResponsable().name() : "");
                row.createCell(13).setCellValue(r.getCommentaire() != null ? r.getCommentaire() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private String toCsvLine(DemandeAchat r) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                escapeCsv(r.getNumero()),
                escapeCsv(r.getCodeOracle()),
                escapeCsv(r.getCodeSAP()),
                escapeCsv(r.getDescription()),
                r.getUniteDeMesure() == null ? "" : r.getUniteDeMesure().name(),
                r.getQuantite() == null ? "" : r.getQuantite().toString(),
                r.getSecteur() == null ? "" : r.getSecteur().name(),
                escapeCsv(r.getCommande()),
                escapeCsv(r.getFournisseur()),
                r.getPourcentageLivraison() == null ? "" : r.getPourcentageLivraison().toString(),
                r.getDelaiLivraison() == null ? "" : r.getDelaiLivraison().toString(),
                r.getStatut() == null ? "" : r.getStatut().name(),
                r.getResponsable() == null ? "" : r.getResponsable().name(),
                escapeCsv(r.getCommentaire()));
    }

    @Transactional
    public ImportResultDto importFromExcel(InputStream inputStream) throws IOException {
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 1) {
                return new ImportResultDto(0, errors);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("La première ligne doit contenir les en-têtes de colonnes.");
            }

            List<String> expectedHeaders = Arrays.asList(
                    "DA", "Dossier", "N°", "code Oracle", "Code SAP", "Description",
                    "UDM", "Q retenue", "Secteur", "Fournisseur", "CMD", "%Livraison",
                    "Délai livraison", "Date notification", "Date Prévisionnelle livraison",
                    "Commentaire", "Statut Livraison", "Responsable Dossier"
            );

            Map<String, Integer> headerIndex = buildHeaderIndex(headerRow);
            List<String> foundHeaders = buildRawHeaderList(headerRow);
            validateRequiredHeaders(headerIndex, expectedHeaders, foundHeaders);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                DemandeAchat demande = buildReportingFromRow(row, headerIndex);
                List<String> rowErrors = new ArrayList<>();
                if (demande.getNumeroDA() == null && demande.getNumeroDossier() == null && demande.getNumero() == null) {
                    rowErrors.add("Identifiant manquant (DA, Dossier ou N°)");
                }
                if (demande.getQuantite() == null) {
                    rowErrors.add("Quantité manquante ou invalide");
                }

                if (!rowErrors.isEmpty()) {
                    errors.add("Ligne " + (rowIndex + 1) + " : " + String.join("; ", rowErrors));
                    continue;
                }

                save(demande);
                importedCount++;
            }
            return new ImportResultDto(importedCount, errors);
        }
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        DataFormatter formatter = new DataFormatter();
        Map<String, Integer> headerIndex = new HashMap<>();
        for (Cell cell : headerRow) {
            String headerValue = formatter.formatCellValue(cell);
            if (headerValue != null && !headerValue.isBlank()) {
                headerIndex.put(normalizeHeader(headerValue), cell.getColumnIndex());
            }
        }
        return headerIndex;
    }

    private List<String> buildRawHeaderList(Row headerRow) {
        DataFormatter formatter = new DataFormatter();
        List<String> rawHeaders = new ArrayList<>();
        for (Cell cell : headerRow) {
            String headerValue = formatter.formatCellValue(cell);
            rawHeaders.add(headerValue == null ? "" : headerValue.trim());
        }
        return rawHeaders;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndex, List<String> expectedHeaders, List<String> foundHeaders) {
        for (String expected : expectedHeaders) {
            String normalized = normalizeHeader(expected);
            if (!headerIndex.containsKey(normalized)) {
                throw new IllegalArgumentException("Colonne manquante : " + expected + ". En-têtes trouvées : " + String.join(", ", foundHeaders));
            }
        }
    }

    private String normalizeHeader(String header) {
        if (header == null) return null;
        String normalized = java.text.Normalizer.normalize(header.trim().toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private DemandeAchat buildReportingFromRow(Row row, Map<String, Integer> headerIndex) {
        DemandeAchat d = new DemandeAchat();
        d.setNumeroDA(getCellValue(row, headerIndex, "da", "DA"));
        d.setNumeroDossier(getCellValue(row, headerIndex, "dossier", "Dossier"));
        d.setNumero(getCellValue(row, headerIndex, "N°", "N", "n", "numero"));
        d.setCodeOracle(getCellValue(row, headerIndex, "codeOracle", "code Oracle"));
        d.setCodeSAP(getCellValue(row, headerIndex, "codeSAP", "Code SAP"));
        d.setDescription(getCellValue(row, headerIndex, "description", "Description"));
        d.setUniteDeMesure(parseEnumValue(row, headerIndex, UdmDA.class, "UDM", "Udm", "udm"));
        d.setQuantite(getDoubleValue(row, headerIndex, "Q retenue", "Qretenue", "q retenue", "quantite"));
        d.setSecteur(parseEnumValue(row, headerIndex, SecteurDA.class, "Secteur", "secteur"));
        d.setCommande(getCellValue(row, headerIndex, "CMD", "cmd", "Commande", "commande"));
        d.setFournisseur(getCellValue(row, headerIndex, "Fournisseur", "fournisseur"));
        d.setPourcentageLivraison(getIntegerValue(row, headerIndex, "%Livraison", "pourcentage livraison", "pourcentage"));
        d.setDelaiLivraison(getIntegerValue(row, headerIndex, "Délai livraison", "Delai livraison", "delai livraison"));
        d.setDateNotification(getDateValue(row, headerIndex, "Date notification", "date notification"));
        d.setDatePrevisionnelle(getDateValue(row, headerIndex, "Date Prévisionnelle livraison", "date prévisionnelle livraison"));
        d.setStatut(parseEnumValue(row, headerIndex, StatutDA.class, "statut livraison", "statut"));
        d.setResponsable(parseEnumValue(row, headerIndex, ResponsableDA.class, "Responsable Dossier", "responsable dossier", "responsable"));
        d.setCommentaire(getCellValue(row, headerIndex, "Commentaire", "commentaire"));
        return d;
    }

    private String getCellValue(Row row, Map<String, Integer> headerIndex, String... headerNames) {
        for (String headerName : headerNames) {
            Integer index = headerIndex.get(normalizeHeader(headerName));
            if (index == null) continue;
            Cell cell = row.getCell(index);
            String value = getCellString(cell);
            if (!value.isBlank()) return value;
        }
        return null;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private Double getDoubleValue(Row row, Map<String, Integer> headerIndex, String... headerNames) {
        String stringValue = getCellValue(row, headerIndex, headerNames);
        if (stringValue == null || stringValue.isBlank()) return null;
        try {
            return Double.parseDouble(normalizeNumber(stringValue));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntegerValue(Row row, Map<String, Integer> headerIndex, String... headerNames) {
        Double doubleValue = getDoubleValue(row, headerIndex, headerNames);
        return doubleValue == null ? null : doubleValue.intValue();
    }

    private String normalizeNumber(String value) {
        return value.trim().replaceAll("%", "").replaceAll("\\s+", "").replace(',', '.');
    }

    private <E extends Enum<E>> E parseEnumValue(Row row, Map<String, Integer> headerIndex, Class<E> enumType, String... headerNames) {
        String stringValue = getCellValue(row, headerIndex, headerNames);
        if (stringValue == null || stringValue.isBlank()) return null;
        String normalized = normalizeEnumValue(stringValue);
        try {
            return Enum.valueOf(enumType, normalized);
        } catch (IllegalArgumentException e) {
            return tryEnumSynonyms(enumType, normalized);
        }
    }

    private String normalizeEnumValue(String value) {
        return java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s\\-]", "_")
                .replaceAll("[^A-Z0-9_]", "");
    }

    private <E extends Enum<E>> E tryEnumSynonyms(Class<E> enumType, String normalized) {
        Map<String, String> synonyms = new HashMap<>();
        if (enumType == StatutDA.class) {
            synonyms.put("CONTENTIEUX", "LITIGE");
            synonyms.put("CONTENTIEUSE", "LITIGE");
            synonyms.put("ARTICLE_ADJUGE", "ADJUGE");
            synonyms.put("ARTICLES_ADJUGE", "ADJUGE");
        } else if (enumType == UdmDA.class) {
            synonyms.put("PIECE", "PCE");
            synonyms.put("PIECES", "PCE");
        }
        String mapped = synonyms.get(normalized);
        if (mapped != null) {
            try {
                return Enum.valueOf(enumType, mapped);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private LocalDate getDateValue(Row row, Map<String, Integer> headerIndex, String... headerNames) {
        Integer index = null;
        for (String headerName : headerNames) {
            Integer headerIndexValue = headerIndex.get(normalizeHeader(headerName));
            if (headerIndexValue != null) {
                index = headerIndexValue;
                break;
            }
        }
        if (index == null) return null;
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String stringValue = getCellString(cell);
        if (stringValue.isBlank()) return null;
        try {
            return LocalDate.parse(stringValue);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(stringValue, DateTimeFormatter.ofPattern("d/MM/yyyy"));
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    private List<DemandeAchat> filterReportings(List<DemandeAchat> list, String search, StatutDA statut, SecteurDA secteur, ResponsableDA responsable, String fournisseur, String commande) {
        return list.stream()
                .filter(r -> matchesStatut(r, statut))
                .filter(r -> matchesSecteur(r, secteur))
                .filter(r -> matchesResponsable(r, responsable))
                .filter(r -> matchesFournisseur(r, fournisseur))
                .filter(r -> matchesCommande(r, commande))
                .filter(r -> matchesSearch(r, search))
                .collect(Collectors.toList());
    }

    private boolean matchesStatut(DemandeAchat r, StatutDA statut) {
        return statut == null || statut.equals(r.getStatut());
    }

    private boolean matchesSecteur(DemandeAchat r, SecteurDA secteur) {
        return secteur == null || secteur.equals(r.getSecteur());
    }

    private boolean matchesResponsable(DemandeAchat r, ResponsableDA responsable) {
        return responsable == null || responsable.equals(r.getResponsable());
    }

    private boolean matchesFournisseur(DemandeAchat r, String fournisseur) {
        if (fournisseur == null || fournisseur.isBlank()) return true;
        return r.getFournisseur() != null && r.getFournisseur().toLowerCase().contains(fournisseur.trim().toLowerCase());
    }

    private boolean matchesCommande(DemandeAchat r, String commande) {
        if (commande == null || commande.isBlank()) return true;
        return r.getCommande() != null && r.getCommande().toLowerCase().contains(commande.trim().toLowerCase());
    }

    private boolean matchesSearch(DemandeAchat r, String search) {
        if (search == null || search.isBlank()) return true;
        String normalized = search.trim().toLowerCase();
        return Stream.of(
                r.getNumeroDA(), r.getNumeroDossier(), r.getNumero(), r.getCodeOracle(),
                r.getCodeSAP(), r.getDescription(), r.getCommande(), r.getFournisseur(), r.getCommentaire()
        ).filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(value -> value.contains(normalized));
    }

    private List<DemandeAchat> sortReportings(List<DemandeAchat> list, String sort) {
        if (sort == null || sort.isBlank()) return list;
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        boolean ascending = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].trim());
        Comparator<DemandeAchat> comparator = switch (field) {
            case "numeroDA" -> Comparator.comparing(DemandeAchat::getNumeroDA, Comparator.nullsLast(String::compareToIgnoreCase));
            case "numeroDossier" -> Comparator.comparing(DemandeAchat::getNumeroDossier, Comparator.nullsLast(String::compareToIgnoreCase));
            case "dateCreation" -> Comparator.comparing(DemandeAchat::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder()));
            case "dateModification" -> Comparator.comparing(DemandeAchat::getDateModification, Comparator.nullsLast(Comparator.naturalOrder()));
            case "quantite" -> Comparator.comparing(DemandeAchat::getQuantite, Comparator.nullsLast(Double::compareTo));
            default -> Comparator.comparing(DemandeAchat::getId, Comparator.nullsLast(Long::compareTo));
        };
        if (!ascending) comparator = comparator.reversed();
        return list.stream().sorted(comparator).collect(Collectors.toList());
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    public Optional<DemandeAchat> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public DemandeAchat save(DemandeAchat demande) {
        LocalDateTime now = LocalDateTime.now();
        if (demande.getId() == null) {
            demande.setDateCreation(now);
            demande.setDateModification(now);
            HistoriqueDA historique = HistoriqueDA.builder()
                    .action("Création du dossier")
                    .ancienneValeur(null)
                    .nouvelleValeur("Création initiale de la Demande d'Achat")
                    .dateAction(now)
                    .demandeAchat(demande)
                    .utilisateur(demande.getUtilisateur())
                    .build();
            demande.getHistoriques().add(historique);
            return repo.save(demande);
        }

        DemandeAchat existing = repo.findById(demande.getId())
                .orElseThrow(() -> new IllegalArgumentException("Demande d'achat introuvable avec l'ID: " + demande.getId()));

        List<HistoriqueDA> modifications = buildModifications(existing, demande, now);
        copyReportingFields(demande, existing);
        existing.setDateModification(now);
        existing.getHistoriques().addAll(modifications);
        return repo.save(existing);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public DemandeAchatStatisticsDto statistics() {
        List<DemandeAchat> list = repo.findAll();
        DemandeAchatStatisticsDto statistics = new DemandeAchatStatisticsDto();
        statistics.setTotalReportings(list.size());
        statistics.setReportingsByStatut(list.stream()
                .map(DemandeAchat::getStatut)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(StatutDA::name, Collectors.counting())));
        statistics.setReportingsBySecteur(list.stream()
                .map(DemandeAchat::getSecteur)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(SecteurDA::name, Collectors.counting())));
        statistics.setReportingsByResponsable(list.stream()
                .map(DemandeAchat::getResponsable)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ResponsableDA::name, Collectors.counting())));
        statistics.setTotalQuantite(list.stream()
                .filter(r -> r.getQuantite() != null)
                .mapToDouble(DemandeAchat::getQuantite)
                .sum());
        statistics.setAverageQuantite(list.stream()
                .filter(r -> r.getQuantite() != null)
                .mapToDouble(DemandeAchat::getQuantite)
                .average()
                .orElse(0.0));
        statistics.setAveragePourcentageLivraison(list.stream()
                .filter(r -> r.getPourcentageLivraison() != null)
                .mapToInt(DemandeAchat::getPourcentageLivraison)
                .average()
                .orElse(0.0));
        statistics.setAverageDelaiLivraison(list.stream()
                .filter(r -> r.getDelaiLivraison() != null)
                .mapToInt(DemandeAchat::getDelaiLivraison)
                .average()
                .orElse(0.0));
        return statistics;
    }

    public DemandeAchatStatisticsDto getStatistics() {
        return statistics();
    }

    private List<HistoriqueDA> buildModifications(DemandeAchat existing, DemandeAchat updated, LocalDateTime now) {
        List<HistoriqueDA> changements = new ArrayList<>();
        compareField(changements, existing.getNumeroDA(), updated.getNumeroDA(), "numéro DA", now, existing);
        compareField(changements, existing.getNumeroDossier(), updated.getNumeroDossier(), "numéro dossier", now, existing);
        compareField(changements, existing.getNumero(), updated.getNumero(), "N°", now, existing);
        compareField(changements, existing.getCodeOracle(), updated.getCodeOracle(), "code Oracle", now, existing);
        compareField(changements, existing.getCodeSAP(), updated.getCodeSAP(), "code SAP", now, existing);
        compareField(changements, existing.getDescription(), updated.getDescription(), "description", now, existing);
        compareField(changements, existing.getUniteDeMesure(), updated.getUniteDeMesure(), "unité de mesure", now, existing);
        compareField(changements, existing.getQuantite(), updated.getQuantite(), "quantité", now, existing);
        compareField(changements, existing.getSecteur(), updated.getSecteur(), "secteur", now, existing);
        compareField(changements, existing.getCommande(), updated.getCommande(), "commande", now, existing);
        compareField(changements, existing.getFournisseur(), updated.getFournisseur(), "fournisseur", now, existing);
        compareField(changements, existing.getPourcentageLivraison(), updated.getPourcentageLivraison(), "pourcentage livraison", now, existing);
        compareField(changements, existing.getDelaiLivraison(), updated.getDelaiLivraison(), "délai livraison", now, existing);
        compareField(changements, existing.getDateNotification(), updated.getDateNotification(), "date notification", now, existing);
        compareField(changements, existing.getDatePrevisionnelle(), updated.getDatePrevisionnelle(), "date prévisionnelle", now, existing);
        compareField(changements, existing.getStatut(), updated.getStatut(), "statut", now, existing);
        compareField(changements, existing.getResponsable(), updated.getResponsable(), "responsable", now, existing);
        compareField(changements, existing.getCommentaire(), updated.getCommentaire(), "commentaire", now, existing);
        return changements;
    }

    private <T> void compareField(List<HistoriqueDA> changements, T oldVal, T newVal, String champ, LocalDateTime now, DemandeAchat demande) {
        if (!Objects.equals(oldVal, newVal)) {
            changements.add(HistoriqueDA.builder()
                    .action("Modification " + champ)
                    .ancienneValeur(oldVal != null ? oldVal.toString() : "")
                    .nouvelleValeur(newVal != null ? newVal.toString() : "")
                    .dateAction(now)
                    .demandeAchat(demande)
                    .utilisateur(demande.getUtilisateur())
                    .build());
        }
    }

    private void copyReportingFields(DemandeAchat source, DemandeAchat target) {
        target.setNumeroDA(source.getNumeroDA());
        target.setNumeroDossier(source.getNumeroDossier());
        target.setNumero(source.getNumero());
        target.setCodeOracle(source.getCodeOracle());
        target.setCodeSAP(source.getCodeSAP());
        target.setDescription(source.getDescription());
        target.setUniteDeMesure(source.getUniteDeMesure());
        target.setQuantite(source.getQuantite());
        target.setSecteur(source.getSecteur());
        target.setCommande(source.getCommande());
        target.setFournisseur(source.getFournisseur());
        target.setPourcentageLivraison(source.getPourcentageLivraison());
        target.setDelaiLivraison(source.getDelaiLivraison());
        target.setDateNotification(source.getDateNotification());
        target.setDatePrevisionnelle(source.getDatePrevisionnelle());
        target.setStatut(source.getStatut());
        target.setResponsable(source.getResponsable());
        target.setCommentaire(source.getCommentaire());
        if (source.getUtilisateur() != null) {
            target.setUtilisateur(source.getUtilisateur());
        }
    }
}
