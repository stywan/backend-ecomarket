package com.ecomarket.backend.auth.controller;
import com.ecomarket.backend.auth.DTO.AddressRequest;
import com.ecomarket.backend.auth.DTO.AddressResponse;
import com.ecomarket.backend.auth.assemblers.AddressAssembler;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.security.AuthenticatedUserProvider;
import com.ecomarket.backend.auth.service.AddressService;
import com.ecomarket.backend.auth.service.UserService;
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
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final AddressAssembler addressAssembler;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @PostMapping
    public EntityModel<AddressResponse> addAddress(@AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody AddressRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        AddressResponse response = addressService.addAddress(realUser, request);
        return addressAssembler.toModel(response);
    }

    @PutMapping("/{id}")
    public EntityModel<AddressResponse> updateAddress(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody AddressRequest request) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        AddressResponse response = addressService.updateAddress(realUser, id, request);
        return addressAssembler.toModel(response);
    }

    @DeleteMapping("/{id}")
    public void deleteAddress(@AuthenticationPrincipal UserDetails userDetails,
                              @PathVariable Long id) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        addressService.deleteAddress(realUser, id);
    }

    @GetMapping
    public CollectionModel<EntityModel<AddressResponse>> getAllAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        User realUser = authenticatedUserProvider.getCurrentUser(userDetails);
        List<EntityModel<AddressResponse>> addresses = addressService.getUserAddresses(realUser)
                .stream()
                .map(addressAssembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(addresses);
    }
}