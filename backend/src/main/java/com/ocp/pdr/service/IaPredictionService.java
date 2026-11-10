package com.ocp.pdr.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocp.pdr.dto.response.AnomalieConsommationIaDto;
import com.ocp.pdr.dto.response.DetectionAnomaliesIaResponse;
import com.ocp.pdr.dto.response.PredictionIaResponse;
import com.ocp.pdr.dto.response.PredictionIaResultDto;
import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.Consommation;
import com.ocp.pdr.repository.ArticlePDRRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IaPredictionService {

    private final ArticlePDRRepository articlePDRRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public PredictionIaResponse genererPredictions() {
        List<ArticlePDR> articles = articlePDRRepository.findAllForAnalysis();
        if (articles == null || articles.isEmpty()) {
            return PredictionIaResponse.builder()
                    .success(true)
                    .totalArticles(0)
                    .nbCritique(0)
                    .nbAlertes(0)
                    .nbRisqueMoyen(0)
                    .nbNormal(0)
                    .nbInsuffisant(0)
                    .predictions(Collections.emptyList())
                    .message("Aucun article trouvé dans le référentiel.")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        Map<String, Object> requestPayload = construirePayload(articles, false);

        // Appel au microservice Python avec fallback local
        List<PredictionIaResultDto> predictions;
        Map<String, Object> evaluationMetrics = null;

        try {
            JsonNode root = appelerServicePythonJson(requestPayload);
            JsonNode predNodes = root.get("predictions");
            predictions = objectMapper.convertValue(predNodes, new TypeReference<List<PredictionIaResultDto>>() {});

            if (root.has("evaluation") && !root.get("evaluation").isNull()) {
                evaluationMetrics = objectMapper.convertValue(root.get("evaluation"), new TypeReference<Map<String, Object>>() {});
            }

            log.info("Prédictions IA obtenues avec succès depuis le microservice Python Random Forest ({} articles traités)", predictions.size());
        } catch (Exception e) {
            log.error("Microservice Python IA indisponible ({}) : {}. Le fallback Java a été désactivé.", aiServiceUrl, e.getMessage());
            throw new IllegalStateException("Service IA Python indisponible. Le moteur de secours Java est désactivé.", e);
        }

        // Calcul des métriques de synthèse
        int nbCritique = 0;
        int nbAlertes = 0;
        int nbRisqueMoyen = 0;
        int nbNormal = 0;
        int nbInsuffisant = 0;

        for (PredictionIaResultDto p : predictions) {
            String niv = p.getNiveauRisque() != null ? p.getNiveauRisque().toUpperCase() : "";
            if ("CRITIQUE".equals(niv)) {
                nbCritique++;
                nbAlertes++;
            } else if ("ELEVE".equals(niv) || "TRES_ELEVE".equals(niv) || Boolean.TRUE.equals(p.getAlerte())) {
                nbAlertes++;
            } else if ("MOYEN".equals(niv)) {
                nbRisqueMoyen++;
            } else if ("FAIBLE".equals(niv)) {
                nbNormal++;
            } else {
                nbInsuffisant++;
            }
        }

        return PredictionIaResponse.builder()
                .success(true)
                .totalArticles(predictions.size())
                .nbCritique(nbCritique)
                .nbAlertes(nbAlertes)
                .nbRisqueMoyen(nbRisqueMoyen)
                .nbNormal(nbNormal)
                .nbInsuffisant(nbInsuffisant)
                .predictions(predictions)
                .evaluation(evaluationMetrics)
                .message("Analyse prédictive IA exécutée avec succès.")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public DetectionAnomaliesIaResponse detecterConsommationsInhabituelles() {
        List<ArticlePDR> articles = articlePDRRepository.findAllForAnalysis();
        if (articles == null || articles.isEmpty()) {
            return DetectionAnomaliesIaResponse.builder()
                    .success(true)
                    .totalArticles(0)
                    .nbAnomalies(0)
                    .nbCritique(0)
                    .nbInhabituelle(0)
                    .nbNormal(0)
                    .nbNonEvaluable(0)
                    .results(Collections.emptyList())
                    .message("Aucun article trouvé dans le référentiel.")
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        Map<String, Object> requestPayload = construirePayload(articles, false);
        List<AnomalieConsommationIaDto> results;

        try {
            JsonNode root = appelerServicePython(requestPayload, "/detect-anomalies");
            JsonNode resultNodes = root.get("results");
            if (resultNodes != null && resultNodes.isArray()) {
                results = objectMapper.convertValue(resultNodes, new TypeReference<List<AnomalieConsommationIaDto>>() {});
            } else {
                results = Collections.emptyList();
            }
            log.info("Détection des consommations inhabituelles réussie via FastAPI ({} articles traités)", results.size());
        } catch (Exception e) {
            log.warn("Microservice Python IA non disponible pour /detect-anomalies : {}. Application du repli statistique Java.", e.getMessage());
            results = executerDetectionAnomaliesLocale(articles);
        }

        int nbCritique = 0;
        int nbInhabituelle = 0;
        int nbNormal = 0;
        int nbNonEvaluable = 0;

        for (AnomalieConsommationIaDto item : results) {
            String niv = item.getNiveau() != null ? item.getNiveau().toUpperCase() : "";
            switch (niv) {
                case "CRITIQUE" -> nbCritique++;
                case "INHABITUELLE" -> nbInhabituelle++;
                case "NORMAL" -> nbNormal++;
                default -> nbNonEvaluable++;
            }
        }

        int nbAnomalies = nbCritique + nbInhabituelle;

        return DetectionAnomaliesIaResponse.builder()
                .success(true)
                .totalArticles(results.size())
                .nbAnomalies(nbAnomalies)
                .nbCritique(nbCritique)
                .nbInhabituelle(nbInhabituelle)
                .nbNormal(nbNormal)
                .nbNonEvaluable(nbNonEvaluable)
                .results(results)
                .message("Détection automatique des consommations inhabituelles exécutée avec succès.")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public List<AnomalieConsommationIaDto> executerDetectionAnomaliesLocale(List<ArticlePDR> articles) {
        List<AnomalieConsommationIaDto> results = new ArrayList<>();
        for (ArticlePDR article : articles) {
            String codeSAP = article.getCodeSAP() != null ? article.getCodeSAP() : "";
            String description = article.getDescription() != null ? article.getDescription() : "";

            Map<String, Double> monthlyMap = new TreeMap<>();
            if (article.getConsommations() != null) {
                for (Consommation c : article.getConsommations()) {
                    if (c.getQuantiteConsommee() != null && c.getDateConsommation() != null) {
                        YearMonth ym = YearMonth.from(c.getDateConsommation());
                        double q = Math.abs(c.getQuantiteConsommee());
                        monthlyMap.put(ym.toString(), monthlyMap.getOrDefault(ym.toString(), 0.0) + q);
                    }
                }
            }

            List<Double> values = new ArrayList<>(monthlyMap.values());
            if (values.size() < 3) {
                double consoRecente = values.isEmpty() ? 0.0 : values.get(values.size() - 1);
                results.add(AnomalieConsommationIaDto.builder()
                        .codeSAP(codeSAP)
                        .description(description)
                        .consommationRecente(Math.round(consoRecente * 100.0) / 100.0)
                        .moyenneHistorique(0.0)
                        .ecartType(0.0)
                        .seuil(0.0)
                        .augmentationPourcentage(0.0)
                        .niveau("NON_EVALUABLE")
                        .anomalie(false)
                        .justification("Historique insuffisant pour l'évaluation statistique (minimum 3 mois requis).")
                        .build());
                continue;
            }

            double consoRecente = values.get(values.size() - 1);
            double sum = 0.0;
            for (double v : values) sum += v;
            double moyenne = sum / values.size();

            double varSum = 0.0;
            for (double v : values) varSum += Math.pow(v - moyenne, 2);
            double std = Math.sqrt(varSum / values.size());

            double seuil = moyenne + 2.0 * std;
            double seuilCritique = moyenne + 3.0 * std;
            double augPct = moyenne > 0 ? ((consoRecente - moyenne) / moyenne) * 100.0 : 0.0;

            String niveau;
            boolean anomalie;
            String justification;

            if (consoRecente > seuilCritique) {
                niveau = "CRITIQUE";
                anomalie = true;
                justification = String.format("La consommation récente (%.2f) dépasse sévèrement le seuil critique (%.2f).", consoRecente, seuilCritique);
            } else if (consoRecente > seuil) {
                niveau = "INHABITUELLE";
                anomalie = true;
                justification = "La consommation récente dépasse le seuil statistique calculé à partir de l'historique.";
            } else {
                niveau = "NORMAL";
                anomalie = false;
                justification = String.format("La consommation récente (%.2f) est conforme et inférieure ou égale au seuil statistique (%.2f).", consoRecente, seuil);
            }

            results.add(AnomalieConsommationIaDto.builder()
                    .codeSAP(codeSAP)
                    .description(description)
                    .consommationRecente(Math.round(consoRecente * 100.0) / 100.0)
                    .moyenneHistorique(Math.round(moyenne * 100.0) / 100.0)
                    .ecartType(Math.round(std * 100.0) / 100.0)
                    .seuil(Math.round(seuil * 100.0) / 100.0)
                    .augmentationPourcentage(Math.round(augPct * 100.0) / 100.0)
                    .niveau(niveau)
                    .anomalie(anomalie)
                    .justification(justification)
                    .build());
        }
        return results;
    }

    public PredictionIaResponse entrainerModele() {
        try {
            JsonNode trainingResponse = appelerServicePython("/train");
            log.info("Modèle Random Forest entraîné avec {} articles et {} échantillons",
                trainingResponse.path("dataset").path("articles").asInt(0),
                trainingResponse.path("evaluation").path("nbSamplesTrained").asInt(0));
                Map<String, Object> evaluation = trainingResponse.has("evaluation") && !trainingResponse.get("evaluation").isNull()
                    ? objectMapper.convertValue(trainingResponse.get("evaluation"), new TypeReference<Map<String, Object>>() {})
                    : null;
                Map<String, Object> dataset = trainingResponse.has("dataset") && !trainingResponse.get("dataset").isNull()
                    ? objectMapper.convertValue(trainingResponse.get("dataset"), new TypeReference<Map<String, Object>>() {})
                    : null;
                return PredictionIaResponse.builder()
                    .success(trainingResponse.path("success").asBoolean(false))
                    .evaluation(evaluation)
                    .dataset(dataset)
                    .modelStatus(trainingResponse.path("status").path("status").asText("UNKNOWN"))
                    .message("Modèle entraîné avec succès.")
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Erreur d'entraînement du modèle IA: {}", e.getMessage(), e);
            throw new IllegalStateException("Entraînement du modèle IA impossible.", e);
        }
    }

    private Map<String, Object> construirePayload(List<ArticlePDR> articles, boolean historiqueUniquement) {
        Map<String, Object> requestPayload = new HashMap<>();
        List<Map<String, Object>> articlesPayload = new ArrayList<>();
        YearMonth moisCourant = null;

        for (ArticlePDR article : articles) {
            Map<String, Object> artMap = new HashMap<>();
            artMap.put("codeSAP", article.getCodeSAP());
            artMap.put("description", article.getDescription() != null ? article.getDescription() : "");
            Double quantiteInstallee = article.getQuantiteInstallee();
            artMap.put("quantiteInstallee", quantiteInstallee != null ? quantiteInstallee.doubleValue() : 0.0);
            artMap.put("categorie", article.getCategorie() != null ? article.getCategorie() : "");

            List<Map<String, Object>> historique = new ArrayList<>();
            if (article.getConsommations() != null) {
                for (Consommation c : article.getConsommations()) {
                    boolean consommationHistorique = c.getDatasetType() == null || "HISTORICAL".equals(c.getDatasetType());
                    if (historiqueUniquement && !consommationHistorique) {
                        continue;
                    }
                    if (c.getQuantiteConsommee() != null && c.getDateConsommation() != null) {
                        Map<String, Object> pt = new HashMap<>();
                        pt.put("date", c.getDateConsommation().toString());
                        pt.put("quantite", Math.abs(c.getQuantiteConsommee()));
                        pt.put("numeroOT", c.getNumeroOT() != null ? c.getNumeroOT() : "");
                        historique.add(pt);
                    }
                    if ("CURRENT".equals(c.getDatasetType()) && c.getPeriodeImport() != null) {
                        try {
                            YearMonth candidate = YearMonth.parse(c.getPeriodeImport());
                            if (moisCourant == null || candidate.isAfter(moisCourant)) {
                                moisCourant = candidate;
                            }
                        } catch (Exception ignored) {
                            log.warn("Période courante invalide ignorée: {}", c.getPeriodeImport());
                        }
                    }
                }
            }
            artMap.put("historique", historique);
            articlesPayload.add(artMap);
        }

        requestPayload.put("articles", articlesPayload);
        if (!historiqueUniquement && moisCourant != null) {
            requestPayload.put("currentMonth", moisCourant.toString());
        }
        return requestPayload;
    }

    private JsonNode appelerServicePythonJson(Map<String, Object> requestPayload) throws Exception {
        return appelerServicePython(requestPayload, "/predict");
    }

    private JsonNode appelerServicePython(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return lireReponsePython(request, endpoint);
    }

    private JsonNode appelerServicePython(Map<String, Object> requestPayload, String endpoint) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestPayload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            return lireReponsePython(request, endpoint);
            }

            private JsonNode lireReponsePython(HttpRequest request, String endpoint) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Réponse HTTP invalide du service IA : " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        if ("/predict".equals(endpoint)) {
            JsonNode predNodes = root.get("predictions");
            if (predNodes == null || !predNodes.isArray()) {
                throw new RuntimeException("Format de réponse inattendu du service IA.");
            }
        }

        return root;
    }

    /**
     * Moteur de repli embarqué Java (Fallback statistique robuste avec score 0-100, features et explications).
     * Ce mécanisme est actuellement désactivé dans le flux principal, mais conservé pour compatibilité et maintenance.
     */
    public List<PredictionIaResultDto> executerModeleLocal(List<ArticlePDR> articles) {
        List<PredictionIaResultDto> results = new ArrayList<>();

        for (ArticlePDR article : articles) {
            String codeSAP = article.getCodeSAP() != null ? article.getCodeSAP() : "";
            String description = article.getDescription() != null ? article.getDescription() : "";
            Double quantiteInstallee = article.getQuantiteInstallee();
            double qInst = quantiteInstallee != null ? quantiteInstallee.doubleValue() : 0.0;
            double seuil = qInst > 0 ? 2.0 * qInst : 0.0;

            // Agrégation mensuelle chronologique
            Map<String, Double> monthlyMap = new TreeMap<>();
            if (article.getConsommations() != null) {
                for (Consommation c : article.getConsommations()) {
                    if (c.getQuantiteConsommee() != null && c.getQuantiteConsommee() > 0 && c.getDateConsommation() != null) {
                        YearMonth ym = YearMonth.from(c.getDateConsommation());
                        String key = ym.toString();
                        monthlyMap.put(key, monthlyMap.getOrDefault(key, 0.0) + c.getQuantiteConsommee());
                    }
                }
            }

            List<Map<String, Object>> historique = new ArrayList<>();
            for (Map.Entry<String, Double> entry : monthlyMap.entrySet()) {
                Map<String, Object> pt = new HashMap<>();
                pt.put("mois", entry.getKey());
                pt.put("quantite", Math.round(entry.getValue() * 100.0) / 100.0);
                historique.add(pt);
            }

            List<Double> values = new ArrayList<>(monthlyMap.values());
            double derniereConso = values.isEmpty() ? 0.0 : values.get(values.size() - 1).doubleValue();

            if (values.size() < 3) {
                List<String> exp = List.of(
                        "Historique insuffisant (< 3 mois de recul)",
                        "Modèle non appliqué pour éviter une fausse prédiction"
                );

                results.add(PredictionIaResultDto.builder()
                        .codeSAP(codeSAP)
                        .description(description)
                        .quantiteInstallee(qInst)
                        .consommationActuelle(Math.round(derniereConso * 100.0) / 100.0)
                        .consommationPredite(null)
                        .seuil(Math.round(seuil * 100.0) / 100.0)
                        .ratio(0.0)
                        .scoreRisque(0)
                        .niveauRisque("NON_EVALUABLE")
                        .alerte(false)
                        .ratioRisque(0.0)
                        .statut("INSUFFISANT")
                        .message("Données historiques insuffisantes (minimum 3 mois requis).")
                        .explications(exp)
                        .features(null)
                        .historique(historique)
                        .nbMoisHistorique(values.size())
                        .modeleUtilise("FALLBACK_JAVA_STATISTIQUE")
                        .build());
                continue;
            }

            int n = values.size();
            double lag1 = values.get(n - 1);
            double lag2 = values.get(n - 2);
            double lag3 = values.get(n - 3);

            double sum3 = lag1 + lag2 + lag3;
            double moy3 = sum3 / 3.0;

            double sum6 = 0;
            int count6 = Math.min(6, n);
            for (int i = n - count6; i < n; i++) {
                sum6 += values.get(i);
            }
            double moy6 = sum6 / count6;

            double variance = 0;
            for (int i = n - count6; i < n; i++) {
                variance += Math.pow(values.get(i) - moy6, 2);
            }
            double ecartType = Math.sqrt(variance / count6);

            double delta = lag1 - lag2;
            String tendanceStr;
            if (delta > 3.0) tendanceStr = "FORTE_HAUSSE";
            else if (delta > 0.5) tendanceStr = "HAUSSE";
            else if (delta < -0.5) tendanceStr = "BAISSE";
            else tendanceStr = "STABLE";

            double variationVsMoy = moy3 > 0 ? ((lag1 - moy3) / moy3) * 100.0 : 0.0;
            double ratioConsoInst = qInst > 0 ? (lag1 / qInst) : 0.0;

            Map<String, Object> featuresMap = new HashMap<>();
            featuresMap.put("lag1", Math.round(lag1 * 100.0) / 100.0);
            featuresMap.put("lag2", Math.round(lag2 * 100.0) / 100.0);
            featuresMap.put("lag3", Math.round(lag3 * 100.0) / 100.0);
            featuresMap.put("moyenne3", Math.round(moy3 * 100.0) / 100.0);
            featuresMap.put("moyenne6", Math.round(moy6 * 100.0) / 100.0);
            featuresMap.put("ecartType", Math.round(ecartType * 100.0) / 100.0);
            featuresMap.put("tendance", tendanceStr);
            featuresMap.put("variationVsMoyenne", Math.round(variationVsMoy * 10.0) / 10.0);
            featuresMap.put("ratioConsoInstallee", Math.round(ratioConsoInst * 100.0) / 100.0);

            if (qInst <= 0) {
                double predVal = Math.max(0.0, 0.7 * (lag1 + 0.5 * delta) + 0.3 * moy3);
                results.add(PredictionIaResultDto.builder()
                        .codeSAP(codeSAP)
                        .description(description)
                        .quantiteInstallee(0.0)
                        .consommationActuelle(Math.round(derniereConso * 100.0) / 100.0)
                        .consommationPredite(Math.round(predVal * 100.0) / 100.0)
                        .seuil(0.0)
                        .ratio(0.0)
                        .scoreRisque(0)
                        .niveauRisque("NON_EVALUABLE")
                        .alerte(false)
                        .ratioRisque(0.0)
                        .statut("SANS_QUANTITE_INSTALLEE")
                        .message("Quantité installée inconnue ou nulle. Seuil d'anomalie 200% non calculable.")
                        .explications(List.of("Quantité installée non renseignée", "Seuil OCP non calculable"))
                        .features(featuresMap)
                        .historique(historique)
                        .nbMoisHistorique(values.size())
                        .modeleUtilise("FALLBACK_JAVA_STATISTIQUE")
                        .build());
                continue;
            }

            double predVal = predireValeurSuivante(values);
            double ratio = seuil > 0 ? predVal / seuil : 0.0;

            double sSeuil = Math.min(100.0, (ratio / 1.5) * 100.0);
            double sTendance = "FORTE_HAUSSE".equals(tendanceStr) ? 95.0 : ("HAUSSE".equals(tendanceStr) ? 70.0 : ("STABLE".equals(tendanceStr) ? 30.0 : 10.0));
            double sEvol = variationVsMoy > 50 ? 90.0 : (variationVsMoy > 20 ? 65.0 : (variationVsMoy >= -10 ? 35.0 : 10.0));
            double sVol = Math.min(100.0, (ecartType / (moy3 > 0 ? moy3 : 1.0)) * 100.0);

            int scoreRisque = (int) Math.round(0.40 * sSeuil + 0.25 * sTendance + 0.20 * sEvol + 0.15 * sVol);
            scoreRisque = Math.max(0, Math.min(100, scoreRisque));

            String niveau;
            if (scoreRisque >= 85) niveau = "CRITIQUE";
            else if (scoreRisque >= 70) niveau = "ELEVE";
            else if (scoreRisque >= 40) niveau = "MOYEN";
            else niveau = "FAIBLE";

            boolean alerte = "CRITIQUE".equals(niveau) || "ELEVE".equals(niveau) || ratio >= 1.0;

            List<String> explications = new ArrayList<>();
            if (ratio >= 1.0) explications.add(String.format("Dépassement du seuil OCP de 200%% prévu (%.1f vs seuil %.1f)", predVal, seuil));
            else if (ratio >= 0.7) explications.add(String.format("Prévision proche du seuil d'alerte OCP (%.0f%% du seuil)", ratio * 100.0));
            else explications.add("Prévision conforme et inférieure au seuil OCP.");

            if ("FORTE_HAUSSE".equals(tendanceStr)) explications.add("Forte accélération de la consommation observée.");
            else if ("HAUSSE".equals(tendanceStr)) explications.add("Tendance générale à la hausse.");

            if (variationVsMoy >= 30.0) explications.add(String.format("Consommation récente en hausse de +%.0f%% vs moyenne.", variationVsMoy));

            results.add(PredictionIaResultDto.builder()
                    .codeSAP(codeSAP)
                    .description(description)
                    .quantiteInstallee(qInst)
                    .consommationActuelle(Math.round(derniereConso * 100.0) / 100.0)
                    .consommationPredite(Math.round(predVal * 100.0) / 100.0)
                    .seuil(Math.round(seuil * 100.0) / 100.0)
                    .ratio(Math.round(ratio * 100.0) / 100.0)
                    .scoreRisque(scoreRisque)
                    .niveauRisque(niveau)
                    .alerte(alerte)
                    .ratioRisque(Math.round(ratio * 100.0) / 100.0)
                    .statut("PREDICTION_DISPONIBLE")
                    .message("Prévision calculée via le moteur statistique de repli.")
                    .explications(explications)
                    .features(featuresMap)
                    .historique(historique)
                    .nbMoisHistorique(values.size())
                    .modeleUtilise("FALLBACK_JAVA_STATISTIQUE")
                    .build());
        }

        return results;
    }

    private double predireValeurSuivante(List<Double> series) {
        int n = series.size();
        if (n == 0) return 0.0;
        if (n == 1) return series.get(0);

        double sumX = 0;
        double sumY = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += series.get(i);
        }
        double xMean = sumX / n;
        double yMean = sumY / n;

        double num = 0;
        double denom = 0;
        for (int i = 0; i < n; i++) {
            num += (i - xMean) * (series.get(i) - yMean);
            denom += Math.pow(i - xMean, 2);
        }

        double trendPred;
        if (denom > 0) {
            double slope = num / denom;
            double intercept = yMean - slope * xMean;
            trendPred = intercept + slope * n;
        } else {
            trendPred = yMean;
        }

        double alpha = 0.6;
        double ema = series.get(0);
        for (int i = 1; i < n; i++) {
            ema = alpha * series.get(i) + (1 - alpha) * ema;
        }

        double finalPred = 0.7 * trendPred + 0.3 * ema;
        return Math.max(0.0, finalPred);
    }
}

