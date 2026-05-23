package co.edu.unbosque.proyectofinal.configuration;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Clase de configuración para la documentación OpenAPI/Swagger de la
 * aplicación.
 * <p>
 * Define el esquema de seguridad global de tipo HTTP Bearer JWT que será
 * usado en la interfaz de Swagger UI para autenticar peticiones a los
 * endpoints protegidos. Con esta configuración, Swagger UI mostrará un
 * botón "Authorize" donde se puede pegar el token JWT obtenido del
 * endpoint de login.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Configuration
@OpenAPIDefinition(info = @Info(
		title = "Plataforma de Conversión de Archivos",
		version = "1.0",
		description = "API REST para convertir archivos de audio, video e imagen "
				+ "entre distintos formatos. Autenticación basada en JWT."))
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT")
public class OpenApiConfig {

}