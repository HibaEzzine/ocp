package com.ocp.pdr.service;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.Stock;
import com.ocp.pdr.model.enums.GroupeHomogene;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests RegleMinMaxService")
class RegleMinMaxServiceTest {

    private RegleMinMaxService service;
    private ArticlePDR article;

    @BeforeEach
    void setUp() {
        service = new RegleMinMaxService();
        article = new ArticlePDR();
        article.setSeuilMin(10.0);
        article.setSeuilMax(50.0);
    }

    @Test
    @DisplayName("Verifier() retourne true si article CRITIQUE et stock < seuil_min")
    void testVerifierArticleCritiqueSousSeuilMin() {
        // Given
        article.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        Stock stock = new Stock();
        stock.setQuantiteStock(5.0);
        article.setStocks(Arrays.asList(stock));

        // When
        boolean result = service.verifier(article);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Verifier() retourne false si stock >= seuil_min")
    void testVerifierArticleStockSuffisant() {
        // Given
        article.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        Stock stock = new Stock();
        stock.setQuantiteStock(15.0);
        article.setStocks(Arrays.asList(stock));

        // When
        boolean result = service.verifier(article);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Verifier() retourne false si groupe non prioritaire")
    void testVerifierArticleGroupeNonPrioritaire() {
        // Tous les groupes sont prioritaires, donc on teste sans groupe
        article.setGroupeHomogene(null);
        Stock stock = new Stock();
        stock.setQuantiteStock(5.0);
        article.setStocks(Arrays.asList(stock));

        // When
        boolean result = service.verifier(article);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("CalculerQuantite() retourne seuil_max - stock_total")
    void testCalculerQuantite() {
        // Given
        article.setSeuilMax(50.0);
        Stock stock = new Stock();
        stock.setQuantiteStock(15.0);
        article.setStocks(Arrays.asList(stock));

        // When
        Double quantite = service.calculerQuantite(article);

        // Then
        assertThat(quantite).isEqualTo(35.0);
    }

    @Test
    @DisplayName("CalculerQuantite() retourne 0 si stock >= seuil_max")
    void testCalculerQuantiteZeroSiStockSuffisant() {
        // Given
        article.setSeuilMax(50.0);
        Stock stock = new Stock();
        stock.setQuantiteStock(50.0);
        article.setStocks(Arrays.asList(stock));

        // When
        Double quantite = service.calculerQuantite(article);

        // Then
        assertThat(quantite).isEqualTo(0.0);
    }

    @Test
    @DisplayName("CalculerQuantite() gère les stocks nuls")
    void testCalculerQuantiteAvecStocksNuls() {
        // Given
        article.setSeuilMax(50.0);
        article.setStocks(new ArrayList<>());

        // When
        Double quantite = service.calculerQuantite(article);

        // Then
        assertThat(quantite).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Verifier() gère les articles sans stocks")
    void testVerifierArticleSansStock() {
        // Given
        article.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        article.setStocks(null);

        // When
        boolean result = service.verifier(article);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Verifier() gère les articles sans seuil_min")
    void testVerifierArticleSansSeuilMin() {
        // Given
        article.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        article.setSeuilMin(null);
        Stock stock = new Stock();
        stock.setQuantiteStock(5.0);
        article.setStocks(Arrays.asList(stock));

        // When
        boolean result = service.verifier(article);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Verifier() fonctionne avec les trois groupes prioritaires")
    void testVerifierAvecTousLesGroupesPrioritaires() {
        // Given
        Stock stock = new Stock();
        stock.setQuantiteStock(5.0);
        article.setStocks(Arrays.asList(stock));

        // When & Then
        article.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        assertThat(service.verifier(article)).isTrue();

        article.setGroupeHomogene(GroupeHomogene.CURATIF);
        assertThat(service.verifier(article)).isTrue();

        article.setGroupeHomogene(GroupeHomogene.CONDITIONNEL);
        assertThat(service.verifier(article)).isTrue();
    }
}
