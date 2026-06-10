package com.example.ssafy_pjt.backend.feature.product.entity;

import com.example.ssafy_pjt.backend.feature.product.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, unique = true, length = 50)
    private ProductType productType;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
}