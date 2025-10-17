package com.locknroll.controller;

import com.locknroll.entity.Fruit;
import com.locknroll.service.FruitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Fruit operations
 */
@RestController
@RequestMapping("/api/fruits")
@CrossOrigin(origins = "*")
public class FruitController {
    
    private static final Logger logger = LoggerFactory.getLogger(FruitController.class);
    
    @Autowired
    private FruitService fruitService;
    
    /**
     * Get all fruits
     */
    @GetMapping
    public ResponseEntity<List<Fruit>> getAllFruits() {
        logger.info("GET /api/fruits - Fetching all fruits");
        List<Fruit> fruits = fruitService.findAll();
        return ResponseEntity.ok(fruits);
    }
    
    /**
     * Get fruit by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Fruit> getFruitById(@PathVariable Long id) {
        logger.info("GET /api/fruits/{} - Fetching fruit by ID", id);
        Optional<Fruit> fruit = fruitService.findById(id);
        return fruit.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get fruit by name
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<Fruit> getFruitByName(@PathVariable String name) {
        logger.info("GET /api/fruits/name/{} - Fetching fruit by name", name);
        Optional<Fruit> fruit = fruitService.findByName(name);
        return fruit.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create a new fruit
     */
    @PostMapping
    public ResponseEntity<Fruit> createFruit(@RequestBody Fruit fruit) {
        logger.info("POST /api/fruits - Creating fruit: {}", fruit.getName());
        try {
            // Set default status to DRAFT for new fruits
            fruit.setStatus("DRAFT");
            Fruit createdFruit = fruitService.createFruit(fruit);
            return ResponseEntity.ok(createdFruit);
        } catch (Exception e) {
            logger.error("Error creating fruit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update fruit
     */
    @PutMapping("/{id}")
    public ResponseEntity<Fruit> updateFruit(@PathVariable Long id, @RequestBody Fruit fruit) {
        logger.info("PUT /api/fruits/{} - Updating fruit", id);
        try {
            Fruit updatedFruit = fruitService.updateFruit(id, fruit);
            return ResponseEntity.ok(updatedFruit);
        } catch (Exception e) {
            logger.error("Error updating fruit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Purchase fruit (decrease quantity)
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<Fruit> purchaseFruit(@PathVariable Long id, 
                                             @RequestParam Integer quantity) {
        logger.info("POST /api/fruits/{}/purchase - Purchasing {} units", id, quantity);
        try {
            Fruit fruit = fruitService.purchaseFruit(id, quantity);
            return ResponseEntity.ok(fruit);
        } catch (Exception e) {
            logger.error("Error purchasing fruit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Restock fruit (increase quantity)
     */
    @PostMapping("/{id}/restock")
    public ResponseEntity<Fruit> restockFruit(@PathVariable Long id, 
                                            @RequestParam Integer quantity) {
        logger.info("POST /api/fruits/{}/restock - Restocking {} units", id, quantity);
        try {
            Fruit fruit = fruitService.restockFruit(id, quantity);
            return ResponseEntity.ok(fruit);
        } catch (Exception e) {
            logger.error("Error restocking fruit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete fruit
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFruit(@PathVariable Long id) {
        logger.info("DELETE /api/fruits/{} - Deleting fruit", id);
        try {
            fruitService.deleteFruit(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting fruit: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get fruits by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Fruit>> getFruitsByCategory(@PathVariable String category) {
        logger.info("GET /api/fruits/category/{} - Fetching fruits by category", category);
        List<Fruit> fruits = fruitService.findByCategory(category);
        return ResponseEntity.ok(fruits);
    }
    
    /**
     * Get low stock fruits
     */
    @GetMapping("/low-stock")
    public ResponseEntity<List<Fruit>> getLowStockFruits(@RequestParam(defaultValue = "10") Integer threshold) {
        logger.info("GET /api/fruits/low-stock - Fetching low stock fruits with threshold: {}", threshold);
        List<Fruit> fruits = fruitService.getLowStockFruits(threshold);
        return ResponseEntity.ok(fruits);
    }
}
