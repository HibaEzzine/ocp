package com.ocp.pdr.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ocp.pdr.dto.DemandeAchatStatisticsDto;
import com.ocp.pdr.model.DemandeAchat;
import com.ocp.pdr.model.enums.ResponsableDA;
import com.ocp.pdr.model.enums.SecteurDA;
import com.ocp.pdr.model.enums.StatutDA;
import com.ocp.pdr.model.enums.UdmDA;
import com.ocp.pdr.repository.DemandeAchatRepository;

@ExtendWith(MockitoExtension.class)
class DemandeAchatServiceTest {

    @Mock
    private DemandeAchatRepository repo;

    @InjectMocks
    private DemandeAchatService service;

    private DemandeAchat da;

    @BeforeEach
    void setUp() {
        da = DemandeAchat.builder()
                .id(1L)
                .numeroDA("DA-1001")
                .numeroDossier("DOS-501")
                .codeSAP("SAP-900")
                .description("Roulement à billes SKF")
                .quantite(10.0)
                .uniteDeMesure(UdmDA.U)
                .secteur(SecteurDA.AMMONIAC)
                .statut(StatutDA.EN_COURS)
                .responsable(ResponsableDA.ATTOUCHI)
                .delaiLivraison(15)
                .pourcentageLivraison(50)
                .build();
    }

    @Test
    void testFindById() {
        when(repo.findById(1L)).thenReturn(Optional.of(da));
        Optional<DemandeAchat> result = service.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("DA-1001", result.get().getNumeroDA());
    }

    @Test
    void testSave() {
        when(repo.findById(1L)).thenReturn(Optional.of(da));
        when(repo.save(any(DemandeAchat.class))).thenReturn(da);
        DemandeAchat saved = service.save(da);
        assertNotNull(saved);
        assertEquals("DA-1001", saved.getNumeroDA());
        verify(repo, times(1)).save(any(DemandeAchat.class));
    }

    @Test
    void testSaveNew() {
        DemandeAchat newDa = DemandeAchat.builder()
                .numeroDA("DA-1002")
                .quantite(5.0)
                .secteur(SecteurDA.AMMONIAC)
                .statut(StatutDA.EN_COURS)
                .responsable(ResponsableDA.ATTOUCHI)
                .build();
        when(repo.save(any(DemandeAchat.class))).thenReturn(newDa);
        DemandeAchat saved = service.save(newDa);
        assertNotNull(saved);
        assertEquals("DA-1002", saved.getNumeroDA());
        verify(repo, times(1)).save(any(DemandeAchat.class));
    }

    @Test
    void testGetStatistics() {
        when(repo.findAll()).thenReturn(List.of(da));
        DemandeAchatStatisticsDto stats = service.getStatistics();
        assertNotNull(stats);
        assertEquals(1, stats.getTotalReportings());
        assertEquals(10.0, stats.getTotalQuantite());
    }
}
