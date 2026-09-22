package com.ocp.pdr.controller;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ocp.pdr.dto.response.ExcelValidationResult;
import com.ocp.pdr.dto.response.ImportResult;
import com.ocp.pdr.service.ExcelExportService;
import com.ocp.pdr.service.ExcelImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping({"/api/excel", "/api/import-export"})
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ImportExportController {

    private final ExcelImportService excelImportService;
    private final ExcelExportService excelExportService;

    /**
     * Importe plusieurs fichiers de référentiels PDR (Stock, Consommations, BOM, Backlog)
     */
    @PostMapping(value = {"/import-multi", "/import/multi"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importMultiExcel(
            @RequestParam(value = "stockFile", required = false) MultipartFile stockFile,
            @RequestParam(value = "consommationFile", required = false) MultipartFile consommationFile,
            @RequestParam(value = "bomFile", required = false) MultipartFile bomFile,
            @RequestParam(value = "backlogFile", required = false) MultipartFile backlogFile,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        List<MultipartFile> filesToProcess = new ArrayList<>();
        // Priority order: BOM first (articles registration), then Stock, Consommation, Backlog
        if (bomFile != null && !bomFile.isEmpty()) filesToProcess.add(bomFile);
        if (stockFile != null && !stockFile.isEmpty()) filesToProcess.add(stockFile);
        if (consommationFile != null && !consommationFile.isEmpty()) filesToProcess.add(consommationFile);
        if (backlogFile != null && !backlogFile.isEmpty()) filesToProcess.add(backlogFile);
        if (file != null && !file.isEmpty()) filesToProcess.add(file);

        if (filesToProcess.isEmpty()) {
            response.put("success", false);
            response.put("message", "Aucun fichier valide fourni pour l'importation");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }

        int totalLignes = 0;
        int totalErreurs = 0;
        List<String> detailsErreurs = new ArrayList<>();
        Map<String, Object> fileResults = new HashMap<>();

        for (MultipartFile f : filesToProcess) {
            try {
                log.info("Import multi-fichier en cours: {}", f.getOriginalFilename());
                ImportResult res = excelImportService.importExcelData(f);
                totalLignes += res.lignesImportees();
                totalErreurs += res.erreurs();
                if (res.detailsErreurs() != null) {
                    detailsErreurs.addAll(res.detailsErreurs());
                }
                fileResults.put(f.getOriginalFilename(), Map.of(
                        "success", res.success(),
                        "lignesImportees", res.lignesImportees(),
                        "message", res.message()
                ));
            } catch (Exception e) {
                log.error("Erreur lors de l'import du fichier {}: {}", f.getOriginalFilename(), e.getMessage(), e);
                totalErreurs++;
                detailsErreurs.add(f.getOriginalFilename() + ": " + e.getMessage());
                fileResults.put(f.getOriginalFilename(), Map.of(
                        "success", false,
                        "error", e.getMessage()
                ));
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        boolean globalSuccess = totalErreurs == 0;
        String message = String.format("Données PDR importées et synchronisées : %d ligne(s) traitée(s) sur %d fichier(s) en %dms.",
                totalLignes, filesToProcess.size(), duration);

        response.put("success", globalSuccess || totalLignes > 0);
        response.put("message", message);
        response.put("lignesImportees", totalLignes);
        response.put("erreurs", totalErreurs);
        response.put("detailsErreurs", detailsErreurs);
        response.put("fileResults", fileResults);
        response.put("duration", duration);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    /**
     * Importe les données depuis un fichier Excel
     */
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelValidationResult> validateExcel(@RequestParam("file") MultipartFile file) {
        ExcelValidationResult validation = excelImportService.validateExcelFile(file);
        return validation.valid()
                ? ResponseEntity.ok(validation)
                : ResponseEntity.badRequest().body(validation);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                log.warn("Tentative d'import avec fichier vide");
                response.put("success", false);
                response.put("message", "Le fichier est vide");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("Import de fichier en cours: {}", file.getOriginalFilename());
            long startTime = System.currentTimeMillis();
            
            ImportResult importResult = excelImportService.importExcelData(file);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Import terminé avec succès en {}ms", duration);
            
            response.put("success", importResult.success());
            response.put("message", importResult.message());
            response.put("lignesImportees", importResult.lignesImportees());
            response.put("erreurs", importResult.erreurs());
            response.put("detailsErreurs", importResult.detailsErreurs());
            response.put("fileName", file.getOriginalFilename());
            response.put("duration", duration);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'import du fichier: {}", file.getOriginalFilename(), e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'import: " + e.getMessage());
            response.put("fileName", file.getOriginalFilename());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Exporte les résultats d'approvisionnement en Excel
     */
    @GetMapping({"/export", "/export/resultats", "/export-resultats"})
    public ResponseEntity<InputStreamResource> exportExcel() {
        try {
            log.info("Export des résultats d'approvisionnement");
            
            ByteArrayInputStream in = excelExportService.exportResultats();
            
            HttpHeaders headers = new HttpHeaders();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            headers.add("Content-Disposition", "attachment; filename=resultats_approvisionnement_" + timestamp + ".xlsx");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            log.error("Erreur lors de l'export des résultats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporte les règles Min & Max en Excel
     */
    @GetMapping({"/export-min-max", "/export/min-max"})
    public ResponseEntity<InputStreamResource> exportMinMax() {
        try {
            log.info("Export de la règle Min & Max");
            ByteArrayInputStream in = excelExportService.exportMinMax();

            HttpHeaders headers = new HttpHeaders();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            headers.add("Content-Disposition", "attachment; filename=regle_min_max_" + timestamp + ".xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            log.error("Erreur lors de l'export de la règle Min & Max", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping({"/export-planifie", "/export/planifie"})
    public ResponseEntity<InputStreamResource> exportPlanifie() {
        try {
            log.info("Export de la règle Mode Planifié");
            ByteArrayInputStream in = excelExportService.exportPlanifie();

            HttpHeaders headers = new HttpHeaders();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            headers.add("Content-Disposition", "attachment; filename=regle_mode_planifie_" + timestamp + ".xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            log.error("Erreur lors de l'export de la règle Mode Planifié", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping({"/export-sur-demande", "/export/sur-demande"})
    public ResponseEntity<InputStreamResource> exportSurDemande() {
        try {
            log.info("Export de la règle Sur Demande");
            ByteArrayInputStream in = excelExportService.exportSurDemande();

            HttpHeaders headers = new HttpHeaders();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            headers.add("Content-Disposition", "attachment; filename=regle_sur_demande_" + timestamp + ".xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            log.error("Erreur lors de l'export de la règle Sur Demande", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping({"/export-anomalies", "/export/anomalies"})
    public ResponseEntity<InputStreamResource> exportAnomalies() {
        try {
            log.info("Export des anomalies de consommation");
            
            ByteArrayInputStream in = excelExportService.exportAnomalies();
            
            HttpHeaders headers = new HttpHeaders();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            headers.add("Content-Disposition", "attachment; filename=anomalies_" + timestamp + ".xlsx");
            
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            log.error("Erreur lors de l'export des anomalies", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

