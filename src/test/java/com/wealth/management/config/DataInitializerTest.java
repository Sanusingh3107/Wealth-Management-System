package com.wealth.management.config;

import com.wms.entity.*;
import com.wms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DataInitializer Tests")
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private InvestmentPlanRepository investmentPlanRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer();
    }

    @Nested
    @DisplayName("Database Initialization Tests")
    class DatabaseInitializationTests {

        @Test
        @DisplayName("Should skip user creation when admin user exists")
        void initDatabase_AdminExists_SkipsUserCreation() throws Exception {
            User existingAdmin = new User();
            existingAdmin.setUsername("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));
            when(clientRepository.count()).thenReturn(5L);
            
            CommandLineRunner runner = dataInitializer.initDatabase(
                    userRepository, clientRepository, portfolioRepository,
                    investmentPlanRepository, reportRepository, auditLogRepository, passwordEncoder);
            
            runner.run();
            
            verify(userRepository, atLeast(1)).findByUsername("admin");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should create users when admin does not exist")
        void initDatabase_NoAdmin_CreatesUsers() throws Exception {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(clientRepository.count()).thenReturn(5L); // Skip client creation
            
            User savedUser = new User();
            savedUser.setUserId(1L);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            
            CommandLineRunner runner = dataInitializer.initDatabase(
                    userRepository, clientRepository, portfolioRepository,
                    investmentPlanRepository, reportRepository, auditLogRepository, passwordEncoder);
            
            runner.run();
            
            // Should create multiple users: admin, advisor, compliance
            verify(userRepository, atLeast(1)).save(any(User.class));
            verify(passwordEncoder, atLeast(1)).encode(anyString());
        }

        @Test
        @DisplayName("Should create sample data when no clients exist")
        void initDatabase_NoClients_CreatesSampleData() throws Exception {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(clientRepository.count()).thenReturn(0L);
            
            User savedUser = new User();
            savedUser.setUserId(1L);
            savedUser.setUsername("admin");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            
            Client savedClient = new Client();
            savedClient.setClientId(1L);
            when(clientRepository.save(any(Client.class))).thenReturn(savedClient);
            
            Portfolio savedPortfolio = new Portfolio();
            savedPortfolio.setPortfolioId(1L);
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedPortfolio);
            
            InvestmentPlan savedPlan = new InvestmentPlan();
            savedPlan.setPlanId(1L);
            when(investmentPlanRepository.save(any(InvestmentPlan.class))).thenReturn(savedPlan);
            
            Report savedReport = new Report();
            savedReport.setReportId(1L);
            when(reportRepository.save(any(Report.class))).thenReturn(savedReport);
            
            AuditLog savedLog = new AuditLog();
            savedLog.setLogId(1L);
            when(auditLogRepository.save(any(AuditLog.class))).thenReturn(savedLog);
            
            CommandLineRunner runner = dataInitializer.initDatabase(
                    userRepository, clientRepository, portfolioRepository,
                    investmentPlanRepository, reportRepository, auditLogRepository, passwordEncoder);
            
            runner.run();
            
            // Verify sample data creation
            verify(clientRepository, atLeast(1)).save(any(Client.class));
            verify(portfolioRepository, atLeast(1)).save(any(Portfolio.class));
            verify(investmentPlanRepository, atLeast(1)).save(any(InvestmentPlan.class));
            verify(reportRepository, atLeast(1)).save(any(Report.class));
            verify(auditLogRepository, atLeast(1)).save(any(AuditLog.class));
        }

        @Test
        @DisplayName("Should skip sample data when clients exist")
        void initDatabase_ClientsExist_SkipsSampleData() throws Exception {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(clientRepository.count()).thenReturn(10L); // Clients already exist
            when(userRepository.save(any(User.class))).thenReturn(new User());
            
            CommandLineRunner runner = dataInitializer.initDatabase(
                    userRepository, clientRepository, portfolioRepository,
                    investmentPlanRepository, reportRepository, auditLogRepository, passwordEncoder);
            
            runner.run();
            
            // Should not create sample clients
            verify(clientRepository, never()).save(any(Client.class));
            verify(portfolioRepository, never()).save(any(Portfolio.class));
        }
    }

    @Nested
    @DisplayName("Bean Creation Tests")
    class BeanCreationTests {

        @Test
        @DisplayName("Should return CommandLineRunner bean")
        void initDatabase_ReturnsCommandLineRunner() {
            CommandLineRunner runner = dataInitializer.initDatabase(
                    userRepository, clientRepository, portfolioRepository,
                    investmentPlanRepository, reportRepository, auditLogRepository, passwordEncoder);
            
            assertNotNull(runner);
        }
    }
}
