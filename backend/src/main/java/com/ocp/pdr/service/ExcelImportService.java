package com.ocp.pdr.service;

import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.ocp.pdr.dto.response.ExcelValidationResult;
import com.ocp.pdr.dto.response.ImportResult;
import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.BacklogOT;
import com.ocp.pdr.model.BesoinEnCours;
import com.ocp.pdr.model.Consommation;
import com.ocp.pdr.model.FicheBOM;
import com.ocp.pdr.model.HistoriqueTraitement;
import com.ocp.pdr.model.ImportDonnees;
import com.ocp.pdr.model.Secteur;
import com.ocp.pdr.model.Stock;
import com.ocp.pdr.model.enums.GroupeHomogene;
import com.ocp.pdr.model.enums.StatutImport;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.ocp.pdr.repository.BacklogOTRepository;
import com.ocp.pdr.repository.BesoinEnCoursRepository;
import com.ocp.pdr.repository.ConsommationRepository;
import com.ocp.pdr.repository.FicheBOMRepository;
import com.ocp.pdr.repository.HistoriqueTraitementRepository;
import com.ocp.pdr.repository.ImportDonneesRepository;
import com.ocp.pdr.repository.SecteurRepository;
import com.ocp.pdr.repository.StockRepository;
import com.ocp.pdr.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelImportService {

    private static final long MAX_IMPORT_FILE_SIZE_BYTES = 500L * 1024 * 1024;

    static {
        IOUtils.setByteArrayMaxOverride((int) MAX_IMPORT_FILE_SIZE_BYTES);
    }

    private final ArticlePDRRepository articlePDRRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StockRepository stockRepository;
    private final BacklogOTRepository backlogOTRepository;
    private final FicheBOMRepository bomRepository;
    private final ConsommationRepository consommationRepository;
    private final BesoinEnCoursRepository besoinRepository;
    private final SecteurRepository secteurRepository;
    private final ImportDonneesRepository importDonneesRepository;
    private final HistoriqueTraitementRepository historiqueTraitementRepository;

    public ExcelValidationResult validateExcelFile(MultipartFile file) {
        return validateExcelFile(file, List.of());
    }

    public ExcelValidationResult validateExcelFile(MultipartFile file, List<String> requiredColumns) {
        if (file == null || file.isEmpty()) {
            return new ExcelValidationResult(false, "Le fichier est vide ou absent.", null, 0L, 0, 0, 0, List.of(), List.of("FICHIER_VIDE"));
        }

        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String normalizedName = originalName.toLowerCase(Locale.ROOT);
        boolean isXls = normalizedName.endsWith(".xls");
        boolean isXlsx = normalizedName.endsWith(".xlsx");
        if (!isXls && !isXlsx) {
            return new ExcelValidationResult(false, "Format de fichier non supporté. Utilisez .xlsx ou .xls.", originalName, file.getSize(), 0, 0, 0, List.of(), List.of("EXTENSION_INVALID"));
        }

        if (file.getSize() > MAX_IMPORT_FILE_SIZE_BYTES) {
            return new ExcelValidationResult(false, "Le fichier Excel est trop volumineux. Taille maximale autorisée : 500 Mo.", originalName, file.getSize(), 0, 0, 0, List.of(), List.of("TAILLE_TROP_IMPORTANTE"));
        }

        if (file.getSize() == 0) {
            return new ExcelValidationResult(false, "Le fichier est vide.", originalName, file.getSize(), 0, 0, 0, List.of(), List.of("FICHIER_VIDE"));
        }

        if (requiredColumns != null && !requiredColumns.isEmpty()) {
            try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
                Sheet firstSheet = workbook.getSheetAt(0);
                if (firstSheet == null || firstSheet.getRow(0) == null) {
                    return new ExcelValidationResult(false, "Le fichier Excel ne contient pas d'en-tête exploitable.", originalName, file.getSize(), 0, 0, 0, List.of(), List.of("ENTETE_ABSENTE"));
                }

                Map<String, Integer> headerMap = getHeaderMap(firstSheet);
                List<String> missing = requiredColumns.stream()
                        .filter(column -> headerMap.keySet().stream().noneMatch(header -> header.equalsIgnoreCase(column.trim()) || header.contains(column.trim().toLowerCase(Locale.ROOT))))
                        .toList();

                if (!missing.isEmpty()) {
                    return new ExcelValidationResult(false, "Colonnes manquantes : " + String.join(", ", missing), originalName, file.getSize(), 0, 0, 0, missing, List.of("COLONNES_MANQUANTES"));
                }
            } catch (Exception e) {
                return new ExcelValidationResult(false, "Le fichier Excel est invalide ou corrompu : " + e.getMessage(), originalName, file.getSize(), 0, 0, 0, List.of(), List.of("FICHIER_INVALIDE"));
            }
        }

        return new ExcelValidationResult(true, "Validation du fichier Excel réussie.", originalName, file.getSize(), 0, 0, 0, List.of(), List.of());
    }

    @Transactional
    public ImportResult importExcelData(MultipartFile file) throws Exception {
        return importExcelData(file, "HISTORICAL", null);
    }

    @Transactional
    public ImportResult importExcelData(MultipartFile file, String datasetType, String periodeImport) throws Exception {
        ExcelValidationResult validation = validateExcelFile(file);
        if (!validation.valid()) {
            throw new IllegalArgumentException(validation.message());
        }

        int stockCount = 0;
        int backlogCount = 0;
        int bomCount = 0;
        int consoCount = 0;
        int articleCount = 0;
        int besoinCount = 0;
        ImportResult.ImportResultBuilder report = new ImportResult.ImportResultBuilder();
        ImportDonnees importDonnees = new ImportDonnees();
        importDonnees.setNomFichier(file.getOriginalFilename());
        importDonnees.setDateImport(java.time.LocalDateTime.now());
        importDonnees.setStatut(StatutImport.PENDING);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            utilisateurRepository.findByEmail(userDetails.getUsername())
                .ifPresent(importDonnees::setUtilisateur);
        }
        importDonnees = importDonneesRepository.save(importDonnees);

        boolean useStreaming = shouldUseStreamingImport(file);
        log.info("Import Excel: {} mode={} taille={} octets", file.getOriginalFilename(), useStreaming ? "streaming" : "legacy", file.getSize());

        try {
            if (useStreaming) {
                consoCount += importExcelStreaming(file, report, datasetType, periodeImport);
            } else {
                try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
                    for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                        Sheet sheet = workbook.getSheetAt(s);
                        if (sheet.getPhysicalNumberOfRows() < 1) continue;

                        Map<String, Integer> colMap = getHeaderMap(sheet);
                        if (colMap.isEmpty()) continue;

                        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
                        String sheetName = sheet.getSheetName().toLowerCase();

                        boolean consommationSheet = containsAnyKey(colMap, "q consommee", "quantite consommee", "consommee", "consommation", "consomm")
                                || sheetName.contains("conso") || originalFilename.contains("conso");

                        if (consommationSheet) {
                            consoCount += importConsommationSheet(sheet, colMap, report, file.getOriginalFilename(), datasetType, periodeImport);
                        } else if (containsAnyKey(colMap, "q installee", "quantite installee", "groupe homogene", "seuil min", "seuil max")
                                || sheetName.contains("bom") || originalFilename.contains("bom")) {
                            int count = importBOMMasterSheet(sheet, colMap);
                            articleCount += count;
                            bomCount += count;
                        } else if (containsAnyKey(colMap, "q stock", "quantite stock", "stock")
                                || sheetName.contains("stock") || originalFilename.contains("stock")) {
                            stockCount += importStockSheet(sheet, colMap, file.getOriginalFilename());
                        } else if (containsAnyKey(colMap, "q non lancee", "quantite non lancee", "non lancee")
                                || sheetName.contains("backlog") || originalFilename.contains("backlog")) {
                            backlogCount += importBacklogSheet(sheet, colMap, file.getOriginalFilename());
                        } else if (containsAnyKey(colMap, "q besoin", "quantite besoin", "besoin en cours", "statut besoin")
                                || sheetName.contains("besoin") || originalFilename.contains("besoin")) {
                            besoinCount += importBesoinSheet(sheet, colMap, report, file.getOriginalFilename());
                        } else if (containsAnyKey(colMap, "code sap", "code_sap")) {
                            if (containsAnyKey(colMap, "stock") || originalFilename.contains("stock")) {
                                stockCount += importStockSheet(sheet, colMap, file.getOriginalFilename());
                            } else if (containsAnyKey(colMap, "lancee") || originalFilename.contains("backlog")) {
                                backlogCount += importBacklogSheet(sheet, colMap, file.getOriginalFilename());
                            } else {
                                stockCount += importStockSheet(sheet, colMap, file.getOriginalFilename());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Erreur durant l'import Excel '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            report.addError("Erreur d'import: " + e.getMessage());
        }

        report.addDetail(articleCount, "fiches articles/BOM");
        report.addDetail(stockCount, "lignes de stock");
        report.addDetail(backlogCount, "ordres backlog OT");
        report.addDetail(consoCount, "lignes de consommation");
        report.addDetail(besoinCount, "besoins en cours");
        ImportResult result = ImportResult.from(report);
        importDonnees.setNombreLignes(result.lignesImportees());
        importDonnees.setStatut(result.success() ? StatutImport.SUCCESS
            : result.lignesImportees() > 0 ? StatutImport.PARTIAL_SUCCESS : StatutImport.ERROR);
        importDonnees.setMessageResultat(result.message() + (result.detailsErreurs().isEmpty()
            ? "" : " Erreurs: " + String.join(" | ", result.detailsErreurs())));
        importDonneesRepository.save(importDonnees);
        HistoriqueTraitement historique = new HistoriqueTraitement();
        historique.setOperation("IMPORT_EXCEL");
        historique.setDateOperation(java.time.LocalDateTime.now());
        historique.setStatut(importDonnees.getStatut().name());
        historique.setMessage(importDonnees.getMessageResultat());
        historique.setImportDonnees(importDonnees);
        historiqueTraitementRepository.save(historique);
        return result;
    }

    private boolean shouldUseStreamingImport(MultipartFile file) {
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        return (originalName.endsWith(".xlsx") || originalName.endsWith(".xls"))
                && file.getSize() > 5 * 1024 * 1024L;
    }

    private int importExcelStreaming(MultipartFile file, ImportResult.ImportResultBuilder report,
                                     String datasetType, String periodeImport) throws Exception {
        final int[] importedCount = new int[1];
        Set<String> seenKeys = new HashSet<>();
        List<Consommation> batch = new ArrayList<>();
        Map<String, ArticlePDR> articleCache = new HashMap<>();

        try (OPCPackage pkg = OPCPackage.open(file.getInputStream())) {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader.SheetIterator iterator = (XSSFReader.SheetIterator) reader.getSheetsData();

            while (iterator.hasNext()) {
                try (InputStream sheetStream = iterator.next()) {
                    String sheetName = iterator.getSheetName();
                    List<String> headers = new ArrayList<>();
                    final Map<Integer, String> rowValues = new HashMap<>();

                    XSSFSheetXMLHandler.SheetContentsHandler handler = new XSSFSheetXMLHandler.SheetContentsHandler() {
                        @Override
                        public void startRow(int rowNum) {
                            rowValues.clear();
                        }

                        @Override
                        public void endRow(int rowNum) {
                            List<String> cells = new ArrayList<>();
                            for (int index = 0; index <= maxColumn(rowValues); index++) {
                                cells.add(rowValues.getOrDefault(index, ""));
                            }
                            if (headers.isEmpty()) {
                                List<String> candidateHeaders = cells.stream()
                                        .map(ExcelImportService.this::normalizeHeader)
                                        .toList();
                                if (candidateHeaders.stream().noneMatch(ExcelImportService.this::isImportHeader)) {
                                    return;
                                }
                                headers.addAll(candidateHeaders);
                                return;
                            }

                            Map<String, String> normalizedRow = new HashMap<>();
                            for (int i = 0; i < cells.size(); i++) {
                                String columnName = i < headers.size() ? headers.get(i) : "";
                                if (!columnName.isBlank()) {
                                    normalizedRow.put(columnName, cells.get(i));
                                }
                            }
                            if (normalizedRow.isEmpty()) {
                                return;
                            }

                                processStreamingRow(sheetName, file.getOriginalFilename(), normalizedRow, rowNum,
                                    report, seenKeys, batch, importedCount, articleCache, datasetType, periodeImport);
                        }

                        @Override
                        public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
                            if (formattedValue == null) {
                                return;
                            }
                            int colIndex = cellRefToIndex(cellReference);
                            rowValues.put(colIndex, formattedValue.trim());
                        }
                    };

                    XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
                    parser.setContentHandler(new XSSFSheetXMLHandler(styles, strings, handler, new DataFormatter(), false));
                    parser.parse(new InputSource(sheetStream));
                }
            }
        }

        if (!batch.isEmpty()) {
            consommationRepository.saveAll(batch);
        }
        report.addDetail(importedCount[0], "lignes Excel traitées");
        return importedCount[0];
    }

    private void processStreamingRow(String sheetName, String sourceFile, Map<String, String> row,
                                     int rowNum, ImportResult.ImportResultBuilder report,
                                     Set<String> seenKeys, List<Consommation> batch,
                                     int[] importedCount, Map<String, ArticlePDR> articleCache,
                                     String datasetType, String periodeImport) {
        try {
            String normalizedSheet = Optional.ofNullable(sheetName).orElse("").toLowerCase(Locale.ROOT);
            String normalizedFile = Optional.ofNullable(sourceFile).orElse("").toLowerCase(Locale.ROOT);
            String codeSap = findValue(row, "code sap", "sap", "code_sap", "code");
            if (codeSap == null || codeSap.isBlank()) {
                return;
            }

            String description = findValue(row, "description", "designation", "libelle");
            String udm = findValue(row, "udm", "unite");
            String rawQuantity = findValue(row, "q consommee", "quantite consommee", "consommee", "consommation", "quantite", "q stock", "quantite stock", "q non lancee", "quantite non lancee", "q besoin", "quantite besoin");
            String rawDate = findValue(row, "date", "date consommation", "date_consommation", "date besoin");
            String rawOt = findValue(row, "numero ot", "n ot", "ot", "numero_ot");
            boolean consommation = isConsommationSheet(normalizedSheet) || normalizedFile.contains("conso") || rowContainsAnyKey(row, "q consommee", "quantite consommee", "consommation");
            boolean stock = normalizedSheet.contains("stock") || normalizedFile.contains("stock") || rowContainsAnyKey(row, "q stock", "quantite stock");
            boolean backlog = normalizedSheet.contains("backlog") || normalizedFile.contains("backlog") || rowContainsAnyKey(row, "q non lancee", "quantite non lancee");
            boolean besoin = normalizedSheet.contains("besoin") || normalizedFile.contains("besoin") || rowContainsAnyKey(row, "q besoin", "quantite besoin");
            boolean bom = normalizedSheet.contains("bom") || normalizedFile.contains("bom") || rowContainsAnyKey(row, "q installee", "quantite installee", "groupe homogene", "seuil min", "seuil max");

            if (consommation) {
                LocalDate date = parseDateFromString(rawDate, rowNum, sheetName, report);
                validateImportPeriod(date, periodeImport);
                double quantity = Math.abs(parseDoubleFromString(rawQuantity, rowNum, sheetName, report));
                String duplicateKey = codeSap.trim() + "|" + date + "|" + rawOt + "|" + quantity;
                if (!seenKeys.add(duplicateKey)) {
                    report.incrementRejectedRows();
                    report.incrementDuplicateRows();
                    report.addError("Feuille " + sheetName + ": ligne " + rowNum + " => doublon détecté");
                    return;
                }
                ArticlePDR article = getOrCreateCachedArticle(articleCache, codeSap, null, description, udm);
                Consommation entity = new Consommation();
                entity.setArticle(article);
                entity.setQuantiteConsommee(quantity);
                entity.setDateConsommation(date);
                entity.setNumeroOT(rawOt == null || rawOt.isBlank() ? "OT-" + rowNum : rawOt.trim());
                entity.setSourceFichier(sourceFile);
                entity.setDateImport(java.time.LocalDateTime.now());
                entity.setDatasetType(datasetType);
                entity.setPeriodeImport(periodeImport);
                batch.add(entity);
                if (batch.size() >= 1000) {
                    consommationRepository.saveAll(batch);
                    batch.clear();
                }
            } else if (bom) {
                ArticlePDR article = getOrCreateCachedArticle(articleCache, codeSap, findValue(row, "code oracle", "oracle"), description, udm);
                Double quantity = parseDoubleOrNull(findValue(row, "q installee", "quantite installee", "installee"));
                if (quantity != null) article.setQuantiteInstallee(quantity);
                article.setCategorie(findValue(row, "categorie", "cat"));
                article.setSeuilMin(parseDoubleOrNull(findValue(row, "seuil min", "minimum")));
                article.setSeuilMax(parseDoubleOrNull(findValue(row, "seuil max", "maximum")));
                articlePDRRepository.save(article);
                FicheBOM entity = new FicheBOM();
                entity.setArticle(article);
                entity.setSecteur(getOrCreateSecteur(Optional.ofNullable(findValue(row, "secteur", "zone", "site")).orElse("General")));
                entity.setReference(findValue(row, "reference", "ref"));
                entity.setQuantiteParEquipement(quantity);
                entity.setDateImport(LocalDate.now());
                bomRepository.save(entity);
            } else if (stock || backlog || besoin) {
                double quantity = Math.abs(parseDoubleFromString(rawQuantity, rowNum, sheetName, report));
                ArticlePDR article = getOrCreateCachedArticle(articleCache, codeSap, null, description, udm);
                if (stock) {
                    Stock entity = new Stock();
                    entity.setArticle(article);
                    entity.setQuantiteStock(quantity);
                    entity.setDateStock(LocalDate.now());
                    entity.setSourceFichier(sourceFile);
                    entity.setDateImport(java.time.LocalDateTime.now());
                    stockRepository.save(entity);
                } else if (backlog) {
                    BacklogOT entity = new BacklogOT();
                    entity.setArticle(article);
                    entity.setNumeroOT(rawOt == null || rawOt.isBlank() ? "OT-" + rowNum : rawOt.trim());
                    entity.setQuantiteNonLancee(quantity);
                    entity.setDateImport(LocalDate.now());
                    entity.setSourceFichier(sourceFile);
                    entity.setDateImportSysteme(java.time.LocalDateTime.now());
                    backlogOTRepository.save(entity);
                } else {
                    BesoinEnCours entity = new BesoinEnCours();
                    entity.setArticle(article);
                    entity.setQuantiteBesoin(quantity);
                    entity.setDateBesoin(rawDate == null ? LocalDate.now() : parseDateFromString(rawDate, rowNum, sheetName, report));
                    entity.setStatut(Optional.ofNullable(findValue(row, "statut", "etat")).orElse("EN_COURS"));
                    entity.setSourceFichier(sourceFile);
                    entity.setDateImport(java.time.LocalDateTime.now());
                    besoinRepository.save(entity);
                }
            } else {
                return;
            }
            report.incrementTotalRows();
            importedCount[0]++;
        } catch (Exception exception) {
            report.incrementRejectedRows();
            report.addError("Feuille " + sheetName + ": ligne " + rowNum + " => " + exception.getMessage());
        }
    }

    private boolean rowContainsAnyKey(Map<String, String> row, String... keys) {
        return row.keySet().stream().anyMatch(column -> {
            for (String key : keys) {
                if (column.contains(normalizeHeader(key))) return true;
            }
            return false;
        });
    }

    private boolean isConsommationSheet(String sheetName) {
        String normalized = Optional.ofNullable(sheetName).orElse("").toLowerCase();
        return normalized.contains("conso") || normalized.contains("consomm") || normalized.contains("consumption");
    }

    private int maxColumn(Map<Integer, String> rowValues) {
        return rowValues.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("_", " ")
                .replace(".", " ")
                .replaceAll("\\s+", " ");
    }

    private boolean isImportHeader(String header) {
        return header.contains("code sap")
                || header.equals("sap")
                || header.contains("description")
                || header.contains("designation")
                || header.contains("date")
                || header.contains("quantite")
                || header.contains("consomm")
                || header.equals("ot")
                || header.contains("numero ot");
    }

    private String findValue(Map<String, String> row, String... keys) {
        for (String key : keys) {
            String value = row.get(normalizeHeader(key));
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private int cellRefToIndex(String cellRef) {
        if (cellRef == null || cellRef.isBlank()) {
            return 0;
        }
        String letters = cellRef.replaceAll("[0-9]", "");
        int value = 0;
        for (int i = 0; i < letters.length(); i++) {
            value = value * 26 + (letters.charAt(i) - 'A' + 1);
        }
        return value - 1;
    }

    private LocalDate parseDateFromString(String rawDate, int rowNum, String sheetName, ImportResult.ImportResultBuilder report) {
        if (rawDate == null || rawDate.isBlank()) {
            throw new IllegalArgumentException("date manquante");
        }
        String cleaned = rawDate.trim();
        for (String pattern : List.of("yyyy-MM-dd", "dd/MM/yyyy", "dd.MM.yyyy", "yyyy/MM/dd")) {
            try {
                return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
                // continue
            }
        }
        throw new IllegalArgumentException("date invalide: " + cleaned);
    }

    private double parseDoubleFromString(String rawValue, int rowNum, String sheetName, ImportResult.ImportResultBuilder report) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("valeur numérique absente");
        }
        String normalized = rawValue.trim().replace(" ", "").replace("\u00A0", "").replace("'", "");
        normalized = normalized.replace(".", "").replace(",", ".");
        if (normalized.contains("(") && normalized.contains(")")) {
            normalized = "-" + normalized.replace("(", "").replace(")", "");
        }
        return Double.parseDouble(normalized);
    }

    private Double parseDoubleOrNull(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            String normalized = rawValue.trim().replace(" ", "").replace("\u00A0", "").replace("'", "");
            normalized = normalized.replace(".", "").replace(",", ".");
            if (normalized.contains("(") && normalized.contains(")")) {
                normalized = "-" + normalized.replace("(", "").replace(")", "");
            }
            return Double.parseDouble(normalized);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Integer> getHeaderMap(Sheet sheet) {
        Map<String, Integer> headerMap = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) return headerMap;

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String val = extractStringValue(cell).trim().toLowerCase()
                        .replace("_", " ")
                        .replace(".", " ")
                        .replaceAll("\\s+", " ");
                if (!val.isEmpty()) {
                    headerMap.put(val, i);
                }
            }
        }
        return headerMap;
    }

    private boolean containsAnyKey(Map<String, Integer> map, String... keys) {
        for (String k : keys) {
            for (String mapKey : map.keySet()) {
                if (mapKey.contains(k)) return true;
            }
        }
        return false;
    }

    private Integer getColumnIndex(Map<String, Integer> map, String... possibleNames) {
        for (String name : possibleNames) {
            String norm = name.toLowerCase().trim();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getKey().equals(norm) || entry.getKey().contains(norm)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private ArticlePDR getOrCreateCachedArticle(Map<String, ArticlePDR> articleCache,
                                                String codeSAP, String codeOracle,
                                                String description, String udm) {
        String cleanCode = codeSAP == null ? "" : codeSAP.trim();
        if (cleanCode.isEmpty()) {
            return null;
        }
        return articleCache.computeIfAbsent(cleanCode,
                key -> getOrCreateArticle(key, codeOracle, description, udm));
    }

    private ArticlePDR getOrCreateArticle(String codeSAP, String codeOracle, String description, String udm) {
        if (codeSAP == null || codeSAP.trim().isEmpty()) {
            return null;
        }
        String cleanCode = codeSAP.trim();
        return articlePDRRepository.findByCodeSAP(cleanCode)
                .map(art -> {
                    boolean modified = false;
                    if (codeOracle != null && !codeOracle.isEmpty() && art.getCodeOracle() == null) {
                        art.setCodeOracle(codeOracle);
                        modified = true;
                    }
                    if (description != null && !description.isEmpty() && (art.getDescription() == null || art.getDescription().startsWith("Article "))) {
                        art.setDescription(description);
                        modified = true;
                    }
                    if (udm != null && !udm.isEmpty() && art.getUdm() == null) {
                        art.setUdm(udm);
                        modified = true;
                    }
                    return modified ? articlePDRRepository.save(art) : art;
                })
                .orElseGet(() -> {
                    ArticlePDR newArticle = new ArticlePDR();
                    newArticle.setCodeSAP(cleanCode);
                    newArticle.setCodeOracle(codeOracle);
                    newArticle.setDescription(description != null && !description.isEmpty() ? description : "Article " + cleanCode);
                    newArticle.setUdm(udm);
                    return articlePDRRepository.save(newArticle);
                });
    }

    private Secteur getOrCreateSecteur(String secteurLabel) {
        if (secteurLabel == null || secteurLabel.trim().isEmpty()) {
            return secteurRepository.findByNom("General")
                    .orElseGet(() -> secteurRepository.save(new Secteur(null, "General", "GEN", "Secteur par défaut", null)));
        }

        String normalized = secteurLabel.trim();
        return secteurRepository.findByNom(normalized)
                .orElseGet(() -> secteurRepository.save(new Secteur(null, normalized, normalized.substring(0, Math.min(3, normalized.length())).toUpperCase(), "Secteur importé", null)));
    }

    private GroupeHomogene parseGroupeHomogene(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String val = value.trim().toUpperCase();
        if (val.contains("CRITIQUE")) return GroupeHomogene.CRITIQUE;
        if (val.contains("CONDITION")) return GroupeHomogene.CONDITIONNEL;
        if (val.contains("CURATIF")) return GroupeHomogene.CURATIF;
        return null;
    }

    // 1. Importation BOM & Fiches Articles complètes
    private int importBOMMasterSheet(Sheet sheet, Map<String, Integer> colMap) {
        int count = 0;
        Integer colCodeSAP = getColumnIndex(colMap, "code sap", "codesap", "sap");
        Integer colCodeOracle = getColumnIndex(colMap, "code oracle", "oracle");
        Integer colDesc = getColumnIndex(colMap, "description", "designation");
        Integer colRef = getColumnIndex(colMap, "reference", "ref");
        Integer colUdm = getColumnIndex(colMap, "udm", "unite");
        Integer colQInstallee = getColumnIndex(colMap, "q installee", "quantite installee", "installee");
        Integer colCategorie = getColumnIndex(colMap, "categorie", "cat");
        Integer colGroupe = getColumnIndex(colMap, "groupe homogene", "groupe", "homogene");
        Integer colSeuilMin = getColumnIndex(colMap, "seuil min", "min");
        Integer colSeuilMax = getColumnIndex(colMap, "seuil max", "max");
        Integer colSecteur = getColumnIndex(colMap, "secteur", "zone", "site");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String codeSAP = colCodeSAP != null ? extractStringValue(row.getCell(colCodeSAP)) : "";
            if (codeSAP.isEmpty()) continue;

            String codeOracle = colCodeOracle != null ? extractStringValue(row.getCell(colCodeOracle)) : "";
            String description = colDesc != null ? extractStringValue(row.getCell(colDesc)) : "";
            String reference = colRef != null ? extractStringValue(row.getCell(colRef)) : "";
            String udm = colUdm != null ? extractStringValue(row.getCell(colUdm)) : "";
            Double qInstallee = colQInstallee != null ? extractNumericValue(row.getCell(colQInstallee)) : null;
            String categorie = colCategorie != null ? extractStringValue(row.getCell(colCategorie)) : "";
            String groupeStr = colGroupe != null ? extractStringValue(row.getCell(colGroupe)) : "";
            Double seuilMin = colSeuilMin != null ? extractNumericValue(row.getCell(colSeuilMin)) : null;
            Double seuilMax = colSeuilMax != null ? extractNumericValue(row.getCell(colSeuilMax)) : null;
            String secteurLabel = colSecteur != null ? extractStringValue(row.getCell(colSecteur)) : "General";

            ArticlePDR article = articlePDRRepository.findByCodeSAP(codeSAP).orElse(new ArticlePDR());
            article.setCodeSAP(codeSAP);
            if (!codeOracle.isEmpty()) article.setCodeOracle(codeOracle);
            if (!description.isEmpty()) article.setDescription(description);
            if (!reference.isEmpty()) article.setReference(reference);
            if (!udm.isEmpty()) article.setUdm(udm);
            if (qInstallee != null) article.setQuantiteInstallee(qInstallee);
            if (!categorie.isEmpty()) article.setCategorie(categorie);
            if (!groupeStr.isEmpty()) {
                GroupeHomogene parsed = parseGroupeHomogene(groupeStr);
                article.setGroupeHomogene(parsed);
            }
            if (seuilMin != null && seuilMin > 0) article.setSeuilMin(seuilMin);
            if (seuilMax != null && seuilMax > 0) article.setSeuilMax(seuilMax);

            ArticlePDR savedArticle = articlePDRRepository.save(article);

            Secteur secteur = getOrCreateSecteur(secteurLabel);
            FicheBOM bom = new FicheBOM();
            bom.setArticle(savedArticle);
            bom.setSecteur(secteur);
            bom.setReference(reference);
            bom.setQuantiteParEquipement(qInstallee);
            bom.setDateImport(LocalDate.now());
            bomRepository.save(bom);

            count++;
        }
        return count;
    }

    // 2. Importation Stock
    private int importStockSheet(Sheet sheet, Map<String, Integer> colMap, String sourceFile) {
        int count = 0;
        Integer colCodeSAP = getColumnIndex(colMap, "code sap", "sap");
        Integer colCodeOracle = getColumnIndex(colMap, "code oracle", "oracle");
        Integer colDesc = getColumnIndex(colMap, "description", "designation");
        Integer colUdm = getColumnIndex(colMap, "udm");
        Integer colQStock = getColumnIndex(colMap, "q stock", "quantite stock", "stock", "quantite");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String codeSAP = colCodeSAP != null ? extractStringValue(row.getCell(colCodeSAP)) : extractStringValue(row.getCell(0));
            if (codeSAP.isEmpty()) continue;

            String codeOracle = colCodeOracle != null ? extractStringValue(row.getCell(colCodeOracle)) : "";
            String description = colDesc != null ? extractStringValue(row.getCell(colDesc)) : "";
            String udm = colUdm != null ? extractStringValue(row.getCell(colUdm)) : "";
            double quantite = colQStock != null ? extractNumericValue(row.getCell(colQStock)) : extractNumericValue(row.getCell(row.getLastCellNum() - 1));

            ArticlePDR article = getOrCreateArticle(codeSAP, codeOracle, description, udm);
            if (article != null) {
                Stock stock = new Stock();
                stock.setArticle(article);
                stock.setQuantiteStock(quantite);
                stock.setDateStock(LocalDate.now());
                stock.setSourceFichier(sourceFile);
                stock.setDateImport(java.time.LocalDateTime.now());
                stockRepository.save(stock);
                count++;
            }
        }
        return count;
    }

    // 3. Importation Backlog
    private int importBacklogSheet(Sheet sheet, Map<String, Integer> colMap, String sourceFile) {
        int count = 0;
        Integer colCodeSAP = getColumnIndex(colMap, "code sap", "sap");
        Integer colDesc = getColumnIndex(colMap, "description", "designation");
        Integer colUdm = getColumnIndex(colMap, "udm");
        Integer colQNonLancee = getColumnIndex(colMap, "q non lancee", "non lancee", "quantite");
        Integer colOT = getColumnIndex(colMap, "ot", "numero ot", "ordre");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String codeSAP = colCodeSAP != null ? extractStringValue(row.getCell(colCodeSAP)) : extractStringValue(row.getCell(0));
            if (codeSAP.isEmpty()) continue;

            String description = colDesc != null ? extractStringValue(row.getCell(colDesc)) : "";
            String udm = colUdm != null ? extractStringValue(row.getCell(colUdm)) : "";
            double quantite = colQNonLancee != null ? extractNumericValue(row.getCell(colQNonLancee)) : 0.0;
            String numeroOT = colOT != null ? extractStringValue(row.getCell(colOT)) : "OT-" + System.currentTimeMillis();

            ArticlePDR article = getOrCreateArticle(codeSAP, null, description, udm);
            if (article != null) {
                BacklogOT backlog = new BacklogOT();
                backlog.setArticle(article);
                backlog.setNumeroOT(numeroOT);
                backlog.setQuantiteNonLancee(quantite);
                backlog.setDateImport(LocalDate.now());
                backlog.setSourceFichier(sourceFile);
                backlog.setDateImportSysteme(java.time.LocalDateTime.now());
                backlogOTRepository.save(backlog);
                count++;
            }
        }
        return count;
    }

    // 4. Importation Consommation
    private int importConsommationSheet(Sheet sheet, Map<String, Integer> colMap,
                                        ImportResult.ImportResultBuilder report, String sourceFile,
                                        String datasetType, String periodeImport) {
        int count = 0;
        Integer colCodeSAP = getColumnIndex(colMap, "code sap", "sap");
        Integer colDesc = getColumnIndex(colMap, "description", "designation");
        Integer colQConso = getColumnIndex(colMap, "q consommee", "consommee", "consomm", "quantite");
        Integer colQInstallee = getColumnIndex(colMap, "q installee", "quantite installee", "installee");
        Integer colUdm = getColumnIndex(colMap, "udm");
        Integer colOT = getColumnIndex(colMap, "n ot", "ot", "numero ot");
        Integer colDate = getColumnIndex(colMap, "date", "date consommation");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String codeSAP = colCodeSAP != null ? extractStringValue(row.getCell(colCodeSAP)) : extractStringValue(row.getCell(0));
                if (codeSAP.isEmpty()) continue;
                String description = colDesc != null ? extractStringValue(row.getCell(colDesc)) : "";
                String udm = colUdm != null ? extractStringValue(row.getCell(colUdm)) : "";
                double quantite = colQConso != null
                    ? Math.abs(extractNumericValue(row.getCell(colQConso)))
                    : 0.0;
                Double quantiteInstallee = colQInstallee != null ? extractNumericValue(row.getCell(colQInstallee)) : null;
                String numeroOT = colOT != null ? extractStringValue(row.getCell(colOT)) : "OT-" + System.currentTimeMillis();
                LocalDate dateConso = colDate != null ? extractDateValue(row.getCell(colDate)) : LocalDate.now();
                validateImportPeriod(dateConso, periodeImport);
                ArticlePDR article = getOrCreateArticle(codeSAP, null, description, udm);
                if (article != null) {
                    if (quantiteInstallee != null) {
                        article.setQuantiteInstallee(quantiteInstallee);
                        articlePDRRepository.save(article);
                    }
                    Consommation conso = new Consommation();
                    conso.setArticle(article);
                    conso.setQuantiteConsommee(quantite);
                    conso.setNumeroOT(numeroOT);
                    conso.setDateConsommation(dateConso);
                    conso.setSourceFichier(sourceFile);
                    conso.setDateImport(java.time.LocalDateTime.now());
                    conso.setDatasetType(datasetType);
                    conso.setPeriodeImport(periodeImport);
                    consommationRepository.save(conso);
                    count++;
                }
            } catch (IllegalArgumentException exception) {
                report.addError("Feuille " + sheet.getSheetName() + ", ligne " + (i + 1) + ": " + exception.getMessage());
            }
        }
        return count;
    }

    private void validateImportPeriod(LocalDate date, String periodeImport) {
        if (periodeImport == null || periodeImport.isBlank()) {
            return;
        }
        YearMonth expected = YearMonth.parse(periodeImport);
        if (!YearMonth.from(date).equals(expected)) {
            throw new IllegalArgumentException("la date " + date + " ne correspond pas au mois " + periodeImport);
        }
    }

    private int importBesoinSheet(Sheet sheet, Map<String, Integer> colMap,
                                  ImportResult.ImportResultBuilder report, String sourceFile) {
        int count = 0;
        Integer colCodeSAP = getColumnIndex(colMap, "code sap", "sap");
        Integer colDesc = getColumnIndex(colMap, "description", "designation");
        Integer colQBesoin = getColumnIndex(colMap, "q besoin", "quantite besoin", "besoin", "quantite");
        Integer colDate = getColumnIndex(colMap, "date besoin", "date");
        Integer colStatut = getColumnIndex(colMap, "statut", "etat");
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            try {
                String codeSAP = colCodeSAP != null ? extractStringValue(row.getCell(colCodeSAP)) : extractStringValue(row.getCell(0));
                if (codeSAP.isEmpty()) continue;
                String description = colDesc != null ? extractStringValue(row.getCell(colDesc)) : "";
                double quantite = extractNumericValue(row.getCell(colQBesoin));
                LocalDate dateBesoin = colDate != null ? extractDateValue(row.getCell(colDate)) : LocalDate.now();
                String statut = colStatut != null ? extractStringValue(row.getCell(colStatut)) : "EN_COURS";
                ArticlePDR article = getOrCreateArticle(codeSAP, null, description, null);
                if (article != null) {
                    BesoinEnCours besoin = new BesoinEnCours();
                    besoin.setArticle(article);
                    besoin.setQuantiteBesoin(quantite);
                    besoin.setDateBesoin(dateBesoin);
                    besoin.setStatut(statut);
                    besoin.setSourceFichier(sourceFile);
                    besoin.setDateImport(java.time.LocalDateTime.now());
                    besoinRepository.save(besoin);
                    count++;
                }
            } catch (IllegalArgumentException exception) {
                report.addError("Feuille " + sheet.getSheetName() + ", ligne " + (i + 1) + ": " + exception.getMessage());
            }
        }
        return count;
    }

    private String extractStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        return String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    private double extractNumericValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return 0.0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    String str = cell.getStringCellValue().trim()
                            .replace(" ", "")
                            .replace("\u00A0", "")
                            .replace("'", "");
                    if (str.isEmpty() || str.equals("-") || str.equals("—")) return 0.0;
                    if (str.contains(",")) {
                        str = str.replace(".", "").replace(",", ".");
                    } else if (str.indexOf('.') != str.lastIndexOf('.')) {
                        str = str.replace(".", "");
                    }
                    return Double.parseDouble(str);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("valeur numérique invalide: " + cell.getStringCellValue());
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    throw new IllegalArgumentException("formule numérique invalide");
                }
            default:
                throw new IllegalArgumentException("valeur numérique invalide");
        }
    }

    private LocalDate extractDateValue(Cell cell) {
        if (cell == null) throw new IllegalArgumentException("date manquante");
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            try {
                if (val.contains(".")) {
                    return LocalDate.parse(val, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                } else if (val.contains("/")) {
                    return LocalDate.parse(val, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else if (val.contains("-")) {
                    return LocalDate.parse(val, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception exception) {
                throw new IllegalArgumentException("date invalide: " + val);
            }
        }
        throw new IllegalArgumentException("date invalide");
    }
}


