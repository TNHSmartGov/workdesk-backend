package com.tnh.baseware.core.configs;

import com.tnh.baseware.core.annotations.ApiOkResponse;
import com.tnh.baseware.core.components.CustomRequiredFieldConverter;
import com.tnh.baseware.core.properties.OpenApiProperties;
import com.tnh.baseware.core.utils.LogStyleHelper;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.List;

@Slf4j
@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "bearerAuth"))
@RequiredArgsConstructor
public class OpenApiConfiguration {

    private final OpenApiProperties openApiProperties;
    private final CustomRequiredFieldConverter customRequiredFieldConverter;

    @PostConstruct
    public void registerCustomConverter() {
        log.debug(LogStyleHelper.debug("Registering custom field converter for OpenAPI"));
        ModelConverters.getInstance().addConverter(customRequiredFieldConverter);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        log.debug(LogStyleHelper.debug("Building custom OpenAPI configuration"));

        var securityName = openApiProperties.getSecurity().getName();

        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .externalDocs(apiExternalDocs())
                .addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement()
                        .addList(securityName))
                .components(new Components()
                        .addSecuritySchemes(securityName, securityScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title(openApiProperties.getInfo().getTitle())
                .description(openApiProperties.getInfo().getDescription())
                .version(openApiProperties.getInfo().getVersion())
                .termsOfService(openApiProperties.getInfo().getTermsOfService())
                .contact(new Contact()
                        .name(openApiProperties.getContact().getName())
                        .email(openApiProperties.getContact().getEmail())
                        .url(openApiProperties.getContact().getUrl()))
                .license(new License()
                        .name(openApiProperties.getLicense().getName())
                        .url(openApiProperties.getLicense().getUrl()));
    }

    private List<Server> apiServers() {
        return openApiProperties.getServers().stream()
                .map(serverInfo -> new Server()
                        .description(serverInfo.getDescription())
                        .url(serverInfo.getUrl()))
                .toList();
    }

    private ExternalDocumentation apiExternalDocs() {
        return new ExternalDocumentation()
                .description(openApiProperties.getExternalDocs().getDescription())
                .url(openApiProperties.getExternalDocs().getUrl());
    }

    private SecurityScheme securityScheme() {
        var security = openApiProperties.getSecurity();
        return new SecurityScheme()
                .name(security.getName())
                .description(security.getDescription())
                .scheme(security.getScheme())
                .type(SecurityScheme.Type.valueOf(security.getType().toUpperCase()))
                .bearerFormat(security.getBearerFormat())
                .in(SecurityScheme.In.valueOf(security.getIn().toUpperCase()));
    }

    @Bean
    public OperationCustomizer apiOkResponseCustomizer() {
        return (operation, handlerMethod) -> {

            ApiOkResponse apiOk = handlerMethod.getMethodAnnotation(ApiOkResponse.class);
            if (apiOk == null) {
                return operation;
            }

            Schema<?> dataSchema = switch (apiOk.type()) {
                case OBJECT -> objectSchema(apiOk.value());
                case LIST -> listSchema(apiOk.value());
                case PAGE -> pageSchema(apiOk.value());
                case HATEOAS_PAGE -> hateoasPageSchema(apiOk.value());
            };

            Schema<?> responseSchema = baseEnvelopeSchema(dataSchema);

            operation.getResponses().addApiResponse("200",
                    new ApiResponse().description("Success")
                            .content(new Content().addMediaType(
                                    MediaType.APPLICATION_JSON_VALUE,
                                    new io.swagger.v3.oas.models.media.MediaType().schema(responseSchema)
                            ))
            );

            return operation;
        };
    }

    private Schema<?> baseEnvelopeSchema(Schema<?> dataSchema) {
        return new Schema<>()
                .type("object")
                .addProperty("message", new StringSchema())
                .addProperty("result", new BooleanSchema())
                .addProperty("code", new IntegerSchema())
                .addProperty("data", dataSchema);
    }

    private Schema<?> objectSchema(Class<?> dto) {
        return new Schema<>().$ref("#/components/schemas/" + dto.getSimpleName());
    }

    private Schema<?> listSchema(Class<?> dto) {
        return new ArraySchema().items(
                new Schema<>().$ref("#/components/schemas/" + dto.getSimpleName())
        );
    }

    private Schema<?> pageSchema(Class<?> dto) {
        return new Schema<>()
                .type("object")
                .addProperty("content", listSchema(dto))
                .addProperty("totalElements", new IntegerSchema())
                .addProperty("totalPages", new IntegerSchema())
                .addProperty("size", new IntegerSchema())
                .addProperty("number", new IntegerSchema());
    }

    private Schema<?> hateoasPageSchema(Class<?> dto) {
        return new Schema<>()
                .type("object")
                .addProperty("content", listSchema(dto))
                .addProperty("page", new Schema<>()
                        .addProperty("size", new IntegerSchema())
                        .addProperty("totalElements", new IntegerSchema())
                        .addProperty("totalPages", new IntegerSchema())
                        .addProperty("number", new IntegerSchema())
                )
                .addProperty("_links", new Schema<>()
                        .additionalProperties(new Schema<>().$ref(
                                "#/components/schemas/Link"
                        ))
                );
    }

    @Bean
    public OpenApiCustomizer hideHateoasLinks() {
        return openApi -> openApi.getComponents().getSchemas().values()
                .forEach(schema -> schema.getProperties()
                        .remove("_links"));
    }
}
