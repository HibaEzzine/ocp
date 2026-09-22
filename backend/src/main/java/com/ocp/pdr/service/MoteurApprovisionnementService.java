package com.ocp.pdr.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.HistoriqueTraitement;
import com.ocp.pdr.model.ResultatApprovisionnement;
import com.ocp.pdr.model.enums.ModeApprovisionnement;
import com.ocp.pdr.repository.AnomalieConsommationRepository;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.ocp.pdr.repository.HistoriqueTraitementRepository;
import com.ocp.pdr.repository.ResultatApprovisionnementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoteurApprovisionnementService {

    private final RegleMinMaxService regleMinMaxService;
    private final ReglePlanifieService reglePlanifieService;
    private final RegleSurDemandeService regleSurDemandeService;
    private final AnalyseConsommationService analyseConsommationService;
    
    private final ArticlePDRRepository articlePDRRepository;
    private final ResultatApprovisionnementRepository resultatApprovisionnementRepository;
    private final AnomalieConsommationRepository anomalieConsommationRepository;
    private final HistoriqueTraitementRepository historiqueTraitementRepository;

    @Transactional
    public ResultatApprovisionnement analyser(ArticlePDR article) {
        ResultatApprovisionnement resultat = new ResultatApprovisionnement();
        resultat.setArticle(article);
        resultat.setDateAnalyse(LocalDate.now());

        // 1. Min & Max
        if (regleMinMaxService.verifier(article)) {
            resultat.setMode(ModeApprovisionnement.MIN_MAX);
            resultat.setPriorite(1);
            resultat.setQuantiteALancer(regleMinMaxService.calculerQuantite(article));
            resultat.setJustification("Article ciblé avec Stock < Seuil Minimum");
        } 
        // 2. Mode Planifié
        else if (reglePlanifieService.verifier(article)) {
            resultat.setMode(ModeApprovisionnement.PLANIFIE);
            resultat.setPriorite(2);
            resultat.setQuantiteALancer(reglePlanifieService.calculerQuantite(article));
            resultat.setJustification("Article requis pour OT du backlog et non retenu par Min&Max");
        } 
        // 3. Sur Demande
        else if (regleSurDemandeService.verifier(article)) {
            resultat.setMode(ModeApprovisionnement.SUR_DEMANDE);
            resultat.setPriorite(3);
            resultat.setQuantiteALancer(regleSurDemandeService.calculerQuantite(article));
            resultat.setJustification("Rupture de stock non couverte par Min&Max ni Planifié");
        } else {
            // Aucun besoin d'approvisionnement détecté
            return null;
        }

        // Sauvegarder le résultat
        if (resultat.getQuantiteALancer() != null && resultat.getQuantiteALancer() > 0) {
            return resultatApprovisionnementRepository.save(resultat);
        }
        
        return null;
    }

    @Transactional
    public List<ResultatApprovisionnement> executerAnalyseGlobale() {
        log.info("Démarrage de l'analyse globale d'approvisionnement");
        long startTime = System.currentTimeMillis();
        
        try {
            resultatApprovisionnementRepository.deleteAll();
            anomalieConsommationRepository.deleteAll();
            
            List<ArticlePDR> allArticles = articlePDRRepository.findAll();
            List<ResultatApprovisionnement> resultats = new ArrayList<>();

            log.info("Nombre d'articles à analyser: {}", allArticles.size());

            for (ArticlePDR article : allArticles) {
                try {
                    // 1. Analyse d'approvisionnement
                    ResultatApprovisionnement resultat = analyser(article);
                    if (resultat != null) {
                        resultats.add(resultat);
                    }

                    // 2. Analyse des consommations inhabituelles
                    analyseConsommationService.detecterEtSauvegarderAnomalie(article);
                } catch (Exception e) {
                    log.error("Erreur lors de l'analyse de l'article {}: {}", article.getCodeSAP(), e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("Analyse globale terminée en {}ms. Résultats générés: {}", duration, resultats.size());
            enregistrerTraitement("SUCCESS", "Analyse globale terminée en " + duration + "ms");
            
            return resultats;
        } catch (Exception e) {
            log.error("Erreur critique lors de l'analyse globale", e);
            enregistrerTraitement("ERROR", e.getMessage());
            throw new RuntimeException("Erreur lors de l'analyse globale: " + e.getMessage());
        }
    }

    private void enregistrerTraitement(String statut, String message) {
        if (historiqueTraitementRepository == null) {
            return;
        }
        HistoriqueTraitement historique = new HistoriqueTraitement();
        historique.setOperation("ANALYSE_GLOBALE");
        historique.setDateOperation(java.time.LocalDateTime.now());
        historique.setStatut(statut);
        historique.setMessage(message);
        historiqueTraitementRepository.save(historique);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenirSummaryAnalyse() {
        Map<String, Object> summary = new HashMap<>();

        try {
            List<ResultatApprovisionnement> resultats = resultatApprovisionnementRepository.findAll();
            long totalArticles = articlePDRRepository.count();

            long minMaxCount = resultats.stream()
                    .filter(r -> r.getMode() == ModeApprovisionnement.MIN_MAX)
                    .count();
            long planifieCount = resultats.stream()
                    .filter(r -> r.getMode() == ModeApprovisionnement.PLANIFIE)
                    .count();
            long surDemandeCount = resultats.stream()
                    .filter(r -> r.getMode() == ModeApprovisionnement.SUR_DEMANDE)
                    .count();
            long zeroStockArticles = articlePDRRepository.countArticlesWithZeroStock();
            long articlesEnStock = Math.max(0, totalArticles - zeroStockArticles);

            double minMaxRate = totalArticles > 0 ? (minMaxCount * 100.0) / totalArticles : 0.0;
            double planifieRate = totalArticles > 0 ? (planifieCount * 100.0) / totalArticles : 0.0;
            double surDemandeRate = totalArticles > 0 ? (surDemandeCount * 100.0) / totalArticles : 0.0;
            double stockRate = totalArticles > 0 ? (articlesEnStock * 100.0) / totalArticles : 0.0;

            summary.put("totalArticles", totalArticles);
            summary.put("minMaxCount", minMaxCount);
            summary.put("planifieCount", planifieCount);
            summary.put("surDemandeCount", surDemandeCount);
            summary.put("anomaliesCount", anomalieConsommationRepository.count());
            summary.put("articlesEnStock", articlesEnStock);
            summary.put("articlesSansStock", zeroStockArticles);
            summary.put("minMaxRate", minMaxRate);
            summary.put("planifieRate", planifieRate);
            summary.put("surDemandeRate", surDemandeRate);
            summary.put("stockRate", stockRate);
            summary.put("resultatCount", resultats.size());

            return summary;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du summary", e);
            throw new RuntimeException("Erreur lors de la génération du summary: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenirSummaryAnalyseDto() {
        return obtenirSummaryAnalyse();
    }

    @Transactional(readOnly = true)
    public List<ResultatApprovisionnement> obtenirTousLesResultats() {
        return resultatApprovisionnementRepository.findAllWithArticle();
    }

    @Transactional(readOnly = true)
    public boolean isAnalyseLancee() {
        return historiqueTraitementRepository
                .findTopByOperationAndStatutOrderByDateOperationDesc("ANALYSE_GLOBALE", "SUCCESS")
                .isPresent();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenirStatistiques() {
        Map<String, Object> stats = new HashMap<>();
        long totalArticles = articlePDRRepository.count();
        long totalResultats = resultatApprovisionnementRepository.count();
        long totalAnomalies = anomalieConsommationRepository.count();

        stats.put("totalArticles", totalArticles);
        stats.put("articlesAnalyses", totalResultats);
        stats.put("anomaliesDetectees", totalAnomalies);
        stats.put("tauxAnalyse", totalArticles > 0 ? (double) totalResultats / totalArticles * 100 : 0);
        stats.put("timestamp", System.currentTimeMillis());

        return stats;
    }
}
