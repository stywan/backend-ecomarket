package com.ecomarket.backend.auth.service;

import com.ecomarket.backend.auth.DTO.FiscalProfileRequest;
import com.ecomarket.backend.auth.DTO.FiscalProfileResponse;
import com.ecomarket.backend.auth.model.FiscalProfile;
import com.ecomarket.backend.auth.model.User;
import com.ecomarket.backend.auth.repository.FiscalProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FiscalProfileServiceTest {

    @Mock
    private FiscalProfileRepository fiscalProfileRepository;

    @InjectMocks
    private FiscalProfileService fiscalProfileService;

    private User testUser;
    private FiscalProfileRequest newProfileRequest;
    private FiscalProfile existingProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        newProfileRequest = FiscalProfileRequest.builder()
                .rut("11.222.333-4")
                .businessName("New Business SA")
                .businessField("Technology")
                .fiscalAddress("New Street 123")
                .fiscalCommune("Santiago")
                .fiscalCity("Santiago")
                .build();

        FiscalProfileRequest existingProfileRequest = FiscalProfileRequest.builder()
                .rut("99.888.777-6")
                .businessName("Existing Business Ltda.")
                .businessField("Retail")
                .fiscalAddress("Old Avenue 456")
                .fiscalCommune("Providencia")
                .fiscalCity("Santiago")
                .build();

        existingProfile = FiscalProfile.builder()
                .id(10L)
                .rut("99.888.777-6")
                .businessName("Existing Business Ltda.")
                .businessField("Retail")
                .fiscalAddress("Old Avenue 456")
                .fiscalCommune("Providencia")
                .fiscalCity("Santiago")
                .user(testUser)
                .build();

        FiscalProfile savedNewProfile = FiscalProfile.builder()
                .id(1L)
                .rut("11.222.333-4")
                .businessName("New Business SA")
                .businessField("Technology")
                .fiscalAddress("New Street 123")
                .fiscalCommune("Santiago")
                .fiscalCity("Santiago")
                .user(testUser)
                .build();

        FiscalProfile updatedProfile = FiscalProfile.builder()
                .id(10L)
                .rut("99.888.777-6")
                .businessName("Updated Business SA")
                .businessField("Services")
                .fiscalAddress("Updated Street 789")
                .fiscalCommune("Las Condes")
                .fiscalCity("Santiago")
                .user(testUser)
                .build();
    }

    @Test
    void addOrUpdateFiscalProfile_shouldAddNewProfile_whenRutNotFoundForUser() {
        when(fiscalProfileRepository.findByUser(testUser)).thenReturn(Collections.emptyList());
        when(fiscalProfileRepository.save(any(FiscalProfile.class))).thenAnswer(invocation -> {
            FiscalProfile fp = invocation.getArgument(0);
            if (fp.getId() == null) fp.setId(1L); // Simulate ID generation for new entity
            return fp;
        });

        FiscalProfileResponse response = fiscalProfileService.addOrUpdateFiscalProfile(testUser, newProfileRequest);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(newProfileRequest.getRut(), response.getRut());
        assertEquals(newProfileRequest.getBusinessName(), response.getBusinessName());
        verify(fiscalProfileRepository, times(1)).findByUser(testUser);
        verify(fiscalProfileRepository, times(1)).save(any(FiscalProfile.class));
    }

    @Test
    void addOrUpdateFiscalProfile_shouldUpdateExistingProfile_whenRutFoundForUser() {
        when(fiscalProfileRepository.findByUser(testUser)).thenReturn(Arrays.asList(existingProfile));
        when(fiscalProfileRepository.save(any(FiscalProfile.class))).thenAnswer(invocation -> {
            FiscalProfile fp = invocation.getArgument(0);
            // Simulate update: ensure ID is preserved
            assertEquals(existingProfile.getId(), fp.getId());
            return fp;
        });

        FiscalProfileRequest updateRequest = FiscalProfileRequest.builder()
                .rut("99.888.777-6") // Same RUT as existingProfile
                .businessName("Updated Business Name")
                .businessField("New Field")
                .fiscalAddress("Updated Address")
                .fiscalCommune("Updated Commune")
                .fiscalCity("Updated City")
                .build();

        FiscalProfileResponse response = fiscalProfileService.addOrUpdateFiscalProfile(testUser, updateRequest);

        assertNotNull(response);
        assertEquals(existingProfile.getId(), response.getId());
        assertEquals(updateRequest.getRut(), response.getRut());
        assertEquals(updateRequest.getBusinessName(), response.getBusinessName());
        verify(fiscalProfileRepository, times(1)).findByUser(testUser);
        verify(fiscalProfileRepository, times(1)).save(any(FiscalProfile.class));
    }

    @Test
    void listUserProfiles_shouldReturnListOfProfiles_whenUserHasProfiles() {
        FiscalProfile profile1 = FiscalProfile.builder().id(1L).rut("11.111.111-1").user(testUser).build();
        FiscalProfile profile2 = FiscalProfile.builder().id(2L).rut("22.222.222-2").user(testUser).build();

        when(fiscalProfileRepository.findByUser(testUser)).thenReturn(Arrays.asList(profile1, profile2));

        List<FiscalProfileResponse> responses = fiscalProfileService.listUserProfiles(testUser);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("22.222.222-2", responses.get(1).getRut());
        verify(fiscalProfileRepository, times(1)).findByUser(testUser);
    }

    @Test
    void listUserProfiles_shouldReturnEmptyList_whenUserHasNoProfiles() {
        when(fiscalProfileRepository.findByUser(testUser)).thenReturn(Collections.emptyList());

        List<FiscalProfileResponse> responses = fiscalProfileService.listUserProfiles(testUser);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(fiscalProfileRepository, times(1)).findByUser(testUser);
    }
}
