package com.ecomarket.backend.auth.service;
import com.ecomarket.backend.auth.DTO.LoginRequest;
import com.ecomarket.backend.auth.DTO.LoginResponse;
import com.ecomarket.backend.auth.DTO.RegisterRequest;
import com.ecomarket.backend.auth.config.JwtService;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role clientRole = roleRepository.findByRoleName("CLIENT")
                .orElseThrow(() -> new RuntimeException("Role CLIENT not found"));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(clientRole)
                .status("ACTIVE")
                .build();

        userRepository.save(user);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        java.util.Collections.emptyList()
                );
        String jwt = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(jwt)
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        java.util.Collections.emptyList()
                );
        String jwt = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .token(jwt)
                .build();
    }
}