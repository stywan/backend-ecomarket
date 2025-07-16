package com.ecomarket.backend.cart_order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cartOrderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cart & Order Service API")
                        .version("v1.0")
                        .description("Documentación del microservicio para gestión de carritos de compra y órdenes.")
                        .contact(new Contact()
                                .name("Equipo EcoMarket")
                                .email("soporte@ecomarket.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
