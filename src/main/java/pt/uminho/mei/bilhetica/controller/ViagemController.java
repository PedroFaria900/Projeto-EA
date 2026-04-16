package pt.uminho.mei.bilhetica.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pt.uminho.mei.bilhetica.dto.ViagemResponse;
import pt.uminho.mei.bilhetica.service.ViagemService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/viagens")
public class ViagemController {

    private final ViagemService viagemService;

    public ViagemController(ViagemService viagemService) {
        this.viagemService = viagemService;
    }

    @GetMapping
    public ResponseEntity<List<ViagemResponse>> historico(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
            viagemService.historico(user.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViagemResponse> detalhe(
            @PathVariable UUID id) {
        return ResponseEntity.ok(viagemService.detalhe(id));
    }
}
