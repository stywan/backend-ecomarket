package com.ecomarket.backend.auth.service;
import com.ecomarket.backend.auth.DTO.AddressRequest;
import com.ecomarket.backend.auth.DTO.AddressResponse;
import com.ecomarket.backend.auth.model.Address;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressResponse addAddress(User user, AddressRequest request) {
        Address address = Address.builder()
                .street(request.getStreet())
                .number(request.getNumber())
                .commune(request.getCommune())
                .postalCode(request.getPostalCode())
                .user(user)
                .build();

        Address saved = addressRepository.save(address);

        return AddressResponse.builder()
                .id(saved.getId())
                .street(saved.getStreet())
                .number(saved.getNumber())
                .commune(saved.getCommune())
                .postalCode(saved.getPostalCode())
                .build();
    }

    public AddressResponse updateAddress(User user, Long id, AddressRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        address.setStreet(request.getStreet());
        address.setNumber(request.getNumber());
        address.setCommune(request.getCommune());
        address.setPostalCode(request.getPostalCode());

        Address saved = addressRepository.save(address);

        return AddressResponse.builder()
                .id(saved.getId())
                .street(saved.getStreet())
                .number(saved.getNumber())
                .commune(saved.getCommune())
                .postalCode(saved.getPostalCode())
                .build();
    }

    public void deleteAddress(User user, Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        addressRepository.delete(address);
    }

    public List<AddressResponse> getUserAddresses(User user) {
        return addressRepository.findByUser(user).stream()
                .map(addr -> AddressResponse.builder()
                        .id(addr.getId())
                        .street(addr.getStreet())
                        .number(addr.getNumber())
                        .commune(addr.getCommune())
                        .postalCode(addr.getPostalCode())
                        .build())
                .collect(Collectors.toList());
    }
}
