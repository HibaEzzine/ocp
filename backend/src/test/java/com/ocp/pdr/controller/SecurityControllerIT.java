package com.ocp.pdr.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exportRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/excel/export/anomalies"))
            .andExpect(status().isForbidden());
    }
}
