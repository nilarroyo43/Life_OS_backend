package com.lifeos.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectRequest {
    
    @NotBlank(message = "El nombre del proyecto es obligatorio")
    private String name;
    
    private String description;
    
    private Long categoryId; // Para saber en qué categoría se crea
}