package com.aurionpro.app.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
 info = @Info(
     title = "Metro Ticket Reservation System API",
     description = "Backend APIs for the Metro Ticket Reservation System.",
     version = "1.0.0"
 ),
 servers = {
     @Server(
         description = "Local Development Server",
         url = "http://localhost:8080"
     )
 }
)
@SecurityScheme(
 name = "bearerAuth", 
 description = "JWT Authorization header using the Bearer scheme.",
 scheme = "bearer",
 type = SecuritySchemeType.HTTP,
 bearerFormat = "JWT",
 in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
 
}
