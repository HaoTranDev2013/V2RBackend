package com.v2r.v2rbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the security scheme name
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                // Add security requirement globally (can be overridden per endpoint)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // Define the security scheme
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token received from login API")))
                // API information
                .info(new Info()
                        .title("V2R Backend API")
                        .version("1.0")
                        .description("API documentation for V2R Backend application\n\n" +
                                "**Authentication:**\n" +
                                "1. Use POST /api/auth/login to get JWT token\n" +
                                "2. Click 'Authorize' button (🔓 icon at the top)\n" +
                                "3. Enter: Bearer YOUR_TOKEN_HERE\n" +
                                "4. Click 'Authorize' and 'Close'\n" +
                                "5. Now you can test protected endpoints!")
                        .contact(new Contact()
                                .name("V2R Team")
                                .email("support@v2r.com")));
    }
}
