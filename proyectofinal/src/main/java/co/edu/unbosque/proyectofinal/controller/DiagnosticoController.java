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
 * @version 2.0
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

	public DiagnosticoController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/cloudconvert")
	@Operation(summary = "Verificar configuración de CloudConvert (SOLO ADMIN)")
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

	@GetMapping("/cloudconvert/probar")
	@Operation(summary = "Probar la API key contra CloudConvert (SOLO ADMIN)")
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

	private String resolverApiKey() {
		if (apiKeyDesdeProperties != null && !apiKeyDesdeProperties.isBlank()) {
			return apiKeyDesdeProperties.trim();
		}
		String env = System.getenv("CLOUDCONVERT_API_KEY");
		return env != null ? env.trim() : "";
	}
}