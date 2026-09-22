package com.ocp.pdr.controller;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
    @WithMockUser
@DisplayName("Tests intégration ArticlePDRController")
class ArticlePDRControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ArticlePDRRepository articleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ArticlePDR testArticle;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();

        testArticle = new ArticlePDR();
        testArticle.setCodeSAP("TEST-001");
        testArticle.setCodeOracle("ORACLE-001");
        testArticle.setDescription("Article de test");
        testArticle.setUdm("PIECE");
        testArticle.setQuantiteInstallee(10.0);
        testArticle.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        testArticle.setSeuilMin(5.0);
        testArticle.setSeuilMax(50.0);
        testArticle.setDateCreation(LocalDateTime.now());
        testArticle.setDateModification(LocalDateTime.now());

        testArticle = articleRepository.save(testArticle);
    }

    @Test
    @DisplayName("GET /api/articles retourne la liste paginée")
    void testGetAllArticles() throws Exception {
        mockMvc.perform(get("/api/articles")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/articles/{id} retourne un article spécifique")
    void testGetArticleById() throws Exception {
        mockMvc.perform(get("/api/articles/" + testArticle.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeSAP", equalTo("TEST-001")))
                .andExpect(jsonPath("$.groupeHomogene", equalTo("CRITIQUE")))
                .andExpect(jsonPath("$.seuilMin", equalTo(5.0)))
                .andExpect(jsonPath("$.seuilMax", equalTo(50.0)));
    }

    @Test
    @DisplayName("GET /api/articles/code/{codeSAP} retourne l'article par code SAP")
    void testGetArticleByCodeSAP() throws Exception {
        mockMvc.perform(get("/api/articles/code/TEST-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeSAP", equalTo("TEST-001")))
                .andExpect(jsonPath("$.description", equalTo("Article de test")));
    }

    @Test
    @DisplayName("GET /api/articles/search?term=xxx recherche les articles")
    void testSearchArticles() throws Exception {
        mockMvc.perform(get("/api/articles/search")
                .param("term", "test")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/articles/groupe/{groupe} filtre par groupe homogène")
    void testGetArticlesByGroupe() throws Exception {
        mockMvc.perform(get("/api/articles/groupe/CRITIQUE")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].groupeHomogene", equalTo("CRITIQUE")));
    }

    @Test
    @DisplayName("POST /api/articles crée un nouvel article")
    void testCreateArticle() throws Exception {
        ArticlePDR newArticle = new ArticlePDR();
        newArticle.setCodeSAP("NEW-001");
        newArticle.setDescription("Nouvel article");
        newArticle.setGroupeHomogene(GroupeHomogene.CURATIF);
        newArticle.setSeuilMin(10.0);
        newArticle.setSeuilMax(100.0);

        String json = objectMapper.writeValueAsString(newArticle);

        MvcResult result = mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codeSAP", equalTo("NEW-001")))
                .andExpect(jsonPath("$.groupeHomogene", equalTo("CURATIF")))
                .andReturn();

        // Vérifier que la dateCreation a été définie
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("dateCreation");
        assertThat(responseBody).contains("dateModification");
    }

    @Test
    @DisplayName("PUT /api/articles/{id} met à jour un article")
    void testUpdateArticle() throws Exception {
        testArticle.setDescription("Description mise à jour");
        testArticle.setSeuilMax(75.0);

        String json = objectMapper.writeValueAsString(testArticle);

        mockMvc.perform(put("/api/articles/" + testArticle.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", equalTo("Description mise à jour")))
                .andExpect(jsonPath("$.seuilMax", equalTo(75.0)));
    }

    @Test
    @DisplayName("DELETE /api/articles/{id} supprime un article")
    void testDeleteArticle() throws Exception {
        Long idToDelete = testArticle.getId();

        mockMvc.perform(delete("/api/articles/" + idToDelete))
                .andExpect(status().isNoContent());

        // Vérifier que l'article a bien été supprimé
        mockMvc.perform(get("/api/articles/" + idToDelete))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/articles/summary retourne les statistiques")
    void testGetSummary() throws Exception {
        mockMvc.perform(get("/api/articles/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalArticles", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.articlesAvecSeuil", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.articlesCritiques", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/articles/{id} retourne 404 si article n'existe pas")
    void testGetArticleByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/articles/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/articles retourne erreur si données invalides")
    void testCreateArticleWithInvalidData() throws Exception {
        ArticlePDR invalidArticle = new ArticlePDR();
        // Pas de code SAP (requis)
        invalidArticle.setDescription("Article sans SAP");

        String json = objectMapper.writeValueAsString(invalidArticle);

        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }
}
