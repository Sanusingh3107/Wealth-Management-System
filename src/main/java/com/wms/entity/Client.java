package com.wms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ========================================
 * CLIENT ENTITY
 * ========================================
 */
@Entity  // Marks this class as a JPA entity (database table)
@Table(name = "client")  // Specifies the table name in database
public class Client {
    
    /**
     * PRIMARY KEY
     * -----------
     * Unique identifier for each client.
     */
    @Id  // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long clientId;
    
    /**
     * CLIENT NAME
     * -----------
     * @NotBlank ensures the name is not null, empty, or just whitespace
     * @Size limits the length of the name
     */
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * EMAIL
     * -----
     * @Email validates email format (must contain @)
     * unique = true ensures no two clients have the same email
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * PHONE NUMBER
     * ------------
     * @Pattern uses regex to validate phone format
     */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone must be 10-15 digits")
    @Column(length = 15)
    private String phone;
    
    /**
     * ADDRESS
     * -------
     * @Lob (Large Object) is used for longer text fields
     * TEXT type in database allows more characters than VARCHAR
     */
    @Column(columnDefinition = "TEXT")
    private String address;
    
    /**
     * DATE OF BIRTH
     * -------------
     * LocalDate represents a date without time (YYYY-MM-DD)
     * @Past ensures the date is in the past
     */
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    
    /**
     * RELATIONSHIPS
     * -------------
     * One client can have multiple investment plans.
     * mappedBy = "client" refers to the 'client' field in InvestmentPlan entity.
     * CascadeType.ALL means operations on Client cascade to related entities.
     * orphanRemoval = true means if we remove a plan from the list, it's deleted from DB.
     */
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvestmentPlan> investmentPlans;
    
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolios;
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    /**
     * DEFAULT CONSTRUCTOR
     * Required by JPA for creating entity instances.
     */
    public Client() {
    }
    
    /**
     * PARAMETERIZED CONSTRUCTOR
     * For creating a client with all fields.
     */
    public Client(String name, String email, String phone, String address, LocalDate dateOfBirth) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
    }
    
    // ========================================
    // GETTERS AND SETTERS
    // ========================================
    
    public Long getClientId() {
        return clientId;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public List<InvestmentPlan> getInvestmentPlans() {
        return investmentPlans;
    }
    
    public void setInvestmentPlans(List<InvestmentPlan> investmentPlans) {
        this.investmentPlans = investmentPlans;
    }
    
    public List<Portfolio> getPortfolios() {
        return portfolios;
    }
    
    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }
    
    /**
     * toString() METHOD
     * -----------------
     * Returns a string representation of the object.
     * Useful for logging and debugging.
     */
    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
