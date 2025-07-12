package com.ecomarket.backend.auth.assemblers;
import com.ecomarket.backend.auth.DTO.FiscalProfileResponse;
import com.ecomarket.backend.auth.controller.FiscalProfileController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class FiscalProfileAssembler implements RepresentationModelAssembler<FiscalProfileResponse, EntityModel<FiscalProfileResponse>> {

    @Override
    public EntityModel<FiscalProfileResponse> toModel(FiscalProfileResponse profile) {
        return EntityModel.of(profile,
                linkTo(FiscalProfileController.class).slash(profile.getId()).withSelfRel(),
                linkTo(FiscalProfileController.class).withRel("allProfiles")
        );
    }
}