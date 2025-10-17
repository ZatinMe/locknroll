package com.locknroll.config;

import com.locknroll.entity.Fruit;
import com.locknroll.repository.FruitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Data initializer for creating sample fruits
 */
@Component
public class FruitDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(FruitDataInitializer.class);

    private final FruitRepository fruitRepository;

    public FruitDataInitializer(FruitRepository fruitRepository) {
        this.fruitRepository = fruitRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Initializing sample fruits...");
        createSampleFruits();
        logger.info("Sample fruits initialized successfully");
    }

    private void createSampleFruits() {
        List<Fruit> sampleFruits = Arrays.asList(
            createFruitIfNotExists("Apple", "Fresh red apples from local farm", "Fruits", new BigDecimal("2.50"), 100, "DRAFT"),
            createFruitIfNotExists("Banana", "Sweet yellow bananas", "Fruits", new BigDecimal("1.80"), 150, "DRAFT"),
            createFruitIfNotExists("Orange", "Juicy oranges from California", "Fruits", new BigDecimal("3.20"), 80, "DRAFT"),
            createFruitIfNotExists("Grapes", "Premium seedless grapes", "Fruits", new BigDecimal("4.50"), 60, "DRAFT"),
            createFruitIfNotExists("Strawberry", "Fresh strawberries", "Berries", new BigDecimal("5.00"), 40, "DRAFT"),
            createFruitIfNotExists("Blueberry", "Organic blueberries", "Berries", new BigDecimal("6.50"), 30, "DRAFT"),
            createFruitIfNotExists("Mango", "Sweet tropical mangoes", "Tropical", new BigDecimal("4.80"), 25, "DRAFT"),
            createFruitIfNotExists("Pineapple", "Fresh pineapples", "Tropical", new BigDecimal("3.90"), 20, "DRAFT")
        );

        // Save all non-null fruits
        List<Fruit> fruitsToSave = sampleFruits.stream()
                .filter(fruit -> fruit != null)
                .collect(java.util.stream.Collectors.toList());
        
        if (!fruitsToSave.isEmpty()) {
            fruitRepository.saveAll(fruitsToSave);
            logger.info("Created {} sample fruits", fruitsToSave.size());
        }
    }

    private Fruit createFruitIfNotExists(String name, String description, String category, 
                                        BigDecimal price, Integer quantity, String status) {
        if (fruitRepository.findByName(name).isEmpty()) {
            Fruit fruit = new Fruit();
            fruit.setName(name);
            fruit.setDescription(description);
            fruit.setCategory(category);
            fruit.setPrice(price);
            fruit.setQuantity(quantity);
            fruit.setStatus(status);
            fruit.setCreatedBy("system");
            // isActive and isDeleted are inherited from BaseEntity and have default values
            
            logger.info("Created sample fruit: {} with status: {}", name, status);
            return fruit;
        }
        return null;
    }
}
