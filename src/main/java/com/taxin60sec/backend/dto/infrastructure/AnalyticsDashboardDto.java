package com.taxin60sec.backend.dto.infrastructure;
import java.math.BigDecimal; import java.util.Map;
public record AnalyticsDashboardDto(long cases,long users,long services,long completedCases,long pendingDocuments,BigDecimal revenue,double completionRate,double averageProcessingDays,Map<String,Long> casesByStage,Map<String,Long> caPerformance){}
