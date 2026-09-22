package com.ocp.pdr.service;

import org.springframework.stereotype.Service;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;

@Service
public class ReglePlanifieService {

    public boolean verifier(ArticlePDR article) {
        if (article == null || article.getBacklogOTs() == null || article.getStocks() == null) {
            return false;
        }

        if (isGroupePrioritaire(article)) {
            return false;
        }

        double stockTotal = calculerStockTotal(article);
        boolean stockEnDessousDuMin = article.getSeuilMin() != null && stockTotal < article.getSeuilMin();

        return stockEnDessousDuMin && article.getBacklogOTs().stream()
                .anyMatch(ot -> ot.getQuantiteNonLancee() != null && ot.getQuantiteNonLancee() > 0);
    }

    public Double calculerQuantite(ArticlePDR article) {
        if (article == null || article.getSeuilMax() == null) {
            return 0.0;
        }

        double quantiteALancer = article.getSeuilMax() - calculerStockTotal(article);
        return Math.max(quantiteALancer, 0.0);
    }

    private boolean isGroupePrioritaire(ArticlePDR article) {
        GroupeHomogene groupe = article.getGroupeHomogene();
        return groupe == GroupeHomogene.CURATIF
                || groupe == GroupeHomogene.CRITIQUE
                || groupe == GroupeHomogene.CONDITIONNEL;
    }

    private double calculerStockTotal(ArticlePDR article) {
        if (article.getStocks() == null) {
            return 0.0;
        }

        return article.getStocks().stream()
                .mapToDouble(stock -> stock.getQuantiteStock() != null ? stock.getQuantiteStock() : 0.0)
                .sum();
    }
}
