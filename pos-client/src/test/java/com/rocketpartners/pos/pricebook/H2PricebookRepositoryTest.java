package com.rocketpartners.pos.pricebook;

import com.rocketpartners.pos.config.DatabaseConfig;
import com.rocketpartners.pos.model.Product;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class H2PricebookRepositoryTest {

    private static H2PricebookRepository repository;

    @BeforeAll
    static void setUp() {
        repository = new H2PricebookRepository(DatabaseConfig.getEntityManagerFactory());
    }

    @AfterAll
    static void tearDown() {
        DatabaseConfig.shutdown();
    }

    @Test
    void findByUpc_knownProduct_returnsProduct() {
        // UPC from pricebook.tsv: Monster Energy
        Optional<Product> result = repository.findByUpc("070847811169");

        assertTrue(result.isPresent());
        assertEquals("070847811169", result.get().getUpc());
        assertEquals("MONSTER ENERGY", result.get().getName());
        assertEquals(3.29, result.get().getPrice());
    }

    @Test
    void findByUpc_unknownUpc_returnsEmpty() {
        Optional<Product> result = repository.findByUpc("000000000000");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUpc_shortNumericUpc_returnsProduct() {
        // UPC "80" — TB OPEN ONLINE LOTTO (short non-standard UPC)
        Optional<Product> result = repository.findByUpc("80");

        assertTrue(result.isPresent());
        assertEquals("TB OPEN ONLINE LOTTO", result.get().getName());
    }
}
