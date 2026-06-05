package com.piedrazul.reports.controller;

import com.piedrazul.reports.service.ReportsService;
import com.piedrazul.reports.model.ReportRequest;
import com.piedrazul.reports.model.ReportResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<ReportResponse> getStatistics(@org.springframework.web.bind.annotation.ModelAttribute @jakarta.validation.Valid ReportRequest request) {
        ReportResponse resp = reportsService.generateStatistics(request);
        return ResponseEntity.ok(resp);
    }

}
