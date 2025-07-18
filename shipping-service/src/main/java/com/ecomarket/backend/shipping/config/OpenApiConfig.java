package com.ecomarket.backend.shipping.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "EcoMarket Shipping API",
                version = "1.0",
                description = "API para la gestión de envíos de EcoMarket, incluyendo seguimiento y estado.",
                termsOfService = "http://ecomarket.com/terms",
                contact = @Contact(
                        name = "Equipo de Desarrollo EcoMarket",
                        email = "dev@ecomarket.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8084", description = "Servidor de Desarrollo Local"),
        }
)
public class OpenApiConfig {
}
