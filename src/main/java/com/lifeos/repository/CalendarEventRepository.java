package com.lifeos.repository;

import com.lifeos.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    // Buscar eventos de un usuario entre dos fechas (para pintar la semana)
    // Usamos JPQL (Java Persistence Query Language) para ser precisos
    @Query("SELECT e FROM CalendarEvent e WHERE e.user.id = :userId AND e.startDateTime BETWEEN :start AND :end")
    List<CalendarEvent> findWeeklyEvents(@Param("userId") Long userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}