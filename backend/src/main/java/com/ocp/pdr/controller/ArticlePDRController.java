package com.ocp.pdr.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ocp.pdr.exception.ResourceNotFoundException;
import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.ocp.pdr.service.ArticleSummaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ArticlePDRController {

    private final ArticlePDRRepository articlePDRRepository;
    private final ArticleSummaryService articleSummaryService;

    @GetMapping
    public ResponseEntity<Page<ArticlePDR>> getAllArticles(Pageable pageable) {
        log.info("Récupération des articles avec pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(articlePDRRepository.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ArticlePDR>> searchArticles(@RequestParam String term, Pageable pageable) {
        log.info("Recherche d'articles: term={}", term);
        return ResponseEntity.ok(articlePDRRepository.search(term, pageable));
    }

    @GetMapping("/groupe/{groupe}")
    public ResponseEntity<Page<ArticlePDR>> getArticlesByGroupe(@PathVariable GroupeHomogene groupe, Pageable pageable) {
        log.info("Récupération des articles du groupe: {}", groupe);
        return ResponseEntity.ok(articlePDRRepository.findByGroupeHomogene(groupe, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticlePDR> getArticleById(@PathVariable Long id) {
        log.info("Récupération de l'article: id={}", id);
        return articlePDRRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + id));
    }

    @GetMapping("/code/{codeSAP}")
    public ResponseEntity<ArticlePDR> getArticleByCodeSAP(@PathVariable String codeSAP) {
        log.info("Récupération de l'article par code SAP: {}", codeSAP);
        return articlePDRRepository.findByCodeSAP(codeSAP)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec le code SAP: " + codeSAP));
    }

    @PostMapping
    public ResponseEntity<ArticlePDR> createArticle(@RequestBody ArticlePDR article) {
        log.info("Création d'un nouvel article: {}", article.getCodeSAP());

        if (article.getId() == null) {
            article.setDateCreation(LocalDateTime.now());
        }
        article.setDateModification(LocalDateTime.now());

        ArticlePDR saved = articlePDRRepository.save(article);
        log.info("Article créé avec succès: id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticlePDR> updateArticle(@PathVariable Long id, @RequestBody ArticlePDR article) {
        log.info("Mise à jour de l'article: id={}", id);

        ArticlePDR existing = articlePDRRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé avec l'id: " + id));

        article.setId(id);
        article.setDateCreation(existing.getDateCreation());
        article.setDateModification(LocalDateTime.now());

        ArticlePDR updated = articlePDRRepository.save(article);
        log.info("Article mis à jour avec succès: id={}", id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        log.info("Suppression de l'article: id={}", id);

        if (!articlePDRRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article non trouvé avec l'id: " + id);
        }

        articlePDRRepository.deleteById(id);
        log.info("Article supprimé avec succès: id={}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(articleSummaryService.buildSummary());
    }
}
