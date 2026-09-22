package com.ocp.pdr.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ocp.pdr.model.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "prenom")
    private String prenom;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ADMINISTRATEUR;

    @Column(name = "actif")
    private Boolean actif = true;

    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<ImportDonnees> imports;

    @JsonIgnore
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<DemandeAchat> demandesAchat;

    public Utilisateur() {}

    public Utilisateur(Long id, String nom, String prenom, String email, String motDePasse, Role role, Boolean actif,
                       List<ImportDonnees> imports, List<DemandeAchat> demandesAchat) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role != null ? role : Role.ADMINISTRATEUR;
        this.actif = actif != null ? actif : true;
        this.imports = imports;
        this.demandesAchat = demandesAchat;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String motDePasse;
        private Role role = Role.ADMINISTRATEUR;
        private Boolean actif = true;
        private List<ImportDonnees> imports;
        private List<DemandeAchat> demandesAchat;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nom(String nom) { this.nom = nom; return this; }
        public Builder prenom(String prenom) { this.prenom = prenom; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder motDePasse(String motDePasse) { this.motDePasse = motDePasse; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder actif(Boolean actif) { this.actif = actif; return this; }
        public Builder imports(List<ImportDonnees> imports) { this.imports = imports; return this; }
        public Builder demandesAchat(List<DemandeAchat> demandesAchat) { this.demandesAchat = demandesAchat; return this; }

        public Utilisateur build() {
            return new Utilisateur(id, nom, prenom, email, motDePasse, role, actif, imports, demandesAchat);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public List<ImportDonnees> getImports() { return imports; }
    public void setImports(List<ImportDonnees> imports) { this.imports = imports; }

    public List<DemandeAchat> getDemandesAchat() { return demandesAchat; }
    public void setDemandesAchat(List<DemandeAchat> demandesAchat) { this.demandesAchat = demandesAchat; }
}
