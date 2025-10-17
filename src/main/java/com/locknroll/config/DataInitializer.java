package com.locknroll.config;

import com.locknroll.entity.Fruit;
import com.locknroll.repository.FruitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Data initializer to populate the database with sample fruits
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private FruitRepository fruitRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing sample data...");
        
        // Check if data already exists
        if (fruitRepository.count() > 0) {
            logger.info("Data already exists, skipping initialization");
            return;
        }
        
        // Create sample fruits
        Fruit[] sampleFruits = {
            new Fruit("Apple", new BigDecimal("2.50"), 100, "Fresh red apples", "Fruit"),
            new Fruit("Banana", new BigDecimal("1.80"), 150, "Yellow bananas", "Fruit"),
            new Fruit("Orange", new BigDecimal("3.00"), 80, "Sweet oranges", "Fruit"),
            new Fruit("Grape", new BigDecimal("4.50"), 60, "Green grapes", "Fruit"),
            new Fruit("Strawberry", new BigDecimal("5.00"), 40, "Fresh strawberries", "Berry"),
            new Fruit("Blueberry", new BigDecimal("6.00"), 30, "Organic blueberries", "Berry"),
            new Fruit("Mango", new BigDecimal("4.00"), 25, "Tropical mangoes", "Fruit"),
            new Fruit("Pineapple", new BigDecimal("3.50"), 20, "Sweet pineapples", "Fruit"),
            new Fruit("Watermelon", new BigDecimal("8.00"), 15, "Large watermelons", "Fruit"),
            new Fruit("Kiwi", new BigDecimal("3.20"), 35, "Green kiwis", "Fruit")
        };
        
        for (Fruit fruit : sampleFruits) {
            try {
                fruitRepository.save(fruit);
                logger.info("Created fruit: {}", fruit.getName());
            } catch (Exception e) {
                logger.warn("Failed to create fruit: {} - {}", fruit.getName(), e.getMessage());
            }
        }
        
        logger.info("Data initialization completed. Created {} fruits.", fruitRepository.count());
    }
}
