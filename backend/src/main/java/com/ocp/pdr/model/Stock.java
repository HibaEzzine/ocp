package com.ocp.pdr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_stock")
    private Double quantiteStock;

    @Column(name = "date_stock")
    private LocalDate dateStock;

    @Column(name = "source_fichier")
    private String sourceFichier;

    @Column(name = "date_import")
    private LocalDateTime dateImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    public Stock() {}

    public Stock(Long id, Double quantiteStock, LocalDate dateStock, String sourceFichier, LocalDateTime dateImport, ArticlePDR article) {
        this.id = id;
        this.quantiteStock = quantiteStock;
        this.dateStock = dateStock;
        this.sourceFichier = sourceFichier;
        this.dateImport = dateImport;
        this.article = article;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(Double quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public LocalDate getDateStock() {
        return dateStock;
    }

    public void setDateStock(LocalDate dateStock) {
        this.dateStock = dateStock;
    }

    public String getSourceFichier() {
        return sourceFichier;
    }

    public void setSourceFichier(String sourceFichier) {
        this.sourceFichier = sourceFichier;
    }

    public LocalDateTime getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDateTime dateImport) {
        this.dateImport = dateImport;
    }

    public ArticlePDR getArticle() {
        return article;
    }

    public void setArticle(ArticlePDR article) {
        this.article = article;
    }
}
