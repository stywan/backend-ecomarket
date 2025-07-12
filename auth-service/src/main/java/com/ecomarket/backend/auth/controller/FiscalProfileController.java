package com.ecomarket.backend.auth.controller;

import com.ecomarket.backend.auth.DTO.FiscalProfileRequest;
import com.ecomarket.backend.auth.DTO.FiscalProfileResponse;
import com.ecomarket.backend.auth.assemblers.FiscalProfileAssembler;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.security.AuthenticatedUserProvider;
import com.ecomarket.backend.auth.service.FiscalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/fiscal-profiles")
@RequiredArgsConstructor
public class FiscalProfileController {

    private final FiscalProfileService fiscalProfileService;
    private final FiscalProfileAssembler fiscalProfileAssembler;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @PostMapping
    public EntityModel<FiscalProfileResponse> addOrUpdateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @Valid @RequestBody FiscalProfileRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        FiscalProfileResponse response = fiscalProfileService.addOrUpdateFiscalProfile(realUser, request);
        return fiscalProfileAssembler.toModel(response);
    }

    @GetMapping
    public CollectionModel<EntityModel<FiscalProfileResponse>> listProfiles(@AuthenticationPrincipal UserDetails userDetails) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        List<EntityModel<FiscalProfileResponse>> profiles = fiscalProfileService.listUserProfiles(realUser)
                .stream()
                .map(fiscalProfileAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(profiles);
    }
}