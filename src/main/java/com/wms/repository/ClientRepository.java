package com.wms.repository;

import com.wms.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ========================================
 * CLIENT REPOSITORY
 * ========================================

 * 
 * JpaRepository provides standard methods like:
 * - save() = INSERT or UPDATE
 * - findById() = SELECT by primary key
 * - findAll() = SELECT all
 * - deleteById() = DELETE
 * - count() = COUNT(*)
 * 
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * FIND BY EMAIL
     * -------------
     */
    Optional<Client> findByEmail(String email);
    
    /**
     * FIND BY NAME (CASE INSENSITIVE, PARTIAL MATCH)
     * ----------------------------------------------
     * 'Containing' = LIKE %value%
     * 'IgnoreCase' = case-insensitive comparison
     */
    List<Client> findByNameContainingIgnoreCase(String name);
    
    /**
     * CHECK IF EMAIL EXISTS
     * ---------------------
     */
    boolean existsByEmail(String email);
    
    /**
     * CHECK IF PHONE EXISTS
     * ---------------------
     */
    boolean existsByPhone(String phone);
    
    /**
     * FIND BY PHONE
     * -------------
     */
    Optional<Client> findByPhone(String phone);
    
    /**
     * CUSTOM QUERY WITH @Query ANNOTATION
     * -----------------------------------
     */
    @Query("SELECT c FROM Client c WHERE c.name LIKE %:searchTerm% OR c.email LIKE %:searchTerm%")
    List<Client> searchClients(@Param("searchTerm") String searchTerm);
    
    /**
     * COUNT CLIENTS BY NAME PATTERN
     * -----------------------------
     */
    long countByNameContainingIgnoreCase(String name);
    
    /**
     * FIND CLIENTS ORDERED BY NAME
     * ----------------------------
     * 'OrderBy' = ORDER BY clause
     * 'Asc' = Ascending order (A to Z)
     */
    List<Client> findAllByOrderByNameAsc();
    
    /**
     * NATIVE SQL QUERY
     * ----------------
     */
    @Query(value = "SELECT * FROM client WHERE YEAR(date_of_birth) = :year", nativeQuery = true)
    List<Client> findByBirthYear(@Param("year") int year);
}
