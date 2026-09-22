package com.ocp.pdr.model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "secteur")
public class Secteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, unique = true)
    private String nom;

    @Column(name = "code")
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "secteur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FicheBOM> boms;

    public Secteur() {}

    public Secteur(Long id, String nom, String code, String description, List<FicheBOM> boms) {
        this.id = id;
        this.nom = nom;
        this.code = code;
        this.description = description;
        this.boms = boms;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<FicheBOM> getBoms() {
        return boms;
    }

    public void setBoms(List<FicheBOM> boms) {
        this.boms = boms;
    }
}
