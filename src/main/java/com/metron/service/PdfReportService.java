package com.metron.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.metron.dto.MonthlyReportDto;
import com.metron.model.entity.User;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PdfReportService {

    public byte[] generateMonthlyPdf(User user, MonthlyReportDto report) {
        Document document = new Document(PageSize.A4, 36, 36, 54, 54);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Colors
            Color brandNavy = new Color(15, 23, 42); // #0f172a
            Color brandTeal = new Color(13, 148, 136); // #0d9488
            Color lightGray = new Color(241, 245, 249); // #f1f5f9
            Color darkGray = new Color(51, 65, 85);

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, brandNavy);
            Font sloganFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, brandTeal);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, brandNavy);
            Font cellBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, brandNavy);
            Font cellRegular = FontFactory.getFont(FontFactory.HELVETICA, 10, darkGray);

            // 1. Header Banner
            Paragraph logoP = new Paragraph("METRON", titleFont);
            logoP.setAlignment(Element.ALIGN_CENTER);
            document.add(logoP);

            Paragraph sloganP = new Paragraph("Não desperdice seu tempo e seu dinheiro.", sloganFont);
            sloganP.setAlignment(Element.ALIGN_CENTER);
            sloganP.setSpacingAfter(15);
            document.add(sloganP);

            // Subtitle / User meta
            Paragraph reportInfo = new Paragraph(
                    String.format("Relatório Financeiro Mensal — %02d/%d\nTitular: %s (%s)",
                            report.getMonth(), report.getYear(), user.getName(), user.getEmail()),
                    FontFactory.getFont(FontFactory.HELVETICA, 11, darkGray)
            );
            reportInfo.setAlignment(Element.ALIGN_CENTER);
            reportInfo.setSpacingAfter(20);
            document.add(reportInfo);

            // 2. Financial Overview Table
            Paragraph sec1 = new Paragraph("1. Resumo Financeiro Consolidado", headerFont);
            sec1.setSpacingAfter(8);
            document.add(sec1);

            PdfPTable overviewTable = new PdfPTable(2);
            overviewTable.setWidthPercentage(100);
            overviewTable.setSpacingAfter(20);

            addTableRow(overviewTable, "Renda Total do Mês:", "R$ " + formatMoney(report.getTotalIncome()), cellBold, cellRegular, lightGray);
            addTableRow(overviewTable, "Despesas Realizadas:", "R$ " + formatMoney(report.getTotalExpenses()), cellBold, cellRegular, Color.WHITE);
            addTableRow(overviewTable, "Saldo Final do Mês:", "R$ " + formatMoney(report.getFinalBalance()), cellBold, cellRegular, lightGray);
            addTableRow(overviewTable, "Valor Efetivamente Poupado:", "R$ " + formatMoney(report.getSavedAmount()), cellBold, cellRegular, Color.WHITE);
            addTableRow(overviewTable, "Taxa de Poupança Alcançada:", report.getSavingsRatePercent().setScale(1, RoundingMode.HALF_UP) + "%", cellBold, cellRegular, lightGray);
            addTableRow(overviewTable, "Categoria com Maior Gasto:", report.getTopSpendingCategory() + " (R$ " + formatMoney(report.getTopSpendingCategoryAmount()) + ")", cellBold, cellRegular, Color.WHITE);
            addTableRow(overviewTable, "Variação vs Mês Anterior:", report.getMonthOverMonthExpenseDiffPercent().setScale(1, RoundingMode.HALF_UP) + "%", cellBold, cellRegular, lightGray);

            document.add(overviewTable);

            // 3. Weekly Breakdown
            Paragraph sec2 = new Paragraph("2. Evolução por Semana", headerFont);
            sec2.setSpacingAfter(8);
            document.add(sec2);

            PdfPTable weekTable = new PdfPTable(2);
            weekTable.setWidthPercentage(100);
            weekTable.setSpacingAfter(20);

            for (MonthlyReportDto.WeekSummary ws : report.getWeeklyComparison()) {
                addTableRow(weekTable, ws.getWeekLabel(), "R$ " + formatMoney(ws.getTotalExpense()), cellBold, cellRegular, lightGray);
            }
            document.add(weekTable);

            // 4. Goals and Objectives
            Paragraph sec3 = new Paragraph("3. Metas e Objetivos Financeiros", headerFont);
            sec3.setSpacingAfter(8);
            document.add(sec3);

            PdfPTable goalsTable = new PdfPTable(2);
            goalsTable.setWidthPercentage(100);
            goalsTable.setSpacingAfter(20);
            addTableRow(goalsTable, "Metas Alcançadas no Mês:", String.valueOf(report.getAchievedGoalsCount()), cellBold, cellRegular, lightGray);
            addTableRow(goalsTable, "Metas em Andamento:", String.valueOf(report.getPendingGoalsCount()), cellBold, cellRegular, Color.WHITE);
            document.add(goalsTable);

            // 5. Recommendations
            Paragraph sec4 = new Paragraph("4. Recomendações Estratégicas para o Próximo Mês", headerFont);
            sec4.setSpacingAfter(8);
            document.add(sec4);

            for (String rec : report.getRecommendationsForNextMonth()) {
                Paragraph p = new Paragraph("• " + rec, cellRegular);
                p.setSpacingAfter(5);
                document.add(p);
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do relatório mensal: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String col1, String col2, Font font1, Font font2, Color bgColor) {
        PdfPCell c1 = new PdfPCell(new Phrase(col1, font1));
        c1.setBackgroundColor(bgColor);
        c1.setPadding(6);
        c1.setBorderColor(new Color(226, 232, 240));

        PdfPCell c2 = new PdfPCell(new Phrase(col2, font2));
        c2.setBackgroundColor(bgColor);
        c2.setPadding(6);
        c2.setBorderColor(new Color(226, 232, 240));

        table.addCell(c1);
        table.addCell(c2);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0,00";
        return amount.setScale(2, RoundingMode.HALF_UP).toString().replace(".", ",");
    }
}
