package com.ocp.pdr.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ocp.pdr.model.AnomalieConsommation;
import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.Consommation;
import com.ocp.pdr.repository.AnomalieConsommationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyseConsommationService {

    private final AnomalieConsommationRepository anomalieConsommationRepository;

    public Double calculerConsommationMensuelle(ArticlePDR article) {
        if (article == null || article.getConsommations() == null) {
            return 0.0;
        }

        LocalDate derniereDate = article.getConsommations().stream()
            .map(Consommation::getDateConsommation)
            .filter(date -> date != null)
            .max(LocalDate::compareTo)
            .orElse(null);
        if (derniereDate == null) {
            return 0.0;
        }

        YearMonth dernierMois = YearMonth.from(derniereDate);
        return article.getConsommations().stream()
            .filter(c -> c.getDateConsommation() != null
                && YearMonth.from(c.getDateConsommation()).equals(dernierMois))
                .mapToDouble(c -> c.getQuantiteConsommee() != null ? c.getQuantiteConsommee() : 0.0)
                .sum();
    }

    public boolean verifierSeuil(ArticlePDR article) {
        if (article == null || article.getQuantiteInstallee() == null || article.getQuantiteInstallee() == 0) {
            return false; // Evite la division par zéro ou fausse alerte si pas de qté installée
        }

        Double consoMensuelle = calculerConsommationMensuelle(article);
        
        // Règle: Consommation mensuelle > 2 × Quantité installée
        return consoMensuelle > (2 * article.getQuantiteInstallee());
    }

    public Double calculerTauxConsommation(ArticlePDR article) {
        if (article == null || article.getQuantiteInstallee() == null || article.getQuantiteInstallee() == 0) {
            return 0.0;
        }
        return calculerConsommationMensuelle(article) / article.getQuantiteInstallee();
    }

    public AnomalieConsommation detecterEtSauvegarderAnomalie(ArticlePDR article) {
        if (verifierSeuil(article)) {
            AnomalieConsommation anomalie = new AnomalieConsommation();
            anomalie.setArticle(article);
            anomalie.setConsommationMensuelle(calculerConsommationMensuelle(article));
            anomalie.setQuantiteInstallee(article.getQuantiteInstallee());
            anomalie.setTauxConsommation(calculerTauxConsommation(article));
            anomalie.setSeuil(2.0); // 200%
            anomalie.setDateDetection(LocalDate.now());
            anomalie.setStatut("DETECTEE");
            
            return anomalieConsommationRepository.save(anomalie);
        }
        return null;
    }

    public List<AnomalieConsommation> obtenirToutesAnomalies() {
        return anomalieConsommationRepository.findAllWithArticle();
    }
}
