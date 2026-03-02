package com.lifeos.model;

import jakarta.persistence.*; // VITAL: Asegura que @Entity y @Id funcionen
import lombok.Data;
import java.time.LocalDateTime;

@Entity // <--- ESTO ES LO QUE SPRING ESTABA BUSCANDO
@Table(name = "calendar_events")
@Data
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;
    
    private String colorHex; 

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}