package com.ocp.pdr.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecteurFicheBOMTest {

    @Test
    void shouldCreateBomLinkedToSecteur() {
        Secteur secteur = new Secteur();
        secteur.setNom("Ammoniac");

        FicheBOM bom = new FicheBOM();
        bom.setSecteur(secteur);

        assertEquals("Ammoniac", bom.getSecteur().getNom());
    }
}
