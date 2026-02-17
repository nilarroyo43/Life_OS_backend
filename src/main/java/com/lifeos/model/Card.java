package com.lifeos.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cards")
@Data
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT") // Para descripciones largas
    private String description;

    @Enumerated(EnumType.STRING)
    private CardStatus status; // PENDING, IN_PROGRESS, DONE, ARCHIVED

    // --- TEMPORALIDAD AUTOMÁTICA ---
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // --- RELACIONES ESTRUCTURALES ---
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // --- RECURSIVIDAD (Padre/Hijo) ---
    @ManyToOne
    @JoinColumn(name = "parent_card_id")
    private Card parentCard;

    @OneToMany(mappedBy = "parentCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> subCards = new ArrayList<>();

    // --- TIPOS DINÁMICOS  ---


    @OneToMany(mappedBy = "definedInCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardType> definedTypes = new ArrayList<>();


    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private CardType selectedType;

    // --- LIFECYCLE HOOKS ---
    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = CardStatus.PENDING;
    }
}

// Pon esto en CardStatus.java
enum CardStatus {
    PENDING, IN_PROGRESS, DONE, ARCHIVED
}