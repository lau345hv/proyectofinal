package co.edu.unbosque.proyectofinal.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;

/**
 * Servicio que gestiona el flujo de verificación de correo electrónico
 * por código numérico.
 * <p>
 * Cuando un usuario quiere registrarse:
 * <ol>
 *   <li>Llama a {@link #solicitarCodigo(String)} con el correo destino.</li>
 *   <li>El servicio genera un código aleatorio de 6 dígitos, lo guarda
 *       en memoria asociado al correo y lo envía por email.</li>
 *   <li>El usuario copia el código del correo recibido y lo pega en
 *       el endpoint de creación junto al resto de los datos.</li>
 *   <li>{@link #validarCodigo(String, String)} verifica que el código
 *       coincide y no ha expirado.</li>
 * </ol>
 * <p>
 * <b>Envío del código:</b>
 * <ul>
 *   <li>SIEMPRE se imprime el código en la consola del servidor (útil
 *       para desarrollo y pruebas sin configurar correo).</li>
 *   <li>Si la variable de entorno {@code BREVO_API_KEY} está configurada,
 *       el código también se envía por correo electrónico real usando
 *       la API HTTP gratuita de Brevo (300 emails/día gratis,
 *       https://www.brevo.com).</li>
 * </ul>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Service
public class VerificacionCorreoService {

	/** Tiempo de vida del código de verificación en minutos. */
	private static final long DURACION_CODIGO_MINUTOS = 10L;

	/** URL del endpoint de envío de correos de Brevo. */
	private static final String URL_BREVO = "https://api.brevo.com/v3/smtp/email";

	/** Mapa de códigos pendientes (correo → info). */
	private final Map<String, CodigoPendiente> codigosPendientes = new HashMap<>();

	private final SecureRandom random = new SecureRandom();

	@Value("${brevo.api.key:}")
	private String brevoApiKeyDesdeProperties;

	@Value("${brevo.remitente.correo:noreply@conversionarchivos.local}")
	private String remitenteCorreo;

	@Value("${brevo.remitente.nombre:Plataforma de Conversión}")
	private String remitenteNombre;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private LanzadorDeExcepcion lanzador;

	

	/**
	 * Genera un código aleatorio de 6 dígitos, lo guarda asociado al
	 * correo y lo envía al destinatario.
	 *
	 * @param correo dirección de correo electrónico destino
	 * @return el código generado (también se imprime en consola)
	 */
	public String solicitarCodigo(String correo) {
		if (correo == null || correo.isBlank()) {
			lanzador.lanzarDatoInvalido("El correo no puede estar vacío.");
		}
		limpiarCodigosExpirados();
		String codigo = generarCodigo();
		codigosPendientes.put(correo.trim().toLowerCase(),
				new CodigoPendiente(codigo, LocalDateTime.now()));

		
		System.out.println("");
		System.out.println("========================================================");
		System.out.println("[VerificacionCorreo] Código de verificación generado");
		System.out.println("[VerificacionCorreo]   Para: " + correo);
		System.out.println("[VerificacionCorreo]   Código: " + codigo);
		System.out.println("[VerificacionCorreo]   Expira en: "
				+ DURACION_CODIGO_MINUTOS + " minutos");
		System.out.println("========================================================");
		System.out.println("");

		
		String apiKey = resolverApiKey();
		if (apiKey != null && !apiKey.isBlank()) {
			enviarCorreoBrevo(correo.trim(), codigo, apiKey);
		} else {
			System.out.println("[VerificacionCorreo] ℹ BREVO_API_KEY no configurada. "
					+ "El código solo aparece en consola (modo desarrollo). "
					+ "Para enviar correos reales, regístrate gratis en "
					+ "https://www.brevo.com y configura la variable de entorno "
					+ "BREVO_API_KEY.");
		}
		return codigo;
	}

	/**
	 * Valida que el código corresponda al correo y no haya expirado.
	 * Si la validación es exitosa, consume el código (lo borra).
	 *
	 * @param correo correo al que se envió el código
	 * @param codigo código que el usuario recibió
	 */
	public void validarCodigo(String correo, String codigo) {
		if (correo == null || correo.isBlank()) {
			lanzador.lanzarCodigoVerificacionInvalido(
					"Debe indicar el correo electrónico.");
		}
		if (codigo == null || codigo.isBlank()) {
			lanzador.lanzarCodigoVerificacionInvalido(
					"Debe indicar el código de verificación que recibió por correo.");
		}
		limpiarCodigosExpirados();
		String clave = correo.trim().toLowerCase();
		CodigoPendiente info = codigosPendientes.get(clave);
		if (info == null) {
			lanzador.lanzarCodigoVerificacionInvalido(
					"No hay ningún código pendiente para el correo " + correo
							+ ". Solicite uno nuevo desde /verificacion/solicitar-codigo.");
		}
		if (LocalDateTime.now().isAfter(
				info.fechaCreacion.plusMinutes(DURACION_CODIGO_MINUTOS))) {
			codigosPendientes.remove(clave);
			lanzador.lanzarCodigoVerificacionInvalido(
					"El código de verificación ha expirado. Solicite uno nuevo.");
		}
		if (!info.codigo.equals(codigo.trim())) {
			lanzador.lanzarCodigoVerificacionInvalido(
					"El código de verificación no es correcto. "
							+ "Verifique el correo electrónico recibido.");
		}
		
		codigosPendientes.remove(clave);
	}

	/**
	 * Verifica si hay un código pendiente válido para el correo dado.
	 * Útil para no permitir registros sin previa verificación.
	 *
	 * @param correo correo a chequear
	 * @return true si existe un código pendiente y no expirado
	 */
	public boolean hayCodigoPendiente(String correo) {
		if (correo == null) {
			return false;
		}
		limpiarCodigosExpirados();
		return codigosPendientes.containsKey(correo.trim().toLowerCase());
	}

	private String generarCodigo() {
		int numero = 100000 + random.nextInt(900000);
		return String.valueOf(numero);
	}

	private void limpiarCodigosExpirados() {
		LocalDateTime ahora = LocalDateTime.now();
		Iterator<Map.Entry<String, CodigoPendiente>> it =
				codigosPendientes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CodigoPendiente> e = it.next();
			if (ahora.isAfter(e.getValue().fechaCreacion
					.plusMinutes(DURACION_CODIGO_MINUTOS))) {
				it.remove();
			}
		}
	}

	private String resolverApiKey() {
		if (brevoApiKeyDesdeProperties != null && !brevoApiKeyDesdeProperties.isBlank()) {
			return brevoApiKeyDesdeProperties.trim();
		}
		String env = System.getenv("BREVO_API_KEY");
		return env != null ? env.trim() : "";
	}

	/**
	 * Envía el correo de verificación usando la API HTTP de Brevo.
	 * Si falla, lo registra en consola pero NO bloquea al usuario (el
	 * código siempre está visible en la consola del servidor para
	 * desarrollo).
	 */
	private void enviarCorreoBrevo(String destinatario, String codigo, String apiKey) {
		try {
			JsonObject body = new JsonObject();

			JsonObject sender = new JsonObject();
			sender.addProperty("name", remitenteNombre);
			sender.addProperty("email", remitenteCorreo);
			body.add("sender", sender);

			JsonArray to = new JsonArray();
			JsonObject destino = new JsonObject();
			destino.addProperty("email", destinatario);
			to.add(destino);
			body.add("to", to);

			body.addProperty("subject",
					"Código de verificación - Plataforma de Conversión");
			body.addProperty("htmlContent",
					"<div style='font-family: Arial, sans-serif; max-width: 500px; "
							+ "margin: 0 auto; padding: 20px;'>"
							+ "<h2 style='color: #4F46E5;'>Verificación de cuenta</h2>"
							+ "<p>Hola,</p>"
							+ "<p>Para completar la creación de tu cuenta en la "
							+ "Plataforma de Conversión, usa el siguiente código de "
							+ "verificación:</p>"
							+ "<div style='font-size: 32px; font-weight: bold; "
							+ "letter-spacing: 6px; text-align: center; padding: 20px; "
							+ "background: #F3F4F6; border-radius: 8px; "
							+ "color: #4F46E5;'>" + codigo + "</div>"
							+ "<p>Este código es válido por <b>"
							+ DURACION_CODIGO_MINUTOS + " minutos</b>.</p>"
							+ "<p>Si no solicitaste esta verificación, ignora este "
							+ "correo.</p>"
							+ "<hr style='margin-top: 30px; border: none; "
							+ "border-top: 1px solid #E5E7EB;'>"
							+ "<p style='color: #6B7280; font-size: 12px;'>Plataforma "
							+ "de Conversión - Proyecto Universitario</p>"
							+ "</div>");

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("api-key", apiKey);
			headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
			HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
			ResponseEntity<String> resp = restTemplate.exchange(
					URL_BREVO, HttpMethod.POST, entity, String.class);
			if (resp.getStatusCode().is2xxSuccessful()) {
				System.out.println("[VerificacionCorreo] ✓ Correo enviado vía Brevo a "
						+ destinatario);
			} else {
				System.err.println("[VerificacionCorreo] ⚠ Brevo respondió "
						+ resp.getStatusCode() + ". Body: " + resp.getBody());
			}
		} catch (HttpStatusCodeException e) {
			System.err.println("[VerificacionCorreo] ⚠ Error al enviar correo vía Brevo: "
					+ e.getStatusCode() + " - " + e.getResponseBodyAsString());
			System.err.println("[VerificacionCorreo] El código sigue disponible en la "
					+ "consola para que pruebes manualmente.");
		} catch (Exception e) {
			System.err.println("[VerificacionCorreo] ⚠ Excepción al enviar correo: "
					+ e.getMessage());
		}
	}

	private static class CodigoPendiente {
		final String codigo;
		final LocalDateTime fechaCreacion;

		CodigoPendiente(String codigo, LocalDateTime fechaCreacion) {
			this.codigo = codigo;
			this.fechaCreacion = fechaCreacion;
		}
	}
}