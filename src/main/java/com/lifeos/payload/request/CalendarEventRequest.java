package com.lifeos.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CalendarEventRequest {

    @NotBlank(message = "El título del evento es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "La fecha/hora de inicio es obligatoria")
    private LocalDateTime startDateTime;

    @NotNull(message = "La fecha/hora de fin es obligatoria")
    private LocalDateTime endDateTime;

    private String colorHex;
}

