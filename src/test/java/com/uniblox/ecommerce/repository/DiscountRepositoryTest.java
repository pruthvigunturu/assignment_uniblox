package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Discount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscountRepositoryTest {

    private DiscountRepository discountRepository;

    @BeforeEach
    void setUp() {
        discountRepository = new DiscountRepository();
    }

    @Test
    void save_NewDiscount_ShouldPersistAndRetrieve() {
        // Arrange
        Discount discount = new Discount();
        discount.setCode("DISC10");
        discount.setPercentage(10.0);
        discount.setUsed(false);

        // Act
        discountRepository.save(discount);

        // Assert
        Discount result = discountRepository.findByCode("DISC10");
        assertNotNull(result);
        assertEquals(10.0, result.getPercentage());
        assertFalse(result.isUsed());
    }

    @Test
    void findByCode_NonExistingDiscount_ShouldReturnNull() {
        // Act & Assert
        assertNull(discountRepository.findByCode("NONEXISTENT"));
    }

    @Test
    void save_UpdateDiscount_ShouldUpdateUsedStatus() {
        // Arrange
        Discount discount = new Discount();
        discount.setCode("DISC15");
        discount.setPercentage(15.0);
        discount.setUsed(false);
        discountRepository.save(discount);

        // Act
        discount.setUsed(true);
        discountRepository.save(discount);

        // Assert
        Discount updated = discountRepository.findByCode("DISC15");
        assertTrue(updated.isUsed());
    }

    @Test
    void findAll_ShouldReturnAllDiscounts() {
        // Arrange
        Discount discount1 = new Discount();
        discount1.setCode("DISC10");
        discount1.setPercentage(10.0);

        Discount discount2 = new Discount();
        discount2.setCode("DISC20");
        discount2.setPercentage(20.0);

        discountRepository.save(discount1);
        discountRepository.save(discount2);

        // Act
        Iterable<Discount> result = discountRepository.findAll();

        // Assert
        int count = 0;
        for (Discount d : result) {
            count++;
        }
        assertEquals(2, count);
    }
}

