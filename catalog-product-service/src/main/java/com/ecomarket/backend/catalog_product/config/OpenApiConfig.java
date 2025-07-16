package com.ecomarket.backend.catalog_product.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "EcoMarket - Catalog Product Service API",
        version = "1.0",
        description = "Microservicio responsable de gestionar el catálogo de productos.",
        contact = @Contact(
            name = "Team EcoMarket",
            email = "ecomarket@example.com",
            url = "https://ecomarket.cl"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8082", description = "Servidor local de desarrollo")
    }
)
public class OpenApiConfig {
}

