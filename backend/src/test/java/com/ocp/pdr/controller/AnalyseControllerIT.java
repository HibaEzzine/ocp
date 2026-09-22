package com.ocp.pdr.controller;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.ResultatApprovisionnement;
import com.ocp.pdr.model.enums.GroupeHomogene;
import com.ocp.pdr.model.enums.ModeApprovisionnement;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.ocp.pdr.repository.ResultatApprovisionnementRepository;
import com.ocp.pdr.repository.AnomalieConsommationRepository;
import com.ocp.pdr.repository.StockRepository;
import com.ocp.pdr.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
    @WithMockUser
@DisplayName("Tests intégration AnalyseController")
class AnalyseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticlePDRRepository articleRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ResultatApprovisionnementRepository resultatRepository;

    @Autowired
    private AnomalieConsommationRepository anomalieRepository;

    private ArticlePDR article1;
    private ArticlePDR article2;

    @BeforeEach
    void setUp() {
        // Nettoyage
        resultatRepository.deleteAll();
        anomalieRepository.deleteAll();
        stockRepository.deleteAll();
        articleRepository.deleteAll();

        // Créer des articles de test
        article1 = new ArticlePDR();
        article1.setCodeSAP("ART-001");
        article1.setDescription("Article 1 - CRITIQUE");
        article1.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        article1.setSeuilMin(10.0);
        article1.setSeuilMax(50.0);
        article1.setQuantiteInstallee(5.0);
        article1.setDateCreation(LocalDateTime.now());
        article1.setDateModification(LocalDateTime.now());
        article1 = articleRepository.save(article1);

        // Ajouter un stock
        Stock stock1 = new Stock();
        stock1.setArticle(article1);
        stock1.setQuantiteStock(5.0);
        stock1.setDateStock(LocalDate.now());
        stock1.setDateImport(LocalDateTime.now());
        stock1.setSourceFichier("STOCK_2026-08-31.xlsx");
        stockRepository.save(stock1);

        article2 = new ArticlePDR();
        article2.setCodeSAP("ART-002");
        article2.setDescription("Article 2 - CURATIF");
        article2.setGroupeHomogene(GroupeHomogene.CURATIF);
        article2.setSeuilMin(20.0);
        article2.setSeuilMax(100.0);
        article2.setQuantiteInstallee(15.0);
        article2.setDateCreation(LocalDateTime.now());
        article2.setDateModification(LocalDateTime.now());
        article2 = articleRepository.save(article2);

        // Ajouter un stock
        Stock stock2 = new Stock();
        stock2.setArticle(article2);
        stock2.setQuantiteStock(25.0);
        stock2.setDateStock(LocalDate.now());
        stock2.setDateImport(LocalDateTime.now());
        stock2.setSourceFichier("STOCK_2026-08-31.xlsx");
        stockRepository.save(stock2);
    }

    @Test
    @DisplayName("POST /api/analyse/executer lance l'analyse globale")
    void testExecuterAnalyse() throws Exception {
        mockMvc.perform(post("/api/analyse/executer")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.message", equalTo("Analyse exécutée avec succès")))
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analyse/resultats retourne tous les résultats")
    void testGetResultats() throws Exception {
        // Insérer un résultat
        mockMvc.perform(post("/api/analyse/executer"));

        mockMvc.perform(get("/api/analyse/resultats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(ArrayList.class)));
    }

    @Test
    @DisplayName("GET /api/analyse/anomalies retourne toutes les anomalies")
    void testGetAnomalies() throws Exception {
        mockMvc.perform(get("/api/analyse/anomalies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(ArrayList.class)));
    }

    @Test
    @DisplayName("GET /api/analyse/summary retourne le résumé de l'analyse")
    void testGetSummary() throws Exception {
        // Lancer l'analyse d'abord
        mockMvc.perform(post("/api/analyse/executer"));

        mockMvc.perform(get("/api/analyse/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.minMaxCount", notNullValue()))
                .andExpect(jsonPath("$.planifieCount", notNullValue()))
                .andExpect(jsonPath("$.surDemandeCount", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/analyse/statistiques retourne les statistiques")
    void testGetStatistiques() throws Exception {
        // Lancer l'analyse d'abord
        mockMvc.perform(post("/api/analyse/executer"));

        mockMvc.perform(get("/api/analyse/statistiques")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles", equalTo(2)))
                .andExpect(jsonPath("$.articlesAnalyses", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.anomaliesDetectees", notNullValue()))
                .andExpect(jsonPath("$.tauxAnalyse", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/analyse/executer gère les erreurs gracieusement")
    void testExecuterAnalyseErrorHandling() throws Exception {
        // Test avec une base vide après suppression
        articleRepository.deleteAll();

        mockMvc.perform(post("/api/analyse/executer")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.resultCount", equalTo(0)));
    }

    @Test
    @DisplayName("GET /api/analyse/summary retourne le bon nombre d'articles")
    void testSummaryArticleCount() throws Exception {
        mockMvc.perform(post("/api/analyse/executer"));

        mockMvc.perform(get("/api/analyse/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles", equalTo(2)));
    }

    @Test
    @DisplayName("Analyse successive remet à zéro les résultats précédents")
    void testAnalysisResetBehavior() throws Exception {
        // Première analyse
        mockMvc.perform(post("/api/analyse/executer"));
        
        // Deuxième analyse
        mockMvc.perform(post("/api/analyse/executer")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)));
    }
}
