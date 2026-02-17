package com.lifeos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // Configuración: ¿Las tarjetas de aquí trackean tiempo?
    private boolean hasTimeTracking;

    // Configuración visual
    private String color;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}