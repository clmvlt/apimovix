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

    @Value("${server.env:dev}")
    private String serverEnv;

    @Value("${app.base-url}")
    private String apiBaseUrl;

    @Value("${app.site-url}")
    private String siteUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("Bearer");

        // Détection automatique du protocole selon l'environnement
        String protocol = "http";
        boolean isProduction = "prod".equals(serverEnv);
        boolean isBeta = "beta".equals(serverEnv);
        boolean isDemo = "demo".equals(serverEnv);

        if (isProduction || isBeta || isDemo) {
            protocol = "https";
        }

        // Construction de l'URL serveur - sans port en production
        String serverUrl;
        if (isProduction || isBeta || isDemo) {
            serverUrl = protocol + "://" + serverAddress;
        } else {
            serverUrl = protocol + "://" + serverAddress + ":" + serverPort;
        }

        // Configuration du titre et de la description selon l'environnement
        String title;
        String description;
        String serverDescription;

        if (isProduction) {
            title = "API Movix - Production";
            description = """
                **API de gestion pour Movix**

                - **API** : https://api.movix.fr
                - **Site** : https://movix.fr""";
            serverDescription = "Serveur de Production";
            securityScheme.description("Format : Bearer {TOKEN_HERE}");
        } else if (isBeta) {
            title = "API Movix - Beta";
            description = """
                **API de gestion pour Movix - Environnement BETA**

                - **API** : https://api.beta.movix.fr
                - **Site** : https://beta.movix.fr

                🧪 Cet environnement est dédié aux tests avant mise en production.""";
            serverDescription = "Serveur Beta";
            securityScheme.description("Format : Bearer {TOKEN_HERE}");
        } else if (isDemo) {
            title = "API Movix - Démo";
            description = """
                **API de gestion pour Movix - Environnement de DÉMONSTRATION**

                - **API** : https://api.demo.movix.fr
                - **Site** : https://demo.movix.fr

                🎭 Cet environnement est dédié aux démonstrations clients.""";
            serverDescription = "Serveur Démo";
            securityScheme.description("Format : Bearer {TOKEN_HERE}");
        } else {
            title = "API Movix - Développement";
            description = """
                **API de gestion pour Movix - Environnement de DÉVELOPPEMENT**

                - **API** : %s
                - **Site** : %s

                🔧 Cet environnement est dédié au développement local.""".formatted(apiBaseUrl, siteUrl);
            serverDescription = "Serveur de Développement";
            securityScheme.description("Format : Bearer {TOKEN_HERE}\nToken par défaut pour le développement : 60834d80ed7a796eb119f71ed3be010b1ff768527e6e1e5a0ee88de32b7b7854");
        }

        return new OpenAPI()
                .openapi("3.0.0")
                .info(new Info()
                        .title(title)
                        .version("1.0")
                        .description(description))
                .addServersItem(new Server().url(serverUrl).description(serverDescription))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", securityScheme);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        boolean isProduction = "prod".equals(serverEnv);

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
                                "RecipientImporterDTO",
                                "CommandImporterDTO",
                                "PackageImporterDTO",
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