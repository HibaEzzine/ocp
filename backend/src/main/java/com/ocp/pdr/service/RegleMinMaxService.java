package com.ocp.pdr.service;
import org.springframework.stereotype.Service;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;
@Service
public class RegleMinMaxService {

    public boolean verifier(ArticlePDR article) {
        if (article == null || article.getStocks() == null) {
            return false;
        }

        if (!isGroupePrioritaire(article)) {
            return false;
        }

        double stockTotal = calculerStockTotal(article);
        return article.getSeuilMin() != null && stockTotal < article.getSeuilMin();
    }

    public Double calculerQuantite(ArticlePDR article) {
        if (article == null || article.getSeuilMax() == null) {
            return 0.0;
        }

        double stockTotal = calculerStockTotal(article);
        double quantiteALancer = article.getSeuilMax() - stockTotal;
        return quantiteALancer > 0 ? quantiteALancer : 0.0;
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
                .mapToDouble(stock -> stock.getQuantiteStock() == null ? 0.0 : stock.getQuantiteStock())
                .sum();
    }
}
