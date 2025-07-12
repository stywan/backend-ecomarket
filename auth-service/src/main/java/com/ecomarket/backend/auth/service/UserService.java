package com.ecomarket.backend.auth.service;
import com.ecomarket.backend.auth.DTO.PasswordUpdateRequest;
import com.ecomarket.backend.auth.DTO.UserUpdateRequest;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateProfile(User user, UserUpdateRequest request) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        userRepository.save(user);
    }

    public void updatePassword(User user, PasswordUpdateRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
