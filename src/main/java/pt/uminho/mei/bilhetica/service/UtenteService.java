// service/UtenteService.java
package pt.uminho.mei.bilhetica.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.uminho.mei.bilhetica.entity.Utente;
import pt.uminho.mei.bilhetica.repository.UtenteRepository;

@Service
public class UtenteService implements UserDetailsService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository,
                         PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Utente não encontrado: " + email));

        return User.builder()
                .username(utente.getEmail())
                .password(utente.getPasswordHash())
                .roles("UTENTE")
                .build();
    }

    public Utente registar(String nome, String email,
                           String telemovel, String password) {
        if (utenteRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já registado");
        }

        Utente utente = Utente.builder()
                .nome(nome)
                .email(email)
                .telemovel(telemovel)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        return utenteRepository.save(utente);
    }
}
