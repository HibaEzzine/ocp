package com.ocp.pdr.service;

import org.springframework.stereotype.Service;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;

@Service
public class RegleSurDemandeService {

    public boolean verifier(ArticlePDR article) {
        if (article == null || article.getStocks() == null) {
            return false;
        }

        boolean presentDansBacklog = article.getBacklogOTs() != null && !article.getBacklogOTs().isEmpty();

        double stockTotal = calculerStockTotal(article);
        boolean stockInsuffisant = article.getSeuilMin() != null && stockTotal < article.getSeuilMin();

        return !isGroupePrioritaire(article)
                && !presentDansBacklog
                && stockInsuffisant;
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
