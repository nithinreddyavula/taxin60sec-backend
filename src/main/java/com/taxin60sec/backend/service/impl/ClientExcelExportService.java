package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.entity.ClientProfile;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.CaseRepository;
import com.taxin60sec.backend.repository.ClientProfileRepository;
import com.taxin60sec.backend.repository.UserRepository;
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
public class ClientExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String[] HEADERS = {
            "Client ID", "Full Name", "Email", "Phone Number", "Status",
            "Joined On", "Business Name", "PAN Number", "GSTIN", "Address",
            "Tier", "Referral Code", "Referred By Code", "Referral Credits", "Total Cases"
    };

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final CaseRepository caseRepository;

    public ClientExcelExportService(UserRepository userRepository, ClientProfileRepository clientProfileRepository, CaseRepository caseRepository) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.caseRepository = caseRepository;
    }

    public byte[] exportClients() {
        List<User> clients = userRepository.findByRoles_NameOrderByCreatedAtAsc("ROLE_CLIENT");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Clients");

            CellStyle headerStyle = headerStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (User user : clients) {
                ClientProfile profile = clientProfileRepository.findByUserId(user.getId()).orElse(null);
                long totalCases = caseRepository.findByClient_IdAndDeletedFalseOrderByCreatedAtDesc(user.getId()).size();

                Row row = sheet.createRow(rowIndex++);
                int col = 0;
                row.createCell(col++).setCellValue(user.getId());
                row.createCell(col++).setCellValue(nullToEmpty(user.getFullName()));
                row.createCell(col++).setCellValue(nullToEmpty(user.getEmail()));
                row.createCell(col++).setCellValue(nullToEmpty(user.getPhoneNumber()));
                row.createCell(col++).setCellValue(user.isActive() ? "Active" : "Inactive");
                row.createCell(col++).setCellValue(user.getCreatedAt() == null ? "" :
                        DATE_FORMAT.format(user.getCreatedAt().atZone(ZoneId.systemDefault())));
                row.createCell(col++).setCellValue(profile != null ? nullToEmpty(profile.getBusinessName()) : "");
                row.createCell(col++).setCellValue(profile != null ? nullToEmpty(profile.getPanNumber()) : "");
                row.createCell(col++).setCellValue(profile != null ? nullToEmpty(profile.getGstin()) : "");
                row.createCell(col++).setCellValue(profile != null ? nullToEmpty(profile.getAddress()) : "");
                row.createCell(col++).setCellValue(profile != null && profile.getTier() != null ? profile.getTier().name() : "");
                row.createCell(col++).setCellValue(nullToEmpty(user.getReferralCode()));
                row.createCell(col++).setCellValue(nullToEmpty(user.getReferredByCode()));
                row.createCell(col++).setCellValue(user.getReferralCredits());
                row.createCell(col).setCellValue(totalCases);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate clients Excel export", ex);
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