package com.ocp.pdr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "besoin_en_cours")
public class BesoinEnCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_besoin")
    private Double quantiteBesoin;

    @Column(name = "date_besoin")
    private LocalDate dateBesoin;

    @Column(name = "statut")
    private String statut;

    @Column(name = "source_fichier")
    private String sourceFichier;

    @Column(name = "date_import")
    private LocalDateTime dateImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    public BesoinEnCours() {}

    public BesoinEnCours(Long id, Double quantiteBesoin, LocalDate dateBesoin, String statut, String sourceFichier, LocalDateTime dateImport, ArticlePDR article) {
        this.id = id;
        this.quantiteBesoin = quantiteBesoin;
        this.dateBesoin = dateBesoin;
        this.statut = statut;
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

    public Double getQuantiteBesoin() {
        return quantiteBesoin;
    }

    public void setQuantiteBesoin(Double quantiteBesoin) {
        this.quantiteBesoin = quantiteBesoin;
    }

    public LocalDate getDateBesoin() {
        return dateBesoin;
    }

    public void setDateBesoin(LocalDate dateBesoin) {
        this.dateBesoin = dateBesoin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
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
