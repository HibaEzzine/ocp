package com.ocp.pdr.controller;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ocp.pdr.dto.DemandeAchatDto;
import com.ocp.pdr.dto.DemandeAchatStatisticsDto;
import com.ocp.pdr.dto.HistoriqueDADto;
import com.ocp.pdr.dto.ImportResultDto;
import com.ocp.pdr.mapper.DemandeAchatMapper;
import com.ocp.pdr.mapper.HistoriqueDAMapper;
import com.ocp.pdr.model.DemandeAchat;
import com.ocp.pdr.model.enums.ResponsableDA;
import com.ocp.pdr.model.enums.SecteurDA;
import com.ocp.pdr.model.enums.StatutDA;
import com.ocp.pdr.service.DemandeAchatService;
import com.ocp.pdr.service.HistoriqueDAService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping({"/api/demandes", "/api/reportings"})
@RequiredArgsConstructor
@Slf4j
public class DemandeAchatController {

    private final DemandeAchatService service;
    private final DemandeAchatMapper mapper;
    private final HistoriqueDAService historiqueService;
    private final HistoriqueDAMapper historiqueMapper;

    @GetMapping
    public List<DemandeAchatDto> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatutDA statut,
            @RequestParam(required = false) SecteurDA secteur,
            @RequestParam(required = false) ResponsableDA responsable,
            @RequestParam(required = false) String fournisseur,
            @RequestParam(required = false) String commande,
            @RequestParam(required = false) String sort) {
        return service.findAll(search, statut, secteur, responsable, fournisseur, commande, sort)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatutDA statut,
            @RequestParam(required = false) SecteurDA secteur,
            @RequestParam(required = false) ResponsableDA responsable,
            @RequestParam(required = false) String fournisseur,
            @RequestParam(required = false) String commande,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "xlsx") String format) throws IOException {

        if ("csv".equalsIgnoreCase(format)) {
            String csv = service.export(search, statut, secteur, responsable, fournisseur, commande, sort);
            byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] csvBytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] payload = new byte[bom.length + csvBytes.length];
            System.arraycopy(bom, 0, payload, 0, bom.length);
            System.arraycopy(csvBytes, 0, payload, bom.length, csvBytes.length);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Content-Disposition", "attachment; filename=demandes_achat.csv")
                    .body(payload);
        } else {
            byte[] excelFile = service.exportToExcel(search, statut, secteur, responsable, fournisseur, commande, sort);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header("Content-Disposition", "attachment; filename=demandes_achat.xlsx")
                    .body(excelFile);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeAchatDto> get(@PathVariable Long id) {
        return service.findById(id).map(mapper::toDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DemandeAchatDto> create(@Valid @RequestBody DemandeAchatDto dto) {
        DemandeAchat saved = service.save(mapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/api/demandes/" + saved.getId())).body(mapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandeAchatDto> update(@PathVariable Long id, @Valid @RequestBody DemandeAchatDto dto) {
        dto.setId(id);
        DemandeAchat saved = service.save(mapper.toEntity(dto));
        return ResponseEntity.ok(mapper.toDto(saved));
    }

    @GetMapping("/statistiques")
    public DemandeAchatStatisticsDto statistics() {
        return service.statistics();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportResultDto result = service.importFromExcel(file.getInputStream());
            if (result.getErrors() == null || result.getErrors().isEmpty()) {
                return ResponseEntity.ok(result.getImportedCount() + " demandes d'achat importées avec succès.");
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Impossible de lire le fichier Excel.");
        } catch (Exception e) {
            log.error("Erreur lors de l'import Excel DA", e);
            return ResponseEntity.status(500).body("Erreur interne : " + e.getMessage());
        }
    }

    @GetMapping("/{id}/historiques")
    public List<HistoriqueDADto> history(@PathVariable Long id) {
        return historiqueService.findByDemandeAchatId(id).stream().map(historiqueMapper::toDto).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
