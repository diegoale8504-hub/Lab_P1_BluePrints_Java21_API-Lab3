package edu.eci.arsw.blueprints.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("ARSW Blueprints REST API")
                        .version("v1.0.0")
                        .description("API REST para la gestión, consulta y procesamiento de planos arquitectónicos (Blueprints). Desarrollado con Java 21 y Spring Boot 3.3.x.")
                        .contact(new Contact()
                                .name("Juan Camilo Melo Cupitra & Diego Rozo")
                                .email("juan.melo-c@mail.escuelaing.edu.co"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}

