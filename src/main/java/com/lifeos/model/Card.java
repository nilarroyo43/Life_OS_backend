package com.lifeos.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Setter
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    // --- TEMPORALIDAD AUTOMÁTICA ---
    private LocalDate startDate;
    private LocalDate endDate;

    // --- RELACIONES ESTRUCTURALES ---
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // --- RECURSIVIDAD (Padre/Hijo) ---
    // @JsonIgnore en parentCard evita el bucle Card -> parentCard -> subCards -> Card...
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "parent_card_id")
    private Card parentCard;

    // @JsonIgnore en subCards evita la serialización recursiva infinita
    @JsonIgnore
    @OneToMany(mappedBy = "parentCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> subCards = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "card_tags",
        joinColumns = @JoinColumn(name = "card_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (this.status == null) this.status = CardStatus.PENDING;
    }
}