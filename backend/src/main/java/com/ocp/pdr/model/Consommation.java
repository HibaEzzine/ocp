package com.ocp.pdr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "consommation")
public class Consommation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantite_consommee")
    private Double quantiteConsommee;

    @Column(name = "date_consommation")
    private LocalDate dateConsommation;

    @Column(name = "numero_ot")
    private String numeroOT;

    @Column(name = "source_fichier")
    private String sourceFichier;

    @Column(name = "date_import")
    private LocalDateTime dateImport;

    @Column(name = "dataset_type")
    private String datasetType;

    @Column(name = "periode_import")
    private String periodeImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    public Consommation() {}

    public Consommation(Long id, Double quantiteConsommee, LocalDate dateConsommation, String numeroOT, String sourceFichier, LocalDateTime dateImport, ArticlePDR article) {
        this.id = id;
        this.quantiteConsommee = quantiteConsommee;
        this.dateConsommation = dateConsommation;
        this.numeroOT = numeroOT;
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

    public Double getQuantiteConsommee() {
        return quantiteConsommee;
    }

    public void setQuantiteConsommee(Double quantiteConsommee) {
        this.quantiteConsommee = quantiteConsommee;
    }

    public LocalDate getDateConsommation() {
        return dateConsommation;
    }

    public void setDateConsommation(LocalDate dateConsommation) {
        this.dateConsommation = dateConsommation;
    }

    public String getNumeroOT() {
        return numeroOT;
    }

    public void setNumeroOT(String numeroOT) {
        this.numeroOT = numeroOT;
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

    public String getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public String getPeriodeImport() {
        return periodeImport;
    }

    public void setPeriodeImport(String periodeImport) {
        this.periodeImport = periodeImport;
    }

    public ArticlePDR getArticle() {
        return article;
    }

    public void setArticle(ArticlePDR article) {
        this.article = article;
    }
}
