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

@RestController
@RequestMapping("/api/auth") // CORS gestionado globalmente en WebSecurityConfig
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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);


        String jwt = jwtUtils.generateJwtToken(authentication);


        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        

        String role = userDetails.getAuthorities().stream().findFirst().get().getAuthority();


        return ResponseEntity.ok(new JwtResponse(jwt,
                                                 null,
                                                 userDetails.getUsername(),
                                                 role));
    }

}