package com.rocketpartners.pos.pricebook;

import com.rocketpartners.pos.model.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class H2PricebookRepository implements PricebookRepository {

    private final EntityManagerFactory entityManagerFactory;

    @Override
    public Optional<Product> findByUpc(String upc) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Product product = em.createQuery(
                            "SELECT p FROM Product p WHERE p.upc = :upc", Product.class)
                    .setParameter("upc", upc)
                    .getSingleResult();
            return Optional.of(product);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
