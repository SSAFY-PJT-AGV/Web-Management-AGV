package com.example.ssafy_pjt.backend.feature.product.repository;

import com.example.ssafy_pjt.backend.feature.product.entity.Product;
import com.example.ssafy_pjt.backend.feature.product.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductType(ProductType productType);
}