package com.ecomarket.backend.auth.assemblers;
import com.ecomarket.backend.auth.DTO.AddressResponse;
import com.ecomarket.backend.auth.controller.AddressController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AddressAssembler implements RepresentationModelAssembler<AddressResponse, EntityModel<AddressResponse>> {

    @Override
    public EntityModel<AddressResponse> toModel(AddressResponse address) {
        return EntityModel.of(address,
                linkTo(AddressController.class).slash(address.getId()).withSelfRel(),
                linkTo(AddressController.class).withRel("allAddresses"),
                linkTo(AddressController.class).slash(address.getId()).withRel("update"),
                linkTo(AddressController.class).slash(address.getId()).withRel("delete")
        );
    }
}