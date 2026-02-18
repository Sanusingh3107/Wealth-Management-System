package com.wealth.management.config;

import com.wms.entity.*;
import com.wms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * =============================================================================
 * DATA INITIALIZER - Seeds the Database with Sample Data
 * =============================================================================
 * 
 * INTERVIEW EXPLANATION:
 * ----------------------
 * This configuration class uses CommandLineRunner to initialize the database
 * with sample data when the application starts. This is very useful for:
 * 
 * 1. Development - Having test data ready without manual entry
 * 2. Demo purposes - Showing the application with realistic data
 * 3. Testing - Ensuring consistent starting state
 * 
 * HOW IT WORKS:
 * -------------
 * - @Configuration marks this as a configuration class
 * - @Bean creates a Spring-managed CommandLineRunner
 * - CommandLineRunner's run() method executes after the application context loads
 * - We check if data exists to avoid duplicate entries on restart
 * 
 * SIMPLE ANALOGY:
 * ---------------
 * Think of this like setting up a new store. Before opening day, you need to:
 * - Stock the shelves (create sample data)
 * - Set up employee accounts (create admin user)
 * - Prepare demo items for display (sample clients, portfolios)
 * 
 * =============================================================================
 */
@Configuration
public class DataInitializer {

    /**
     * Creates a CommandLineRunner bean that runs on application startup
     * 
     * @param userRepository Repository for User operations
     * @param clientRepository Repository for Client operations
     * @param portfolioRepository Repository for Portfolio operations
     * @param investmentPlanRepository Repository for InvestmentPlan operations
     * @param reportRepository Repository for Report operations
     * @param auditLogRepository Repository for AuditLog operations
     * @param passwordEncoder For encoding passwords securely
     * @return CommandLineRunner that initializes data
     */
    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            ClientRepository clientRepository,
            PortfolioRepository portfolioRepository,
            InvestmentPlanRepository investmentPlanRepository,
            ReportRepository reportRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            System.out.println("\n🔄 Checking database initialization...\n");
            
            // =========================================================
            // STEP 1: Create Default Users (if they don't exist)
            // =========================================================
            if (userRepository.findByUsername("admin").isEmpty()) {
                System.out.println("📝 Creating default admin user...");
                
                // Create Admin User
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt encoded
                admin.setEmail("admin@wealthmanagement.com");
                admin.setFullName("System Administrator");
                admin.setEnabled(true);
                admin.setAccountNonExpired(true);
                admin.setAccountNonLocked(true);
                admin.setCredentialsNonExpired(true);
                
                Set<User.Role> adminRoles = new HashSet<>();
                adminRoles.add(User.Role.ROLE_ADMIN);
                adminRoles.add(User.Role.ROLE_ADVISOR);
                admin.setRoles(adminRoles);
                
                userRepository.save(admin);
                System.out.println("✅ Admin user created (username: admin, password: admin123)");
                
                // Create Advisor User
                User advisor = new User();
                advisor.setUsername("advisor");
                advisor.setPassword(passwordEncoder.encode("advisor123"));
                advisor.setEmail("advisor@wealthmanagement.com");
                advisor.setFullName("John Advisor");
                advisor.setEnabled(true);
                advisor.setAccountNonExpired(true);
                advisor.setAccountNonLocked(true);
                advisor.setCredentialsNonExpired(true);
                
                Set<User.Role> advisorRoles = new HashSet<>();
                advisorRoles.add(User.Role.ROLE_ADVISOR);
                advisor.setRoles(advisorRoles);
                
                userRepository.save(advisor);
                System.out.println("✅ Advisor user created (username: advisor, password: advisor123)");
                
                // Create Compliance Officer
                User compliance = new User();
                compliance.setUsername("compliance");
                compliance.setPassword(passwordEncoder.encode("compliance123"));
                compliance.setEmail("compliance@wealthmanagement.com");
                compliance.setFullName("Sarah Compliance");
                compliance.setEnabled(true);
                compliance.setAccountNonExpired(true);
                compliance.setAccountNonLocked(true);
                compliance.setCredentialsNonExpired(true);
                
                Set<User.Role> complianceRoles = new HashSet<>();
                complianceRoles.add(User.Role.ROLE_COMPLIANCE_OFFICER);
                compliance.setRoles(complianceRoles);
                
                userRepository.save(compliance);
                System.out.println("✅ Compliance Officer created (username: compliance, password: compliance123)");
            }
            
            // =========================================================
            // STEP 2: Create Sample Clients (if they don't exist)
            // =========================================================
            if (clientRepository.count() == 0) {
                System.out.println("\n📝 Creating sample clients...");
                
                // Client 1 - High Net Worth Individual
                Client client1 = new Client();
                client1.setName("Robert Johnson");
                client1.setEmail("robert.johnson@email.com");
                client1.setPhone("5550101001");
                client1.setAddress("123 Wall Street, New York, NY 10005");
                client1.setDateOfBirth(LocalDate.of(1975, 3, 15));
                clientRepository.save(client1);
                
                // Client 2 - Young Professional
                Client client2 = new Client();
                client2.setName("Emily Chen");
                client2.setEmail("emily.chen@email.com");
                client2.setPhone("5550102002");
                client2.setAddress("456 Tech Boulevard, San Francisco, CA 94105");
                client2.setDateOfBirth(LocalDate.of(1990, 7, 22));
                clientRepository.save(client2);
                
                // Client 3 - Retired Executive
                Client client3 = new Client();
                client3.setName("Michael Thompson");
                client3.setEmail("michael.thompson@email.com");
                client3.setPhone("5550103003");
                client3.setAddress("789 Retirement Lane, Miami, FL 33101");
                client3.setDateOfBirth(LocalDate.of(1958, 11, 8));
                clientRepository.save(client3);
                
                // Client 4 - Small Business Owner
                Client client4 = new Client();
                client4.setName("Sarah Williams");
                client4.setEmail("sarah.williams@email.com");
                client4.setPhone("5550104004");
                client4.setAddress("321 Business Park, Chicago, IL 60601");
                client4.setDateOfBirth(LocalDate.of(1982, 5, 30));
                clientRepository.save(client4);
                
                System.out.println("✅ 4 sample clients created");
                
                // =========================================================
                // STEP 3: Create Investment Plans
                // =========================================================
                System.out.println("\n📝 Creating investment plans...");
                
                // Plan for Client 1 - Aggressive Growth
                InvestmentPlan plan1 = new InvestmentPlan();
                plan1.setClient(client1);
                plan1.setInvestmentObjective("Aggressive Wealth Growth");
                plan1.setRiskAppetite(InvestmentPlan.RiskAppetite.HIGH);
                plan1.setTargetAmount(new BigDecimal("2000000.00"));
                plan1.setDurationYears(10);
                plan1.setAllocationDetails("Equities: 70%\nGrowth Stocks: 40%\nInternational Stocks: 20%\nEmerging Markets: 10%\nBonds: 20%\nAlternatives: 10%");
                plan1.setStatus(InvestmentPlan.PlanStatus.ACTIVE);
                investmentPlanRepository.save(plan1);
                
                // Plan for Client 2 - Balanced Growth
                InvestmentPlan plan2 = new InvestmentPlan();
                plan2.setClient(client2);
                plan2.setInvestmentObjective("Home Down Payment Savings");
                plan2.setRiskAppetite(InvestmentPlan.RiskAppetite.MEDIUM);
                plan2.setTargetAmount(new BigDecimal("150000.00"));
                plan2.setDurationYears(5);
                plan2.setAllocationDetails("US Stocks: 40%\nInternational Stocks: 15%\nBonds: 35%\nCash/Money Market: 10%");
                plan2.setStatus(InvestmentPlan.PlanStatus.ACTIVE);
                investmentPlanRepository.save(plan2);
                
                // Plan for Client 3 - Conservative Income
                InvestmentPlan plan3 = new InvestmentPlan();
                plan3.setClient(client3);
                plan3.setInvestmentObjective("Retirement Income Generation");
                plan3.setRiskAppetite(InvestmentPlan.RiskAppetite.LOW);
                plan3.setTargetAmount(new BigDecimal("500000.00"));
                plan3.setDurationYears(15);
                plan3.setAllocationDetails("Dividend Stocks: 25%\nBonds: 50%\nREITs: 15%\nCash: 10%");
                plan3.setStatus(InvestmentPlan.PlanStatus.ACTIVE);
                investmentPlanRepository.save(plan3);
                
                System.out.println("✅ 3 investment plans created");
                
                // =========================================================
                // STEP 4: Create Portfolios
                // =========================================================
                System.out.println("\n📝 Creating portfolios...");
                
                // Portfolio for Client 1
                Portfolio portfolio1 = new Portfolio();
                portfolio1.setClient(client1);
                portfolio1.setPortfolioName("Growth & Momentum Portfolio");
                portfolio1.setInitialInvestment(new BigDecimal("500000.00"));
                portfolio1.setTotalValue(new BigDecimal("575000.00")); // 15% gain
                portfolio1.setAllocationSummary("AAPL: ₹115,000 (20%)\nMSFT: ₹86,250 (15%)\nAMZN: ₹57,500 (10%)\nNVDA: ₹57,500 (10%)\nVOO (S&P 500 ETF): ₹143,750 (25%)\nBND (Bond ETF): ₹115,000 (20%)");
                portfolio1.setLastUpdated(LocalDate.now());
                portfolioRepository.save(portfolio1);
                
                // Portfolio for Client 2
                Portfolio portfolio2 = new Portfolio();
                portfolio2.setClient(client2);
                portfolio2.setPortfolioName("First-Time Buyer Fund");
                portfolio2.setInitialInvestment(new BigDecimal("50000.00"));
                portfolio2.setTotalValue(new BigDecimal("54500.00")); // 9% gain
                portfolio2.setAllocationSummary("VTI (Total Stock Market): ₹21,800 (40%)\nVXUS (International): ₹8,175 (15%)\nBND (Bonds): ₹19,075 (35%)\nVMFXX (Money Market): ₹5,450 (10%)");
                portfolio2.setLastUpdated(LocalDate.now());
                portfolioRepository.save(portfolio2);
                
                // Portfolio for Client 3
                Portfolio portfolio3 = new Portfolio();
                portfolio3.setClient(client3);
                portfolio3.setPortfolioName("Income Generation Portfolio");
                portfolio3.setInitialInvestment(new BigDecimal("750000.00"));
                portfolio3.setTotalValue(new BigDecimal("785000.00")); // 4.67% gain
                portfolio3.setAllocationSummary("VYM (High Dividend ETF): ₹196,250 (25%)\nAGG (Aggregate Bond): ₹235,500 (30%)\nLQD (Corporate Bonds): ₹157,000 (20%)\nVNQ (REIT ETF): ₹117,750 (15%)\nCash Reserves: ₹78,500 (10%)");
                portfolio3.setLastUpdated(LocalDate.now());
                portfolioRepository.save(portfolio3);
                
                System.out.println("✅ 3 portfolios created");
                
                // =========================================================
                // STEP 5: Create Sample Reports
                // =========================================================
                System.out.println("\n📝 Creating sample reports...");
                
                // Get user IDs for generated_by field
                User adminUser = userRepository.findByUsername("admin").orElse(null);
                User advisorUser = userRepository.findByUsername("advisor").orElse(null);
                
                Report report1 = new Report();
                report1.setPortfolio(portfolio1);
                report1.setReportTitle("Q4 2023 Performance Review");
                report1.setReportType(Report.ReportType.QUARTERLY);
                report1.setReportDate(LocalDate.of(2024, 1, 5));
                report1.setPerformanceSummary("The Growth & Momentum Portfolio delivered exceptional performance in Q4 2023.\n\n" +
                        "Key Highlights:\n" +
                        "- Total Return: +8.5% (vs S&P 500: +11.2%)\n" +
                        "- Best Performer: NVDA (+42%)\n" +
                        "- YTD Return: +15%\n\n" +
                        "Market Commentary:\n" +
                        "Strong performance driven by tech sector recovery and AI momentum. " +
                        "Portfolio benefited from overweight positions in semiconductor and cloud computing sectors.");
                report1.setGeneratedBy(adminUser != null ? adminUser.getUserId() : null);
                report1.setNotes("Client expressed satisfaction with performance. Discussed potential rebalancing in Q1 2024.");
                reportRepository.save(report1);
                
                Report report2 = new Report();
                report2.setPortfolio(portfolio3);
                report2.setReportTitle("Annual Income Report 2023");
                report2.setReportType(Report.ReportType.ANNUAL);
                report2.setReportDate(LocalDate.of(2024, 1, 15));
                report2.setPerformanceSummary("Income Generation Portfolio - Annual Review 2023\n\n" +
                        "Income Summary:\n" +
                        "- Total Dividends Received: ₹28,500\n" +
                        "- Bond Interest: ₹18,750\n" +
                        "- REIT Distributions: ₹8,250\n" +
                        "- Total Income: ₹55,500 (7.4% yield)\n\n" +
                        "Principal Change: +₹35,000 (+4.67%)\n\n" +
                        "The portfolio continues to meet its primary objective of generating " +
                        "steady income while preserving capital.");
                report2.setGeneratedBy(advisorUser != null ? advisorUser.getUserId() : null);
                reportRepository.save(report2);
                
                System.out.println("✅ 2 sample reports created");
                
                // =========================================================
                // STEP 6: Create Sample Audit Logs
                // =========================================================
                System.out.println("\n📝 Creating sample audit logs...");
                
                AuditLog log1 = new AuditLog();
                log1.setEventTimestamp(LocalDateTime.now().minusDays(1));
                log1.setEventType(AuditLog.EventType.LOGIN);
                log1.setEventDescription("User 'admin' logged in successfully");
                log1.setUserId(1L);
                log1.setUsername("admin");
                log1.setComplianceStatus(AuditLog.ComplianceStatus.PASS);
                log1.setIpAddress("192.168.1.100");
                auditLogRepository.save(log1);
                
                AuditLog log2 = new AuditLog();
                log2.setEventTimestamp(LocalDateTime.now().minusHours(12));
                log2.setEventType(AuditLog.EventType.CREATE);
                log2.setEventDescription("New portfolio 'Growth & Momentum Portfolio' created for client Robert Johnson");
                log2.setUserId(1L);
                log2.setUsername("admin");
                log2.setComplianceStatus(AuditLog.ComplianceStatus.PASS);
                log2.setEntityType("Portfolio");
                log2.setEntityId(1L);
                log2.setIpAddress("192.168.1.100");
                auditLogRepository.save(log2);
                
                AuditLog log3 = new AuditLog();
                log3.setEventTimestamp(LocalDateTime.now().minusHours(6));
                log3.setEventType(AuditLog.EventType.UPDATE);
                log3.setEventDescription("Portfolio value updated");
                log3.setUserId(2L);
                log3.setUsername("advisor");
                log3.setComplianceStatus(AuditLog.ComplianceStatus.PASS);
                log3.setEntityType("Portfolio");
                log3.setEntityId(1L);
                log3.setOldValue("₹550,000.00");
                log3.setNewValue("₹575,000.00");
                log3.setIpAddress("192.168.1.101");
                auditLogRepository.save(log3);
                
                AuditLog log4 = new AuditLog();
                log4.setEventTimestamp(LocalDateTime.now().minusHours(2));
                log4.setEventType(AuditLog.EventType.COMPLIANCE_CHECK);
                log4.setEventDescription("Routine compliance verification completed");
                log4.setUserId(3L);
                log4.setUsername("compliance");
                log4.setComplianceStatus(AuditLog.ComplianceStatus.PASS);
                log4.setIpAddress("192.168.1.102");
                auditLogRepository.save(log4);
                
                System.out.println("✅ 4 sample audit logs created");
            }
            
            System.out.println("\n========================================");
            System.out.println("  ✅ Database initialization complete!");
            System.out.println("========================================\n");
        };
    }
}
