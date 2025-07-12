package com.ecomarket.backend.auth.assemblers;
import com.ecomarket.backend.auth.DTO.UserResponse;
import com.ecomarket.backend.auth.controller.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class UserAssembler implements RepresentationModelAssembler<UserResponse, EntityModel<UserResponse>> {

    @Override
    public EntityModel<UserResponse> toModel(UserResponse user) {
        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).getProfile(null)).withSelfRel(),
                linkTo(methodOn(UserController.class).updateProfile(null, null)).withRel("updateProfile"),
                linkTo(methodOn(UserController.class).updatePassword(null, null)).withRel("updatePassword")
        );
    }
}