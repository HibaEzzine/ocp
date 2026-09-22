package com.ocp.pdr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_traitement")
public class HistoriqueTraitement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation")
    private String operation;

    @Column(name = "date_operation")
    private LocalDateTime dateOperation;

    @Column(name = "statut")
    private String statut;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id")
    private ImportDonnees importDonnees;

    public HistoriqueTraitement() {}

    public HistoriqueTraitement(Long id, String operation, LocalDateTime dateOperation, String statut, String message, ImportDonnees importDonnees) {
        this.id = id;
        this.operation = operation;
        this.dateOperation = dateOperation;
        this.statut = statut;
        this.message = message;
        this.importDonnees = importDonnees;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public LocalDateTime getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(LocalDateTime dateOperation) {
        this.dateOperation = dateOperation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ImportDonnees getImportDonnees() {
        return importDonnees;
    }

    public void setImportDonnees(ImportDonnees importDonnees) {
        this.importDonnees = importDonnees;
    }
}
