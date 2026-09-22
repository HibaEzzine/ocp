package com.ocp.pdr.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;
import com.ocp.pdr.repository.ArticlePDRRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleSummaryService {

    private final ArticlePDRRepository articlePDRRepository;

    public Map<String, Object> buildSummary() {
        Map<String, Object> summary = new HashMap<>();
        List<ArticlePDR> allArticles = articlePDRRepository.findAllWithStocks();

        summary.put("totalArticles", allArticles.size());
        summary.put("articlesAvecSeuil", allArticles.stream()
                .filter(article -> article.getSeuilMin() != null && article.getSeuilMax() != null)
                .count());
        summary.put("articlesSansStock", allArticles.stream()
                .filter(article -> article.getStocks() != null && article.getStocks().stream()
                        .mapToDouble(stock -> stock.getQuantiteStock() == null ? 0.0 : stock.getQuantiteStock())
                        .sum() == 0)
                .count());
        summary.put("articlesCritiques", allArticles.stream()
                .filter(article -> article.getGroupeHomogene() == GroupeHomogene.CRITIQUE)
                .count());
        summary.put("timestamp", System.currentTimeMillis());

        return summary;
    }
}
