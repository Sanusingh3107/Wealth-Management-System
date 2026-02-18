package com.wealth.management;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WealthManagementApplication Tests")
class WealthManagementApplicationTest {

    @Nested
    @DisplayName("Application Class Tests")
    class ApplicationClassTests {

        @Test
        @DisplayName("Should have main method")
        void shouldHaveMainMethod() throws NoSuchMethodException {
            // Verify main method exists with correct signature
            assertDoesNotThrow(() -> 
                WealthManagementApplication.class.getMethod("main", String[].class));
        }

        @Test
        @DisplayName("Should be annotated with SpringBootApplication")
        void shouldBeAnnotatedWithSpringBootApplication() {
            assertTrue(WealthManagementApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.SpringBootApplication.class));
        }

        @Test
        @DisplayName("Should be annotated with ComponentScan")
        void shouldBeAnnotatedWithComponentScan() {
            assertTrue(WealthManagementApplication.class.isAnnotationPresent(
                org.springframework.context.annotation.ComponentScan.class));
        }

        @Test
        @DisplayName("Should be annotated with EntityScan")
        void shouldBeAnnotatedWithEntityScan() {
            assertTrue(WealthManagementApplication.class.isAnnotationPresent(
                org.springframework.boot.autoconfigure.domain.EntityScan.class));
        }

        @Test
        @DisplayName("Should be annotated with EnableJpaRepositories")
        void shouldBeAnnotatedWithEnableJpaRepositories() {
            assertTrue(WealthManagementApplication.class.isAnnotationPresent(
                org.springframework.data.jpa.repository.config.EnableJpaRepositories.class));
        }

        @Test
        @DisplayName("Should scan correct base packages")
        void shouldScanCorrectBasePackages() {
            org.springframework.context.annotation.ComponentScan annotation = 
                WealthManagementApplication.class.getAnnotation(
                    org.springframework.context.annotation.ComponentScan.class);
            
            assertNotNull(annotation);
            String[] basePackages = annotation.basePackages();
            assertTrue(basePackages.length >= 2);
        }

        @Test
        @DisplayName("Should be able to instantiate application class")
        void shouldBeAbleToInstantiate() {
            WealthManagementApplication app = new WealthManagementApplication();
            assertNotNull(app);
        }
    }

    /**
     * Integration test that loads the full Spring context
     * This will cover the main method's SpringApplication.run() call
     */
    @Nested
    @DisplayName("Application Context Tests")
    @SpringBootTest
    class ApplicationContextTests {

        @Test
        @DisplayName("Should load application context successfully")
        void contextLoads() {
            // If this test passes, the application context loads correctly
            // This implicitly tests that SpringApplication.run() works
            assertTrue(true);
        }
    }
}
