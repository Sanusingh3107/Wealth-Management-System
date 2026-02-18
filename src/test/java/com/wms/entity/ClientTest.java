package com.wms.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Client entity
 * Tests constructors, getters, setters, and toString
 */
class ClientTest {

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client();
        client.setClientId(1L);
        client.setName("John Doe");
        client.setEmail("john.doe@example.com");
        client.setPhone("1234567890");
        client.setAddress("123 Main St, New York, NY");
        client.setDateOfBirth(LocalDate.of(1990, 1, 15));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create client with default constructor")
        void defaultConstructor() {
            Client c = new Client();
            assertNotNull(c);
        }

        @Test
        @DisplayName("Should create client with parameterized constructor")
        void parameterizedConstructor() {
            Client c = new Client("Jane Smith", "jane@example.com", "9876543210", 
                                   "456 Oak Ave", LocalDate.of(1985, 6, 20));
            
            assertEquals("Jane Smith", c.getName());
            assertEquals("jane@example.com", c.getEmail());
            assertEquals("9876543210", c.getPhone());
            assertEquals("456 Oak Ave", c.getAddress());
            assertEquals(LocalDate.of(1985, 6, 20), c.getDateOfBirth());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set clientId")
        void clientId() {
            client.setClientId(100L);
            assertEquals(100L, client.getClientId());
        }

        @Test
        @DisplayName("Should get and set name")
        void name() {
            client.setName("New Name");
            assertEquals("New Name", client.getName());
        }

        @Test
        @DisplayName("Should get and set email")
        void email() {
            client.setEmail("new@example.com");
            assertEquals("new@example.com", client.getEmail());
        }

        @Test
        @DisplayName("Should get and set phone")
        void phone() {
            client.setPhone("5555555555");
            assertEquals("5555555555", client.getPhone());
        }

        @Test
        @DisplayName("Should get and set address")
        void address() {
            client.setAddress("New Address");
            assertEquals("New Address", client.getAddress());
        }

        @Test
        @DisplayName("Should get and set dateOfBirth")
        void dateOfBirth() {
            LocalDate newDate = LocalDate.of(1995, 5, 10);
            client.setDateOfBirth(newDate);
            assertEquals(newDate, client.getDateOfBirth());
        }

        @Test
        @DisplayName("Should get and set investmentPlans")
        void investmentPlans() {
            client.setInvestmentPlans(new ArrayList<>());
            assertNotNull(client.getInvestmentPlans());
            assertEquals(0, client.getInvestmentPlans().size());
        }

        @Test
        @DisplayName("Should get and set portfolios")
        void portfolios() {
            client.setPortfolios(new ArrayList<>());
            assertNotNull(client.getPortfolios());
            assertEquals(0, client.getPortfolios().size());
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return correct string representation")
        void toStringTest() {
            String result = client.toString();
            
            assertTrue(result.contains("clientId=1"));
            assertTrue(result.contains("name='John Doe'"));
            assertTrue(result.contains("email='john.doe@example.com'"));
            assertTrue(result.contains("phone='1234567890'"));
        }
    }
}
