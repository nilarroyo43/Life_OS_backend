package com.lifeos.controllers;

import com.lifeos.model.CalendarEvent;
import com.lifeos.model.User;
import com.lifeos.payload.request.CalendarEventRequest;
import com.lifeos.repository.CalendarEventRepository;
import com.lifeos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarEventController {

    @Autowired
    CalendarEventRepository calendarEventRepository;

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<?> createEvent(@Valid @RequestBody CalendarEventRequest request) {
        User currentUser = userService.getCurrentUser();

        // Validación de fechas: el inicio debe ser antes que el fin
        if (request.getEndDateTime().isBefore(request.getStartDateTime())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La fecha de fin no puede ser anterior a la de inicio");
        }

        CalendarEvent event = new CalendarEvent();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartDateTime(request.getStartDateTime());
        event.setEndDateTime(request.getEndDateTime());
        event.setColorHex(request.getColorHex());
        event.setUser(currentUser);

        return ResponseEntity.ok(calendarEventRepository.save(event));
    }

    @GetMapping("/range")
    public ResponseEntity<?> getEventsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        User currentUser = userService.getCurrentUser();
        List<CalendarEvent> events = calendarEventRepository.findWeeklyEvents(
                currentUser.getId(), start, end);
        return ResponseEntity.ok(events);
    }
}

