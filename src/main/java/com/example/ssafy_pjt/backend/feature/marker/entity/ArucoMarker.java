package com.example.ssafy_pjt.backend.feature.marker.entity;

import com.example.ssafy_pjt.backend.feature.marker.enums.MarkerType;
import com.example.ssafy_pjt.backend.feature.material.entity.Material;
import com.example.ssafy_pjt.backend.feature.product.entity.Product;
import com.example.ssafy_pjt.backend.feature.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aruco_marker")
@Getter
@Setter
@NoArgsConstructor
public class ArucoMarker {

    @Id
    @Column(name = "marker_id")
    private Integer markerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "marker_type", nullable = false, length = 20)
    private MarkerType markerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "x_position")
    private Double xPosition;

    @Column(name = "y_position")
    private Double yPosition;

    @Column(length = 100)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "empty_status")
    private Boolean emptyStatus;
}