package com.wms.entity;

import com.wms.entity.Report.ReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Report entity
 * Tests constructors, getters, setters, and enums
 */
class ReportTest {

    private Report report;
    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setClientId(1L);

        portfolio = new Portfolio();
        portfolio.setPortfolioId(1L);
        portfolio.setPortfolioName("Retirement Fund");
        portfolio.setTotalValue(new BigDecimal("100000.00"));
        portfolio.setClient(client);

        report = new Report();
        report.setReportId(1L);
        report.setPortfolio(portfolio);
        report.setReportTitle("Q4 2025 Performance Report");
        report.setReportType(ReportType.QUARTERLY);
        report.setReportDate(LocalDate.now());
        report.setPerformanceSummary("{\"totalValue\": 100000.00}");
        report.setNotes("Good performance this quarter");
        report.setGeneratedBy(1L);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create report with default constructor")
        void defaultConstructor() {
            Report r = new Report();
            assertNotNull(r);
            assertNotNull(r.getReportDate());
        }

        @Test
        @DisplayName("Should create report with parameterized constructor")
        void parameterizedConstructor() {
            Report r = new Report(portfolio, "Annual Report 2025", ReportType.ANNUAL);
            
            assertEquals(portfolio, r.getPortfolio());
            assertEquals("Annual Report 2025", r.getReportTitle());
            assertEquals(ReportType.ANNUAL, r.getReportType());
            assertNotNull(r.getReportDate());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set reportId")
        void reportId() {
            report.setReportId(100L);
            assertEquals(100L, report.getReportId());
        }

        @Test
        @DisplayName("Should get and set portfolio")
        void portfolioTest() {
            Portfolio newPortfolio = new Portfolio();
            newPortfolio.setPortfolioId(2L);
            report.setPortfolio(newPortfolio);
            assertEquals(2L, report.getPortfolio().getPortfolioId());
        }

        @Test
        @DisplayName("Should get and set reportDate")
        void reportDate() {
            LocalDate date = LocalDate.of(2025, 6, 30);
            report.setReportDate(date);
            assertEquals(date, report.getReportDate());
        }

        @Test
        @DisplayName("Should get and set reportType")
        void reportType() {
            report.setReportType(ReportType.TAX);
            assertEquals(ReportType.TAX, report.getReportType());
        }

        @Test
        @DisplayName("Should get and set performanceSummary")
        void performanceSummary() {
            report.setPerformanceSummary("{\"updated\": true}");
            assertEquals("{\"updated\": true}", report.getPerformanceSummary());
        }

        @Test
        @DisplayName("Should get and set reportTitle")
        void reportTitle() {
            report.setReportTitle("New Title");
            assertEquals("New Title", report.getReportTitle());
        }

        @Test
        @DisplayName("Should get and set notes")
        void notes() {
            report.setNotes("Updated notes");
            assertEquals("Updated notes", report.getNotes());
        }

        @Test
        @DisplayName("Should get and set generatedBy")
        void generatedBy() {
            report.setGeneratedBy(2L);
            assertEquals(2L, report.getGeneratedBy());
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have all expected ReportType values")
        void reportTypeValues() {
            assertEquals(6, ReportType.values().length);
            assertNotNull(ReportType.MONTHLY);
            assertNotNull(ReportType.QUARTERLY);
            assertNotNull(ReportType.ANNUAL);
            assertNotNull(ReportType.CUSTOM);
            assertNotNull(ReportType.PERFORMANCE);
            assertNotNull(ReportType.TAX);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return meaningful string representation")
        void toStringTest() {
            String result = report.toString();
            assertNotNull(result);
            assertTrue(result.contains("1"));
            assertTrue(result.contains("QUARTERLY"));
            assertTrue(result.contains("Q4 2025 Performance Report"));
        }
    }
}
