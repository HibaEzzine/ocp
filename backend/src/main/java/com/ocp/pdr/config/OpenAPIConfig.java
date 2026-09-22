package com.ocp.pdr.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API PDR - Pièces de Rechange")
                        .version("1.0.0")
                        .description("API de gestion du système de pièces de rechange (PDR) avec approvisionnement automatisé")
                        .contact(new Contact()
                                .name("Support OCP")
                                .email("support@ocp.com")
                                .url("https://ocp.com"))
                        .license(new License()
                                .name("Propriétaire OCP")
                                .url("https://ocp.com/license")));
    }
}
