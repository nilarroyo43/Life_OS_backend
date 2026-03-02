package com.lifeos.controllers;

import com.lifeos.payload.request.LoginRequest;
import com.lifeos.payload.response.JwtResponse;
import com.lifeos.security.jwt.JwtUtils;
import com.lifeos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600) // Permite que Angular (puerto 4200) nos hable
@RestController
@RequestMapping("/api/auth") // Todas las URL empezarán por aquí
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager; 

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils; // Nuestra fábrica de tokens

    // El Endpoint de Login
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // 1. INTENTO DE AUTENTICACIÓN
        // El AuthenticationManager coge el usuario y pass, los hashea y compara con BBDD.
        // Si falla, lanza una excepción automáticamente.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 2. GUARDAR EN EL CONTEXTO
        // Si llegamos aquí, es que el usuario es válido. Lo guardamos en la memoria de seguridad de Spring.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. GENERAR EL TOKEN (La Llave)
        String jwt = jwtUtils.generateJwtToken(authentication);

        // 4. OBTENER DATOS DEL USUARIO (Para enviarlos al frontend)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Obtenemos el rol (asumimos que solo tiene uno para simplificar)
        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();

        // 5. RESPONDER CON EL JSON
        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 null, // No tenemos el ID a mano fácil en UserDetails estándar, podemos buscarlo o ignorarlo por ahora.
                                                 userDetails.getUsername(),
                                                 role));
    }
    
    // Nota: Para obtener el ID real en el punto 5, habría que hacer un pequeño cambio 
    // en UserDetailsImpl o buscar el usuario en bbdd. 
    // Para probar ahora, puedes dejar el ID como null o 0L.
}