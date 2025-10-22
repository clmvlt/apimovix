package bzh.stack.apimovix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Value("${server.address}")
    private String serverAddress;

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("Bearer");

        // Détection automatique du protocole selon l'adresse
        String protocol = "http";
        boolean isProduction = !serverAddress.contains("192.168.") && !serverAddress.contains("127.0.") && !serverAddress.contains("localhost");

        if (isProduction) {
            protocol = "https";
        }

        // Construction de l'URL serveur - sans port en production
        String serverUrl;
        if (isProduction) {
            serverUrl = protocol + "://" + serverAddress;
        } else {
            serverUrl = protocol + "://" + serverAddress + ":" + serverPort;
        }

        if (protocol.equals("https") || serverAddress.contains("api.movix.fr")) {
            securityScheme.description("Format : Bearer {TOKEN_HERE}");
        } else {
            securityScheme.description("Format : Bearer {TOKEN_HERE}\nToken par défaut pour le développement : 60834d80ed7a796eb119f71ed3be010b1ff768527e6e1e5a0ee88de32b7b7854");
        }

        return new OpenAPI()
                .openapi("3.0.0")
                .info(new Info()
                        .title("API Movix")
                        .version("1.0")
                        .description("API de gestion pour Movix"))
                .addServersItem(new Server().url(serverUrl).description("API Server"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", securityScheme);
    }
} 