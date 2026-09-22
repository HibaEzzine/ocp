package com.ocp.pdr.model;

import java.time.LocalDateTime;
import java.util.List;

import com.ocp.pdr.model.enums.StatutImport;
import com.ocp.pdr.model.enums.TypeFichier;

import jakarta.persistence.*;

@Entity
@Table(name = "import_donnees")
public class ImportDonnees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_fichier")
    private TypeFichier typeFichier;

    @Column(name = "date_import")
    private LocalDateTime dateImport;

    @Column(name = "nombre_lignes")
    private Integer nombreLignes;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutImport statut;

    @Column(name = "message_resultat", columnDefinition = "TEXT")
    private String messageResultat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "importDonnees", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueTraitement> historiqueTraitements;

    public ImportDonnees() {}

    public ImportDonnees(Long id, String nomFichier, TypeFichier typeFichier, LocalDateTime dateImport, Integer nombreLignes, StatutImport statut, String messageResultat, Utilisateur utilisateur, List<HistoriqueTraitement> historiqueTraitements) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.typeFichier = typeFichier;
        this.dateImport = dateImport;
        this.nombreLignes = nombreLignes;
        this.statut = statut;
        this.messageResultat = messageResultat;
        this.utilisateur = utilisateur;
        this.historiqueTraitements = historiqueTraitements;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public TypeFichier getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(TypeFichier typeFichier) {
        this.typeFichier = typeFichier;
    }

    public LocalDateTime getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDateTime dateImport) {
        this.dateImport = dateImport;
    }

    public Integer getNombreLignes() {
        return nombreLignes;
    }

    public void setNombreLignes(Integer nombreLignes) {
        this.nombreLignes = nombreLignes;
    }

    public StatutImport getStatut() {
        return statut;
    }

    public void setStatut(StatutImport statut) {
        this.statut = statut;
    }

    public String getMessageResultat() {
        return messageResultat;
    }

    public void setMessageResultat(String messageResultat) {
        this.messageResultat = messageResultat;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public List<HistoriqueTraitement> getHistoriqueTraitements() {
        return historiqueTraitements;
    }

    public void setHistoriqueTraitements(List<HistoriqueTraitement> historiqueTraitements) {
        this.historiqueTraitements = historiqueTraitements;
    }
}
