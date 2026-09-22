package com.ocp.pdr.model;

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
@Table(name = "historique_da")
public class HistoriqueDA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action")
    private String action;

    @Column(name = "ancienne_valeur", length = 2000)
    private String ancienneValeur;

    @Column(name = "nouvelle_valeur", length = 2000)
    private String nouvelleValeur;

    @Column(name = "date_action")
    private LocalDateTime dateAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_achat_id", nullable = false)
    private DemandeAchat demandeAchat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    public HistoriqueDA() {}

    public HistoriqueDA(Long id, String action, String ancienneValeur, String nouvelleValeur,
                        LocalDateTime dateAction, DemandeAchat demandeAchat, Utilisateur utilisateur) {
        this.id = id;
        this.action = action;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.dateAction = dateAction;
        this.demandeAchat = demandeAchat;
        this.utilisateur = utilisateur;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String action;
        private String ancienneValeur;
        private String nouvelleValeur;
        private LocalDateTime dateAction;
        private DemandeAchat demandeAchat;
        private Utilisateur utilisateur;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder ancienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; return this; }
        public Builder nouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; return this; }
        public Builder dateAction(LocalDateTime dateAction) { this.dateAction = dateAction; return this; }
        public Builder demandeAchat(DemandeAchat demandeAchat) { this.demandeAchat = demandeAchat; return this; }
        public Builder utilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; return this; }

        public HistoriqueDA build() {
            return new HistoriqueDA(id, action, ancienneValeur, nouvelleValeur, dateAction, demandeAchat, utilisateur);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }

    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public DemandeAchat getDemandeAchat() { return demandeAchat; }
    public void setDemandeAchat(DemandeAchat demandeAchat) { this.demandeAchat = demandeAchat; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
}
