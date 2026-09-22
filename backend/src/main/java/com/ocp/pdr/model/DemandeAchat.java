package com.ocp.pdr.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ocp.pdr.model.enums.ResponsableDA;
import com.ocp.pdr.model.enums.SecteurDA;
import com.ocp.pdr.model.enums.StatutDA;
import com.ocp.pdr.model.enums.UdmDA;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "demande_achat")
public class DemandeAchat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_da")
    private String numeroDA;

    @Column(name = "numero_dossier")
    private String numeroDossier;

    @Column(name = "numero")
    private String numero;

    @Column(name = "code_oracle")
    private String codeOracle;

    @Column(name = "code_sap")
    private String codeSAP;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "unite_de_mesure")
    private UdmDA uniteDeMesure;

    @Column(name = "quantite")
    private Double quantite;

    @Enumerated(EnumType.STRING)
    @Column(name = "secteur")
    private SecteurDA secteur;

    @Column(name = "commande")
    private String commande;

    @Column(name = "fournisseur")
    private String fournisseur;

    @Column(name = "pourcentage_livraison")
    private Integer pourcentageLivraison;

    @Column(name = "delai_livraison")
    private Integer delaiLivraison;

    @Column(name = "date_notification")
    private LocalDate dateNotification;

    @Column(name = "date_previsionnelle")
    private LocalDate datePrevisionnelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutDA statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsable")
    private ResponsableDA responsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @JsonIgnore
    @OneToMany(mappedBy = "demandeAchat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueDA> historiques = new ArrayList<>();

    public DemandeAchat() {}

    public DemandeAchat(Long id, String numeroDA, String numeroDossier, String numero, String codeOracle,
                        String codeSAP, String description, UdmDA uniteDeMesure, Double quantite,
                        SecteurDA secteur, String commande, String fournisseur, Integer pourcentageLivraison,
                        Integer delaiLivraison, LocalDate dateNotification, LocalDate datePrevisionnelle,
                        StatutDA statut, ResponsableDA responsable, Utilisateur utilisateur, String commentaire,
                        LocalDateTime dateCreation, LocalDateTime dateModification, List<HistoriqueDA> historiques) {
        this.id = id;
        this.numeroDA = numeroDA;
        this.numeroDossier = numeroDossier;
        this.numero = numero;
        this.codeOracle = codeOracle;
        this.codeSAP = codeSAP;
        this.description = description;
        this.uniteDeMesure = uniteDeMesure;
        this.quantite = quantite;
        this.secteur = secteur;
        this.commande = commande;
        this.fournisseur = fournisseur;
        this.pourcentageLivraison = pourcentageLivraison;
        this.delaiLivraison = delaiLivraison;
        this.dateNotification = dateNotification;
        this.datePrevisionnelle = datePrevisionnelle;
        this.statut = statut;
        this.responsable = responsable;
        this.utilisateur = utilisateur;
        this.commentaire = commentaire;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.historiques = historiques != null ? historiques : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String numeroDA;
        private String numeroDossier;
        private String numero;
        private String codeOracle;
        private String codeSAP;
        private String description;
        private UdmDA uniteDeMesure;
        private Double quantite;
        private SecteurDA secteur;
        private String commande;
        private String fournisseur;
        private Integer pourcentageLivraison;
        private Integer delaiLivraison;
        private LocalDate dateNotification;
        private LocalDate datePrevisionnelle;
        private StatutDA statut;
        private ResponsableDA responsable;
        private Utilisateur utilisateur;
        private String commentaire;
        private LocalDateTime dateCreation;
        private LocalDateTime dateModification;
        private List<HistoriqueDA> historiques = new ArrayList<>();

        public Builder id(Long id) { this.id = id; return this; }
        public Builder numeroDA(String numeroDA) { this.numeroDA = numeroDA; return this; }
        public Builder numeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; return this; }
        public Builder numero(String numero) { this.numero = numero; return this; }
        public Builder codeOracle(String codeOracle) { this.codeOracle = codeOracle; return this; }
        public Builder codeSAP(String codeSAP) { this.codeSAP = codeSAP; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder uniteDeMesure(UdmDA uniteDeMesure) { this.uniteDeMesure = uniteDeMesure; return this; }
        public Builder quantite(Double quantite) { this.quantite = quantite; return this; }
        public Builder secteur(SecteurDA secteur) { this.secteur = secteur; return this; }
        public Builder commande(String commande) { this.commande = commande; return this; }
        public Builder fournisseur(String fournisseur) { this.fournisseur = fournisseur; return this; }
        public Builder pourcentageLivraison(Integer pourcentageLivraison) { this.pourcentageLivraison = pourcentageLivraison; return this; }
        public Builder delaiLivraison(Integer delaiLivraison) { this.delaiLivraison = delaiLivraison; return this; }
        public Builder dateNotification(LocalDate dateNotification) { this.dateNotification = dateNotification; return this; }
        public Builder datePrevisionnelle(LocalDate datePrevisionnelle) { this.datePrevisionnelle = datePrevisionnelle; return this; }
        public Builder statut(StatutDA statut) { this.statut = statut; return this; }
        public Builder responsable(ResponsableDA responsable) { this.responsable = responsable; return this; }
        public Builder utilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; return this; }
        public Builder commentaire(String commentaire) { this.commentaire = commentaire; return this; }
        public Builder dateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; return this; }
        public Builder dateModification(LocalDateTime dateModification) { this.dateModification = dateModification; return this; }
        public Builder historiques(List<HistoriqueDA> historiques) { this.historiques = historiques; return this; }

        public DemandeAchat build() {
            return new DemandeAchat(id, numeroDA, numeroDossier, numero, codeOracle, codeSAP, description,
                    uniteDeMesure, quantite, secteur, commande, fournisseur, pourcentageLivraison, delaiLivraison,
                    dateNotification, datePrevisionnelle, statut, responsable, utilisateur, commentaire,
                    dateCreation, dateModification, historiques);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroDA() { return numeroDA; }
    public void setNumeroDA(String numeroDA) { this.numeroDA = numeroDA; }

    public String getNumeroDossier() { return numeroDossier; }
    public void setNumeroDossier(String numeroDossier) { this.numeroDossier = numeroDossier; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getCodeOracle() { return codeOracle; }
    public void setCodeOracle(String codeOracle) { this.codeOracle = codeOracle; }

    public String getCodeSAP() { return codeSAP; }
    public void setCodeSAP(String codeSAP) { this.codeSAP = codeSAP; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UdmDA getUniteDeMesure() { return uniteDeMesure; }
    public void setUniteDeMesure(UdmDA uniteDeMesure) { this.uniteDeMesure = uniteDeMesure; }

    public Double getQuantite() { return quantite; }
    public void setQuantite(Double quantite) { this.quantite = quantite; }

    public SecteurDA getSecteur() { return secteur; }
    public void setSecteur(SecteurDA secteur) { this.secteur = secteur; }

    public String getCommande() { return commande; }
    public void setCommande(String commande) { this.commande = commande; }

    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }

    public Integer getPourcentageLivraison() { return pourcentageLivraison; }
    public void setPourcentageLivraison(Integer pourcentageLivraison) { this.pourcentageLivraison = pourcentageLivraison; }

    public Integer getDelaiLivraison() { return delaiLivraison; }
    public void setDelaiLivraison(Integer delaiLivraison) { this.delaiLivraison = delaiLivraison; }

    public LocalDate getDateNotification() { return dateNotification; }
    public void setDateNotification(LocalDate dateNotification) { this.dateNotification = dateNotification; }

    public LocalDate getDatePrevisionnelle() { return datePrevisionnelle; }
    public void setDatePrevisionnelle(LocalDate datePrevisionnelle) { this.datePrevisionnelle = datePrevisionnelle; }

    public StatutDA getStatut() { return statut; }
    public void setStatut(StatutDA statut) { this.statut = statut; }

    public ResponsableDA getResponsable() { return responsable; }
    public void setResponsable(ResponsableDA responsable) { this.responsable = responsable; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

    public List<HistoriqueDA> getHistoriques() { return historiques; }
    public void setHistoriques(List<HistoriqueDA> historiques) { this.historiques = historiques; }
}
