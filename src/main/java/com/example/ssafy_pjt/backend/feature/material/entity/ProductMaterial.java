package com.example.ssafy_pjt.backend.feature.material.entity;

import com.example.ssafy_pjt.backend.feature.product.entity.Product;
import com.example.ssafy_pjt.backend.feature.product.enums.ProductType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_material",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"product_type", "material_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ProductMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "quantity_per_unit", nullable = false)
    private Integer quantityPerUnit;
}
