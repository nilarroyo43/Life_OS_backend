package com.lifeos.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok genera Getters y Setters automáticamente
public class LoginRequest {
    
    @NotBlank // Valida que no venga vacío
    private String username;

    @NotBlank
    private String password;
}