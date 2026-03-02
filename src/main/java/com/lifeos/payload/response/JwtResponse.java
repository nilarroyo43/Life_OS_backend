package com.lifeos.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer"; // El estándar de JWT
    private Long id;
    private String username;
    private String role;

    public JwtResponse(String accessToken, Long id, String username, String role) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.role = role;
    }
}