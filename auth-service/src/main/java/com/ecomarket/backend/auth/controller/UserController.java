package com.ecomarket.backend.auth.controller;
import com.ecomarket.backend.auth.DTO.PasswordUpdateRequest;
import com.ecomarket.backend.auth.DTO.UserResponse;
import com.ecomarket.backend.auth.DTO.UserUpdateRequest;
import com.ecomarket.backend.auth.assemblers.UserAssembler;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.security.AuthenticatedUserProvider;
import com.ecomarket.backend.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAssembler userAssembler;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @GetMapping("/me")
    public EntityModel<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        UserResponse response = userService.getUserProfile(realUser);
        return userAssembler.toModel(response);
    }

    @PutMapping("/me")
    public EntityModel<UserResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody UserUpdateRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        userService.updateProfile(realUser, request);
        UserResponse response = userService.getUserProfile(realUser);
        return userAssembler.toModel(response);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody PasswordUpdateRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        userService.updatePassword(realUser, request);
        return ResponseEntity.noContent()
                .header("X-Message", "Contraseña actualizada con éxito")
                .build();
    }

}