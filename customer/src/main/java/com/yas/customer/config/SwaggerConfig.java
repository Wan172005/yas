package com.yas.customer.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Customer Service API",
        description = "Customer API documentation",
        version = "1.0"
    )
)
@SecurityScheme(
    name = "oauth2_bearer",
    type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(
        authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oauthflow.authorization-url}",
            tokenUrl = "${springdoc.oauthflow.token-url}",
            scopes = {
                @OAuthScope(name = "openid", description = "openid")
            }
        )
    )
)
public class SwaggerConfig {
}
