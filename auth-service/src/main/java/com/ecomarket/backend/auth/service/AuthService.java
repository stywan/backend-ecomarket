package com.ecomarket.backend.auth.service;
import com.ecomarket.backend.auth.DTO.LoginRequest;
import com.ecomarket.backend.auth.DTO.LoginResponse;
import com.ecomarket.backend.auth.DTO.RegisterRequest;
import com.ecomarket.backend.auth.config.JwtProvider;
import com.ecomarket.backend.auth.model.Role;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.RoleRepository;
import com.ecomarket.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByRoleName("CLIENT")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status("ACTIVE")
                .build();

        userRepository.save(user);
        String token = jwtProvider.generateToken(user.getEmail());
        return new LoginResponse(token);
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
        String token = jwtProvider.generateToken(request.getEmail());
        return new LoginResponse(token);
    }
}