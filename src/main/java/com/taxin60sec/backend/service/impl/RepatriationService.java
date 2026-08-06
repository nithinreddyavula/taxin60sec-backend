package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.dto.repatriation.RepatriationRecordRequest;
import com.taxin60sec.backend.dto.repatriation.RepatriationSummaryResponse;
import com.taxin60sec.backend.dto.repatriation.RepatriationSummaryResponse.RepatriationRecordView;
import com.taxin60sec.backend.entity.RepatriationRecord;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.repository.RepatriationRecordRepository;
import com.taxin60sec.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class RepatriationService {

    private static final String DISCLAIMER =
            "This is a tracking tool only, not legal or tax advice. Actual applicable repatriation " +
            "limits vary by transaction type and individual circumstances - confirm your specific " +
            "limit and eligibility with your assigned CA before relying on these figures.";

    private final RepatriationRecordRepository repatriationRepository;
    private final UserRepository userRepository;

    public RepatriationService(
            RepatriationRecordRepository repatriationRepository,
            UserRepository userRepository
    ) {
        this.repatriationRepository = repatriationRepository;
        this.userRepository = userRepository;
    }

    public RepatriationSummaryResponse summaryFor(Long clientId) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));

        String currentFy = currentFinancialYear(LocalDate.now());

        List<RepatriationRecord> records =
                repatriationRepository.findByClientIdAndDeletedFalseOrderByTransactionDateDesc(clientId)
                        .stream()
                        .filter(r -> currentFy.equals(financialYearFor(r.getTransactionDate())))
                        .toList();

        BigDecimal used = records.stream()
                .map(RepatriationRecord::getAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal limit = client.getRepatriationLimitUsd();
        BigDecimal remaining = limit != null ? limit.subtract(used) : null;

        List<RepatriationRecordView> views = records.stream()
                .map(r -> new RepatriationRecordView(
                        r.getId(),
                        r.getAmountUsd(),
                        r.getTransactionDate(),
                        r.getPurpose(),
                        r.isForm15caFiled()
                ))
                .toList();

        return new RepatriationSummaryResponse(currentFy, limit, used, remaining, views, DISCLAIMER);
    }

    @Transactional
    public RepatriationSummaryResponse addRecord(Long clientId, RepatriationRecordRequest request) {

        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));

        RepatriationRecord record = new RepatriationRecord();
        record.setClient(client);
        record.setAmountUsd(request.amountUsd());
        record.setTransactionDate(request.transactionDate());
        record.setPurpose(request.purpose());
        record.setForm15caFiled(request.form15caFiled());

        repatriationRepository.save(record);

        return summaryFor(clientId);
    }

    private String currentFinancialYear(LocalDate date) {
        return financialYearFor(date);
    }

    private String financialYearFor(LocalDate date) {
        int startYear = date.getMonthValue() >= 4 ? date.getYear() : date.getYear() - 1;
        int endYearShort = (startYear + 1) % 100;
        return startYear + "-" + String.format("%02d", endYearShort);
    }
}