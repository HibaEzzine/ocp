package com.ocp.pdr.service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.ocp.pdr.dto.response.ExcelValidationResult;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.ocp.pdr.repository.BacklogOTRepository;
import com.ocp.pdr.repository.BesoinEnCoursRepository;
import com.ocp.pdr.repository.ConsommationRepository;
import com.ocp.pdr.repository.FicheBOMRepository;
import com.ocp.pdr.repository.HistoriqueTraitementRepository;
import com.ocp.pdr.repository.ImportDonneesRepository;
import com.ocp.pdr.repository.SecteurRepository;
import com.ocp.pdr.repository.StockRepository;
import com.ocp.pdr.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class ExcelImportServiceValidationTest {

    @Mock private ArticlePDRRepository articlePDRRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private StockRepository stockRepository;
    @Mock private BacklogOTRepository backlogOTRepository;
    @Mock private FicheBOMRepository bomRepository;
    @Mock private ConsommationRepository consommationRepository;
    @Mock private BesoinEnCoursRepository besoinRepository;
    @Mock private SecteurRepository secteurRepository;
    @Mock private ImportDonneesRepository importDonneesRepository;
    @Mock private HistoriqueTraitementRepository historiqueTraitementRepository;

    @InjectMocks
    private ExcelImportService excelImportService;

    @Test
    void shouldRejectUnsupportedFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sample.csv",
            "text/csv",
            "code,sale\nA1,10\n".getBytes()
        );

        ExcelValidationResult result = excelImportService.validateExcelFile(file);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("xlsx");
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );

        ExcelValidationResult result = excelImportService.validateExcelFile(file);

        assertThat(result.valid()).isFalse();
        assertThat(result.message()).contains("vide");
    }

    @Test
    void shouldRequireExpectedColumnsWhenHeaderIsPresent() throws Exception {
        byte[] workbookBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            workbook.createSheet().createRow(0).createCell(0).setCellValue("DESCRIPTION");
            workbook.write(output);
            workbookBytes = output.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sample.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            workbookBytes
        );

        ExcelValidationResult result = excelImportService.validateExcelFile(file, List.of("CODE_SAP", "DATE", "QUANTITE"));

        assertThat(result.valid()).isFalse();
        assertThat(result.missingColumns()).contains("CODE_SAP");
    }

    @Test
    void shouldUseLegacyImportForSmallXlsxFiles() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "sample.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "small content".getBytes()
        );

        Method method = ExcelImportService.class.getDeclaredMethod("shouldUseStreamingImport", MultipartFile.class);
        method.setAccessible(true);

        boolean useStreaming = (boolean) method.invoke(excelImportService, file);

        assertThat(useStreaming).isFalse();
    }

    @Test
    void shouldPreferStreamingImportForMediumXlsxFiles() throws Exception {
        byte[] payload = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "medium.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            payload
        );

        Method method = ExcelImportService.class.getDeclaredMethod("shouldUseStreamingImport", MultipartFile.class);
        method.setAccessible(true);

        boolean useStreaming = (boolean) method.invoke(excelImportService, file);

        assertThat(useStreaming).isTrue();
    }

    @Test
    void shouldPreferStreamingImportForLargeXlsFiles() throws Exception {
        byte[] payload = new byte[11 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.xls",
            "application/vnd.ms-excel",
            payload
        );

        Method method = ExcelImportService.class.getDeclaredMethod("shouldUseStreamingImport", MultipartFile.class);
        method.setAccessible(true);

        boolean useStreaming = (boolean) method.invoke(excelImportService, file);

        assertThat(useStreaming).isTrue();
    }
}
