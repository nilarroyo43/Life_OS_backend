package com.lifeos.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

import com.lifeos.model.CardStatus;

@Data
public class CardRequest {
    
    @NotBlank(message = "El título es obligatorio")
    private String title;
    
    private String description;
    
    private CardStatus status; // PENDING, IN_PROGRESS, DONE, ARCHIVED
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Long categoryId; // EL DATO CLAVE
}
