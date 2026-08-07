package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.repository.CaseRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CaseExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String[] HEADERS = {
            "Case ID", "Case Number", "Client Name", "Client Email", "Client Phone",
            "Service", "Status", "Intake Completed", "Assigned CA", "Created On"
    };

    private final CaseRepository caseRepository;

    public CaseExcelExportService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public byte[] exportCases() {
        List<Case> cases = caseRepository.findAllByOrderByCreatedAtDesc();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Cases");

            CellStyle headerStyle = headerStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Case taxCase : cases) {
                Row row = sheet.createRow(rowIndex++);
                int col = 0;
                row.createCell(col++).setCellValue(taxCase.getId());
                row.createCell(col++).setCellValue(nullToEmpty(taxCase.getCaseNumber()));
                row.createCell(col++).setCellValue(taxCase.getClient() != null ? nullToEmpty(taxCase.getClient().getFullName()) : "");
                row.createCell(col++).setCellValue(taxCase.getClient() != null ? nullToEmpty(taxCase.getClient().getEmail()) : "");
                row.createCell(col++).setCellValue(taxCase.getClient() != null ? nullToEmpty(taxCase.getClient().getPhoneNumber()) : "");
                row.createCell(col++).setCellValue(taxCase.getServiceOffering() != null ? nullToEmpty(taxCase.getServiceOffering().getDisplayName()) : "");
                row.createCell(col++).setCellValue(taxCase.getStatus().name());
                row.createCell(col++).setCellValue(taxCase.isIntakeCompleted() ? "Yes" : "No");
                row.createCell(col++).setCellValue(taxCase.getAssignedCa() != null ? nullToEmpty(taxCase.getAssignedCa().getFullName()) : "Unassigned");
                row.createCell(col).setCellValue(taxCase.getCreatedAt() == null ? "" :
                        DATE_FORMAT.format(taxCase.getCreatedAt().atZone(ZoneId.systemDefault())));
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate cases Excel export", ex);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}