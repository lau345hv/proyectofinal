package co.edu.unbosque.proyectofinal.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador de diagnóstico para verificar la configuración del sistema
 * sin necesidad de revisar logs ni el código fuente.
 * <p>
 * Todos los endpoints requieren autenticación con rol ADMIN, ya que
 * exponen información sensible sobre la configuración del servidor.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 2.1
 */
@RestController
@RequestMapping("/diagnostico")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Diagnóstico",
		description = "Endpoints para verificar la configuración del servidor (SOLO ADMIN)")
public class DiagnosticoController {

	@Value("${cloudconvert.api.key:}")
	private String apiKeyDesdeProperties;

	@Value("${cloudconvert.sandbox:true}")
	private boolean sandbox;

	private final RestTemplate restTemplate;

	/**
	 * Crea el controlador inyectando el {@link RestTemplate} necesario para
	 * realizar peticiones HTTP a la API de CloudConvert.
	 *
	 * @param restTemplate cliente HTTP de Spring usado para probar la conectividad
	 *                     con la API de CloudConvert
	 */
	public DiagnosticoController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * Verifica y muestra la configuración actual de CloudConvert sin realizar
	 * ninguna petición a la API externa.
	 * <p>
	 * Informa el ambiente activo (SANDBOX o PRODUCCION), la URL base de la API,
	 * si la API key está configurada, su longitud y los últimos 6 caracteres
	 * para confirmar visualmente cuál key está en uso, y la fuente desde donde
	 * se leyó ({@code application.properties} o variable de entorno).
	 * </p>
	 *
	 * @return {@code 200 OK} con un mapa de información diagnóstica sobre la
	 *         configuración de CloudConvert
	 */
	@GetMapping("/cloudconvert")
	@Operation(
		summary = "Verificar configuración de CloudConvert (SOLO ADMIN)",
		responses = {
			@ApiResponse(responseCode = "200", description = "Configuración leída correctamente",
				content = @Content(mediaType = "application/json",
					examples = @ExampleObject(value = """
						{
						  "ambiente": "SANDBOX",
						  "urlApi": "https://api.sandbox.cloudconvert.com/v2",
						  "urlDashboardCorrecto": "https://sandbox.cloudconvert.com/dashboard/api/v2/keys",
						  "apiKeyConfigurada": true,
						  "longitudKey": 512,
						  "ultimos6Caracteres": "...abc123",
						  "origen": "application.properties"
						}
						""")))
		}
	)
	public ResponseEntity<Map<String, Object>> verificarCloudConvert() {
		Map<String, Object> resultado = new LinkedHashMap<>();
		String key = resolverApiKey();
		String ambiente = sandbox ? "SANDBOX" : "PRODUCCION";

		resultado.put("ambiente", ambiente);
		resultado.put("urlApi", sandbox
				? "https://api.sandbox.cloudconvert.com/v2"
				: "https://api.cloudconvert.com/v2");
		resultado.put("urlDashboardCorrecto", sandbox
				? "https://sandbox.cloudconvert.com/dashboard/api/v2/keys"
				: "https://cloudconvert.com/dashboard/api/v2/keys");

		if (key == null || key.isEmpty()) {
			resultado.put("apiKeyConfigurada", false);
			resultado.put("mensaje",
					"La API key NO está configurada. Agrega cloudconvert.api.key "
							+ "al application.properties o configura la variable de "
							+ "entorno CLOUDCONVERT_API_KEY.");
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		}

		resultado.put("apiKeyConfigurada", true);
		resultado.put("longitudKey", key.length());
		resultado.put("ultimos6Caracteres", "..."
				+ key.substring(Math.max(0, key.length() - 6)));
		resultado.put("origen", apiKeyDesdeProperties != null
				&& !apiKeyDesdeProperties.isBlank()
						? "application.properties"
						: "variable de entorno CLOUDCONVERT_API_KEY");
		return new ResponseEntity<>(resultado, HttpStatus.OK);
	}

	/**
	 * Realiza una petición real al endpoint {@code /users/me} de CloudConvert
	 * para verificar si la API key configurada es válida en el ambiente activo.
	 * <p>
	 * Un código HTTP {@code 200} de CloudConvert indica que la key es correcta.
	 * Un código {@code 401} indica que la key no corresponde al ambiente
	 * seleccionado (sandbox vs. producción son keys distintas). Cualquier otra
	 * excepción se captura y se informa en el mapa de respuesta sin propagar
	 * el error al cliente.
	 * </p>
	 *
	 * @return {@code 200 OK} con el resultado de la prueba, incluyendo el estado,
	 *         el código HTTP devuelto por CloudConvert y un mensaje explicativo
	 */
	@GetMapping("/cloudconvert/probar")
	@Operation(
		summary = "Probar la API key contra CloudConvert (SOLO ADMIN)",
		responses = {
			@ApiResponse(responseCode = "200", description = "Resultado de la prueba contra CloudConvert",
				content = @Content(mediaType = "application/json",
					examples = @ExampleObject(value = """
						{
						  "ambiente": "SANDBOX",
						  "estado": "OK",
						  "codigoHttp": 200,
						  "respuesta": "{ ...datos del usuario CloudConvert... }",
						  "mensaje": "La API key es VÁLIDA para el ambiente SANDBOX."
						}
						""")))
		}
	)
	public ResponseEntity<Map<String, Object>> probarCloudConvert() {
		Map<String, Object> resultado = new LinkedHashMap<>();
		String key = resolverApiKey();
		String urlBase = sandbox
				? "https://api.sandbox.cloudconvert.com/v2"
				: "https://api.cloudconvert.com/v2";
		resultado.put("ambiente", sandbox ? "SANDBOX" : "PRODUCCION");

		if (key == null || key.isEmpty()) {
			resultado.put("estado", "ERROR");
			resultado.put("mensaje", "No hay API key configurada.");
			return new ResponseEntity<>(resultado, HttpStatus.OK);
		}

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(key);
			HttpEntity<Void> entity = new HttpEntity<>(headers);
			ResponseEntity<String> respuesta = restTemplate.exchange(
					urlBase + "/users/me", HttpMethod.GET, entity, String.class);
			resultado.put("estado", "OK");
			resultado.put("codigoHttp", respuesta.getStatusCode().value());
			resultado.put("respuesta", respuesta.getBody());
			resultado.put("mensaje",
					"La API key es VÁLIDA para el ambiente "
							+ (sandbox ? "SANDBOX" : "PRODUCCION") + ".");
		} catch (HttpStatusCodeException e) {
			resultado.put("estado", "ERROR");
			resultado.put("codigoHttp", e.getStatusCode().value());
			resultado.put("respuestaCloudConvert", e.getResponseBodyAsString());
			if (e.getStatusCode().value() == 401) {
				resultado.put("mensaje",
						"La API key NO es válida para el ambiente "
								+ (sandbox ? "SANDBOX" : "PRODUCCION")
								+ ". Las keys de sandbox y producción son DISTINTAS.");
			} else {
				resultado.put("mensaje",
						"CloudConvert respondió con error " + e.getStatusCode());
			}
		} catch (Exception e) {
			resultado.put("estado", "ERROR");
			resultado.put("mensaje", "Excepción inesperada: " + e.getMessage());
		}
		return new ResponseEntity<>(resultado, HttpStatus.OK);
	}

	/**
	 * Resuelve la API key de CloudConvert con la siguiente prioridad:
	 * primero intenta obtenerla desde {@code application.properties}; si no
	 * está configurada ahí, busca la variable de entorno
	 * {@code CLOUDCONVERT_API_KEY}.
	 *
	 * @return la API key como cadena de texto, o una cadena vacía si no está
	 *         configurada en ninguna fuente
	 */
	private String resolverApiKey() {
		if (apiKeyDesdeProperties != null && !apiKeyDesdeProperties.isBlank()) {
			return apiKeyDesdeProperties.trim();
		}
		String env = System.getenv("CLOUDCONVERT_API_KEY");
		return env != null ? env.trim() : "";
	}
}