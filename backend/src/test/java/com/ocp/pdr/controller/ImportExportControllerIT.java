package com.ocp.pdr.controller;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser
@DisplayName("Tests d'intégration ImportExportController")
class ImportExportControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/excel/export/min-max retourne un fichier Excel")
    void exportMinMax_shouldReturnExcel() throws Exception {
        mockMvc.perform(get("/api/excel/export/min-max"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("regle_min_max_")))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/excel/export/planifie retourne un fichier Excel")
    void exportPlanifie_shouldReturnExcel() throws Exception {
        mockMvc.perform(get("/api/excel/export/planifie"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("regle_mode_planifie_")))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/excel/export/sur-demande retourne un fichier Excel")
    void exportSurDemande_shouldReturnExcel() throws Exception {
        mockMvc.perform(get("/api/excel/export/sur-demande"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("regle_sur_demande_")))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("GET /api/excel/export/anomalies retourne un fichier Excel")
    void exportAnomalies_shouldReturnExcel() throws Exception {
        mockMvc.perform(get("/api/excel/export/anomalies"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("anomalies_")))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("POST /api/excel/import-multi sans fichier retourne 400")
    void importMulti_withoutFiles_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/excel/import-multi"))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.success", org.hamcrest.Matchers.is(false)));
    }
}
