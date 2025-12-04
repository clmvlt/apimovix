package bzh.stack.apimovix.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashMap;
import java.util.Map;

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
        boolean isProduction = !serverAddress.contains("192.168.") && !serverAddress.contains("127.0.") && !serverAddress.contains("localhost") && !serverAddress.contains("0.0.0.0");

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

    @Bean
    public GroupedOpenApi publicApi() {
        boolean isProduction = serverAddress.contains("api.movix.fr");

        if (isProduction) {
            // En production (api.movix.fr), afficher uniquement le controller Importer
            return GroupedOpenApi.builder()
                    .group("public")
                    .pathsToMatch("/**")
                    .packagesToScan("bzh.stack.apimovix.controller")
                    .addOpenApiCustomizer(openApi -> {
                        // Filtrer les paths pour ne garder que ceux du controller Importer
                        openApi.getPaths().entrySet().removeIf(entry ->
                            !entry.getKey().startsWith("/command/send") &&
                            !entry.getKey().startsWith("/package/getLabel")
                        );

                        // Masquer tous les tags/sections sauf "Importer"
                        if (openApi.getTags() != null) {
                            openApi.getTags().removeIf(tag -> !"Importer".equals(tag.getName()));
                        }

                        // Conserver uniquement les schémas (DTOs) utilisés par le controller Importer
                        if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                            @SuppressWarnings("rawtypes")
                            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
                            @SuppressWarnings("rawtypes")
                            Map<String, Schema> importerSchemas = new HashMap<>();

                            // Liste des schémas utilisés par le controller Importer
                            String[] allowedSchemas = {
                                "SendCommandRequestDTO",
                                "SendCommandResponseDTO",
                                "SenderDTO",
                                "CommandImporterDTO",
                                "PharmacyCreateDTO",
                                "PackageDTO",
                                "PackageStatusDTO",
                                "ProfilDTO"
                            };

                            // Conserver uniquement les schémas autorisés
                            for (String schemaName : allowedSchemas) {
                                if (schemas.containsKey(schemaName)) {
                                    importerSchemas.put(schemaName, schemas.get(schemaName));
                                }
                            }

                            openApi.setComponents(new Components()
                                .securitySchemes(openApi.getComponents().getSecuritySchemes())
                                .schemas(importerSchemas)
                            );
                        }
                    })
                    .build();
        } else {
            // En développement, afficher tous les controllers
            return GroupedOpenApi.builder()
                    .group("public")
                    .pathsToMatch("/**")
                    .build();
        }
    }
} 