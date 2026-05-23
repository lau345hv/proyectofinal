package co.edu.unbosque.proyectofinal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;

/**
 * Servicio que valida la existencia real de un correo electrónico
 * consultando la API pública y gratuita de <b>pingutil.com</b>, la cual
 * verifica si el dominio del correo tiene registros MX válidos y si
 * el correo es de un proveedor desechable.
 * <p>
 * El endpoint utilizado es:
 * {@code https://api.eva.pingutil.com/email?email={correo}}
 * y NO requiere API key.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Service
public class EmailValidacionService {

	private static final String URL_VALIDACION =
			"https://api.eva.pingutil.com/email?email=";

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LanzadorDeExcepcion lanzador;

	private final Gson gson = new Gson();

	/**
	 * Valida que el correo electrónico exista realmente verificando el
	 * dominio y los registros MX a través de la API externa.
	 *
	 * @param correo dirección de correo electrónico a validar
	 * @throws co.edu.unbosque.proyectofinal.exception.CorreoNoExisteException
	 *         si el correo no existe o no es válido
	 */
	public void validarExistenciaCorreo(String correo) {
		if (correo == null || correo.isBlank()) {
			lanzador.lanzarCorreoNoExiste("El correo electrónico no puede estar vacío.");
		}
		try {
			ResponseEntity<String> respuesta = restTemplate.getForEntity(
					URL_VALIDACION + correo.trim(), String.class);
			if (!respuesta.getStatusCode().is2xxSuccessful()) {
				// Si el servicio externo falla, no bloqueamos al usuario por eso.
				// Solo se valida formato (ya se hizo antes en el service).
				return;
			}
			JsonObject body = gson.fromJson(respuesta.getBody(), JsonObject.class);
			if (body == null || !body.has("data")) {
				return;
			}
			JsonObject data = body.getAsJsonObject("data");
			boolean formatoValido = data.has("valid_syntax")
					&& data.get("valid_syntax").getAsBoolean();
			boolean dominioValido = data.has("mx_records")
					&& data.get("mx_records").getAsBoolean();
			boolean correoDesechable = data.has("disposable")
					&& data.get("disposable").getAsBoolean();

			if (!formatoValido) {
				lanzador.lanzarCorreoNoExiste(
						"El correo '" + correo + "' tiene un formato inválido.");
			}
			if (!dominioValido) {
				lanzador.lanzarCorreoNoExiste(
						"El dominio del correo '" + correo + "' no existe o no acepta "
								+ "correos (no tiene registros MX válidos).");
			}
			if (correoDesechable) {
				lanzador.lanzarCorreoNoExiste(
						"No se permiten correos electrónicos desechables/temporales. "
								+ "Use una dirección de correo permanente.");
			}
		} catch (org.springframework.web.client.RestClientException e) {
			// Si la API externa de validación falla, no bloqueamos al usuario.
			// La verificación por código de email que viene a continuación
			// sirve como segunda barrera de validación.
			System.err.println("[EmailValidacion] No se pudo contactar la API "
					+ "de validación de correos: " + e.getMessage()
					+ ". Se procederá solo con la verificación por código.");
		}
	}
}
