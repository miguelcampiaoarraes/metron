package com.metron.controller;

import com.metron.dto.MonthlyReportDto;
import com.metron.dto.WeeklyEvaluationDto;
import com.metron.model.entity.User;
import com.metron.security.CurrentUserContext;
import com.metron.service.MonthlyReportService;
import com.metron.service.PdfReportService;
import com.metron.service.WeeklyEvaluationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final WeeklyEvaluationService weeklyEvaluationService;
    private final MonthlyReportService monthlyReportService;
    private final PdfReportService pdfReportService;
    private final CurrentUserContext userContext;

    public ReportController(WeeklyEvaluationService weeklyEvaluationService,
                            MonthlyReportService monthlyReportService,
                            PdfReportService pdfReportService,
                            CurrentUserContext userContext) {
        this.weeklyEvaluationService = weeklyEvaluationService;
        this.monthlyReportService = monthlyReportService;
        this.pdfReportService = pdfReportService;
        this.userContext = userContext;
    }

    @GetMapping("/weekly")
    public ResponseEntity<WeeklyEvaluationDto> getWeeklyEvaluation() {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(weeklyEvaluationService.evaluateCurrentWeek(user));
    }

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportDto> getMonthlyReport(@PathVariable Integer year, @PathVariable Integer month) {
        User user = userContext.getCurrentUser();
        return ResponseEntity.ok(monthlyReportService.generateMonthlyReport(user, month, year));
    }

    @GetMapping("/monthly/{year}/{month}/pdf")
    public ResponseEntity<byte[]> getMonthlyReportPdf(@PathVariable Integer year, @PathVariable Integer month) {
        User user = userContext.getCurrentUser();
        MonthlyReportDto report = monthlyReportService.generateMonthlyReport(user, month, year);
        byte[] pdfBytes = pdfReportService.generateMonthlyPdf(user, report);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", String.format("metron-relatorio-%02d-%d.pdf", month, year));

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
