package com.locknroll.repository;

import com.locknroll.entity.Fruit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Fruit entity operations
 */
@Repository
public interface FruitRepository extends JpaRepository<Fruit, Long> {
    
    /**
     * Find fruit by name
     */
    Optional<Fruit> findByName(String name);
    
    /**
     * Find fruits by category
     */
    List<Fruit> findByCategory(String category);
    
    /**
     * Find fruits with low stock (quantity less than threshold)
     */
    @Query("SELECT f FROM Fruit f WHERE f.quantity < :threshold")
    List<Fruit> findLowStockFruits(@Param("threshold") Integer threshold);
    
    /**
     * Find fruits by price range
     */
    @Query("SELECT f FROM Fruit f WHERE f.price BETWEEN :minPrice AND :maxPrice")
    List<Fruit> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * Check if fruit exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Find all fruit names
     */
    @Query("SELECT f.name FROM Fruit f")
    List<String> findAllNames();
    
    /**
     * Find fruits by status
     */
    List<Fruit> findByStatus(String status);
    
    /**
     * Find fruits by created by user
     */
    List<Fruit> findByCreatedBy(String createdBy);
    
    /**
     * Find fruits by created by user and status
     */
    List<Fruit> findByCreatedByAndStatus(String createdBy, String status);
}
