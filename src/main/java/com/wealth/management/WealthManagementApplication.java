package com.wealth.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * =============================================================================
 * WEALTH MANAGEMENT SYSTEM - Main Application Class
 * =============================================================================

 * This is the entry point of our Spring Boot application. The @SpringBootApplication
 * annotation is actually a combination of three annotations:
 * 
 * 1. @Configuration - Marks this class as a source of bean definitions
 * 2. @EnableAutoConfiguration - Tells Spring Boot to automatically configure 
 *    beans based on the dependencies in the classpath
 * 3. @ComponentScan - Tells Spring to scan for components in this package 
 *    and all sub-packages
 * =============================================================================
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.wealth.management", "com.wms"})
@EntityScan(basePackages = {"com.wealth.management", "com.wms"})
@EnableJpaRepositories(basePackages = {"com.wealth.management", "com.wms"})
public class WealthManagementApplication {

    /**
     * Main method - the starting point of the application
     * 
     * @param args Command line arguments (if any)
     */
    public static void main(String[] args) {
        /*
         * SpringApplication.run() does the following:
         * 1. Creates an ApplicationContext (Spring's IoC container)
         * 2. Triggers auto-configuration
         * 3. Performs component scanning
         * 4. Starts the embedded web server
         */
        SpringApplication.run(WealthManagementApplication.class, args);
        
        System.out.println("\n========================================");
        System.out.println("  WEALTH MANAGEMENT SYSTEM STARTED!");
        System.out.println("========================================");
        System.out.println("  Access the application at:");
        System.out.println("  http://localhost:9090");
        System.out.println("========================================");
        System.out.println("  Default Admin Credentials:");
        System.out.println("  Username: admin");
        System.out.println("  Password: admin123");
        System.out.println("========================================\n");
    }
}
