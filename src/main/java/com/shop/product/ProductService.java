package com.shop.product;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getProducts() {
        return repository.findByActiveTrue();
    }

    public Product getProduct(Long id) {
        Product product = repository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException("Product not found"));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is inactive");
        }
        return product;
    }

    @Transactional
    public Product create(Product product) {
        if (product.getName() == null || product.getName().isBlank()
                || product.getSku() == null || product.getSku().isBlank()
                || product.getPrice() == null
                || product.getPrice().compareTo(BigDecimal.ZERO) < 0
                || product.getStock() < 0) {
            throw new IllegalArgumentException("Invalid product details");
        }

        if (repository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }
        return repository.save(product);
    }
}