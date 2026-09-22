package com.ocp.pdr.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ocp.pdr.model.enums.GroupeHomogene;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "article_pdr")
public class ArticlePDR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_sap", unique = true, nullable = false)
    private String codeSAP;

    @Column(name = "code_oracle")
    private String codeOracle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference", columnDefinition = "TEXT")
    private String reference;

    @Column(name = "udm")
    private String udm;

    @Column(name = "quantite_installee")
    private Double quantiteInstallee;

    @Column(name = "categorie", columnDefinition = "TEXT")
    private String categorie;

    @Enumerated(EnumType.STRING)
    @Column(name = "groupe_homogene")
    private GroupeHomogene groupeHomogene;

    @Column(name = "seuil_min")
    private Double seuilMin;

    @Column(name = "seuil_max")
    private Double seuilMax;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultatApprovisionnement> resultatsApprovisionnement = new ArrayList<>();

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FicheBOM> boms = new ArrayList<>();

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BesoinEnCours> besoinsEnCours = new ArrayList<>();

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BacklogOT> backlogOTs = new ArrayList<>();

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Consommation> consommations = new ArrayList<>();

    @JsonIgnore
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnomalieConsommation> anomaliesConsommation = new ArrayList<>();

    public ArticlePDR() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodeSAP() { return codeSAP; }
    public void setCodeSAP(String codeSAP) { this.codeSAP = codeSAP; }

    public String getCodeOracle() { return codeOracle; }
    public void setCodeOracle(String codeOracle) { this.codeOracle = codeOracle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getUdm() { return udm; }
    public void setUdm(String udm) { this.udm = udm; }

    public Double getQuantiteInstallee() { return quantiteInstallee; }
    public void setQuantiteInstallee(Double quantiteInstallee) { this.quantiteInstallee = quantiteInstallee; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public GroupeHomogene getGroupeHomogene() { return groupeHomogene; }
    public void setGroupeHomogene(GroupeHomogene groupeHomogene) { this.groupeHomogene = groupeHomogene; }

    public Double getSeuilMin() { return seuilMin; }
    public void setSeuilMin(Double seuilMin) { this.seuilMin = seuilMin; }

    public Double getSeuilMax() { return seuilMax; }
    public void setSeuilMax(Double seuilMax) { this.seuilMax = seuilMax; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public List<Stock> getStocks() { return stocks; }
    public void setStocks(List<Stock> stocks) { this.stocks = stocks; }

    public List<ResultatApprovisionnement> getResultatsApprovisionnement() { return resultatsApprovisionnement; }
    public void setResultatsApprovisionnement(List<ResultatApprovisionnement> resultatsApprovisionnement) { this.resultatsApprovisionnement = resultatsApprovisionnement; }

    public List<FicheBOM> getBoms() { return boms; }
    public void setBoms(List<FicheBOM> boms) { this.boms = boms; }

    public List<BesoinEnCours> getBesoinsEnCours() { return besoinsEnCours; }
    public void setBesoinsEnCours(List<BesoinEnCours> besoinsEnCours) { this.besoinsEnCours = besoinsEnCours; }

    public List<BacklogOT> getBacklogOTs() { return backlogOTs; }
    public void setBacklogOTs(List<BacklogOT> backlogOTs) { this.backlogOTs = backlogOTs; }

    public List<Consommation> getConsommations() { return consommations; }
    public void setConsommations(List<Consommation> consommations) { this.consommations = consommations; }

    public List<AnomalieConsommation> getAnomaliesConsommation() { return anomaliesConsommation; }
    public void setAnomaliesConsommation(List<AnomalieConsommation> anomaliesConsommation) { this.anomaliesConsommation = anomaliesConsommation; }
}
