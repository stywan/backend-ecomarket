package com.ecomarket.backend.auth.service;

import com.ecomarket.backend.auth.DTO.FiscalProfileRequest;
import com.ecomarket.backend.auth.DTO.FiscalProfileResponse;
import com.ecomarket.backend.auth.model.FiscalProfile;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.FiscalProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FiscalProfileService {

    private final FiscalProfileRepository fiscalProfileRepository;

    public FiscalProfileResponse addOrUpdateFiscalProfile(User user, FiscalProfileRequest request) {
        FiscalProfile profile = fiscalProfileRepository.findByUser(user).stream()
                .filter(fp -> fp.getRut().equals(request.getRut()))
                .findFirst()
                .orElse(new FiscalProfile());

        profile.setRut(request.getRut());
        profile.setBusinessName(request.getBusinessName());
        profile.setBusinessField(request.getBusinessField());
        profile.setFiscalAddress(request.getFiscalAddress());
        profile.setFiscalCommune(request.getFiscalCommune());
        profile.setFiscalCity(request.getFiscalCity());
        profile.setUser(user);

        FiscalProfile saved = fiscalProfileRepository.save(profile);

        return FiscalProfileResponse.builder()
                .id(saved.getId())
                .rut(saved.getRut())
                .businessName(saved.getBusinessName())
                .businessField(saved.getBusinessField())
                .fiscalAddress(saved.getFiscalAddress())
                .fiscalCommune(saved.getFiscalCommune())
                .fiscalCity(saved.getFiscalCity())
                .build();
    }

    public List<FiscalProfileResponse> listUserProfiles(User user) {
        return fiscalProfileRepository.findByUser(user).stream()
                .map(fp -> FiscalProfileResponse.builder()
                        .id(fp.getId())
                        .rut(fp.getRut())
                        .businessName(fp.getBusinessName())
                        .businessField(fp.getBusinessField())
                        .fiscalAddress(fp.getFiscalAddress())
                        .fiscalCommune(fp.getFiscalCommune())
                        .fiscalCity(fp.getFiscalCity())
                        .build())
                .collect(Collectors.toList());
    }
}