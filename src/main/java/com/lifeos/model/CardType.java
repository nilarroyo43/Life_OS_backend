package com.lifeos.model;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "card_Type")
@Data
public class CardType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color; // para que la etiqueta tenga color

    // Relación: Esta opción fue definida EN esta tarjeta padre
    @ManyToOne
    @JoinColumn(name = "defined_in_card_id")
    @JsonIgnore // Evita bucles infinitos al serializar a JSON
    private Card definedInCard;
}