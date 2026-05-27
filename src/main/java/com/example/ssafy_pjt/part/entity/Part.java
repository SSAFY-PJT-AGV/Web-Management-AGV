package com.example.ssafy_pjt.part.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "part")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Part {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private int partId;

    @Column(name = "part_code", nullable = false, unique = true, length = 50)
    private String partCode;

    @Column(name = "part_name", nullable = false, length = 100)
    private String partName;
}
