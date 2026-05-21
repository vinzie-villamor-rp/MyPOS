package com.rocketpartners.pos.pricebook;

import com.rocketpartners.pos.model.Product;

import java.util.Optional;

public interface PricebookRepository {

    Optional<Product> findByUpc(String upc);
}
