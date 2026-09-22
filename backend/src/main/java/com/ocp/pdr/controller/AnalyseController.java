package com.ocp.pdr.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ocp.pdr.dto.response.AnalysisSummaryResponse;
import com.ocp.pdr.dto.response.ApiSuccessResponse;
import com.ocp.pdr.dto.response.PredictionIaResponse;
import com.ocp.pdr.model.AnomalieConsommation;
import com.ocp.pdr.model.ResultatApprovisionnement;
import com.ocp.pdr.service.AnalyseConsommationService;
import com.ocp.pdr.service.IaPredictionService;
import com.ocp.pdr.service.MoteurApprovisionnementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/analyse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AnalyseController {

    private final MoteurApprovisionnementService moteurApprovisionnementService;
    private final AnalyseConsommationService analyseConsommationService;
    private final IaPredictionService iaPredictionService;

    @PostMapping({"/executer", "/lancer", "/calculer"})
    public ResponseEntity<ApiSuccessResponse> executerAnalyse() {
        try {
            log.info("Requête d'exécution d'analyse globale reçue");
            List<ResultatApprovisionnement> resultats = moteurApprovisionnementService.executerAnalyseGlobale();

            ApiSuccessResponse response = ApiSuccessResponse.builder()
                    .success(true)
                    .message("Analyse exécutée avec succès")
                    .resultCount(resultats.size())
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution de l'analyse", e);
            ApiSuccessResponse error = ApiSuccessResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/resultats")
    public ResponseEntity<List<ResultatApprovisionnement>> getResultats(
            @RequestParam(value = "mode", required = false) String mode,
            @RequestParam(value = "search", required = false) String search
    ) {
        List<ResultatApprovisionnement> all = moteurApprovisionnementService.obtenirTousLesResultats();
        if (mode != null && !mode.isBlank() && !"ALL".equalsIgnoreCase(mode)) {
            all = all.stream()
                    .filter(r -> r.getMode() != null && r.getMode().name().equalsIgnoreCase(mode))
                    .toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            all = all.stream()
                    .filter(r -> (r.getCodeSAP() != null && r.getCodeSAP().toLowerCase().contains(q))
                            || (r.getDesignation() != null && r.getDesignation().toLowerCase().contains(q)))
                    .toList();
        }
        return ResponseEntity.ok(all);
    }

    @GetMapping("/anomalies")
    public ResponseEntity<List<AnomalieConsommation>> getAnomalies() {
        return ResponseEntity.ok(analyseConsommationService.obtenirToutesAnomalies());
    }

    @GetMapping("/predictions-ia")
    public ResponseEntity<PredictionIaResponse> getPredictionsIa() {
        log.info("Requête de récupération des prédictions de consommation IA");
        return ResponseEntity.ok(iaPredictionService.genererPredictions());
    }

    @PostMapping("/predictions-ia/calculer")
    public ResponseEntity<PredictionIaResponse> calculerPredictionsIa() {
        log.info("Requête de recalcul des prédictions de consommation IA");
        return ResponseEntity.ok(iaPredictionService.genererPredictions());
    }

    @PostMapping("/ml/train")
    public ResponseEntity<PredictionIaResponse> entrainerModeleMl() {
        log.info("Requête d'entraînement du modèle Random Forest");
        return ResponseEntity.ok(iaPredictionService.entrainerModele());
    }

    @PostMapping("/ml/predict")
    public ResponseEntity<PredictionIaResponse> predireAvecModeleMl() {
        log.info("Requête de prédiction avec le modèle Random Forest");
        return ResponseEntity.ok(iaPredictionService.genererPredictions());
    }

    @GetMapping("/ml/metrics")
    public ResponseEntity<PredictionIaResponse> obtenirMetriquesMl() {
        return ResponseEntity.ok(iaPredictionService.genererPredictions());
    }

    @GetMapping("/ml/status")
    public ResponseEntity<Map<String, Object>> obtenirStatutMl() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "RANDOM_FOREST_PYTHON");
        status.put("available", true);
        status.put("message", "Le modèle est entraîné à la demande lors de l'analyse IA et réutilisé si les données n'ont pas changé.");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/etat")
    public ResponseEntity<Map<String, Object>> getEtatAnalyse() {
        Map<String, Object> etat = new HashMap<>();
        etat.put("analyseLancee", moteurApprovisionnementService.isAnalyseLancee());
        return ResponseEntity.ok(etat);
    }

    @GetMapping("/summary")
    public ResponseEntity<AnalysisSummaryResponse> getSummary() {
        Map<String, Object> summary = moteurApprovisionnementService.obtenirSummaryAnalyseDto();

        AnalysisSummaryResponse response = AnalysisSummaryResponse.builder()
                .totalArticles(((Number) summary.getOrDefault("totalArticles", 0)).longValue())
                .minMaxCount(((Number) summary.getOrDefault("minMaxCount", 0)).longValue())
                .planifieCount(((Number) summary.getOrDefault("planifieCount", 0)).longValue())
                .surDemandeCount(((Number) summary.getOrDefault("surDemandeCount", 0)).longValue())
                .anomaliesCount(((Number) summary.getOrDefault("anomaliesCount", 0)).longValue())
                .articlesEnStock(((Number) summary.getOrDefault("articlesEnStock", 0)).longValue())
                .minMaxRate(((Number) summary.getOrDefault("minMaxRate", 0)).doubleValue())
                .planifieRate(((Number) summary.getOrDefault("planifieRate", 0)).doubleValue())
                .surDemandeRate(((Number) summary.getOrDefault("surDemandeRate", 0)).doubleValue())
                .stockRate(((Number) summary.getOrDefault("stockRate", 0)).doubleValue())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(moteurApprovisionnementService.obtenirStatistiques());
    }
}

