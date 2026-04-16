package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.*;
import pt.uminho.mei.bilhetica.security.JwtUtil;
import pt.uminho.mei.bilhetica.service.UtenteService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UtenteService utenteService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(UtenteService utenteService,
                          AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil) {
        this.utenteService = utenteService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/registar")
    public ResponseEntity<?> registar(@RequestBody RegistarRequest request) {
        utenteService.registar(
            request.getNome(),
            request.getEmail(),
            request.getTelemovel(),
            request.getPassword()
        );
        return ResponseEntity.status(201).body("Utente registado com sucesso");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()));

        String token = jwtUtil.gerarToken(auth.getName());
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", 86400000));
    }
}