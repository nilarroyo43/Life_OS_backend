package com.lifeos.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TagRequest {

    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    private String name;

    private String color; // Ej: "#FF5733" — opcional

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoryId;
}

