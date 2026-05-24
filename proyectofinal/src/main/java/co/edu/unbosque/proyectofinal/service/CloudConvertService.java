package co.edu.unbosque.proyectofinal.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unbosque.proyectofinal.service.AuditoriaService;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
 
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.edu.unbosque.proyectofinal.dto.AuditoriaDTO;
import co.edu.unbosque.proyectofinal.dto.HistorialConversionDTO;
import co.edu.unbosque.proyectofinal.exception.ApiExternaException;
import co.edu.unbosque.proyectofinal.exception.ArchivoVacioException;
import co.edu.unbosque.proyectofinal.exception.ConversionFallidaException;
import co.edu.unbosque.proyectofinal.exception.FormatoArchivoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Servicio encargado de realizar las conversiones de archivos utilizando
 * la API REST gratuita de CloudConvert.
 * <p>
 * Este servicio realiza las llamadas HTTP directamente con
 * {@link RestTemplate} y parsea las respuestas JSON con {@link Gson}, sin
 * necesidad de la librería oficial {@code cloudconvert-java}.
 * </p>
 * <p>
 * El flujo completo es:
 * <ol>
 *   <li>Crear un Job en CloudConvert con tres tareas: import-upload, convert
 *       y export-url.</li>
 *   <li>Subir el archivo binario al endpoint que devuelve la tarea
 *       import-upload.</li>
 *   <li>Hacer polling al estado del job hasta que termine.</li>
 *   <li>Tomar la URL de descarga del archivo convertido y registrar la
 *       operación en el historial del usuario.</li>
 * </ol>
 * </p>
 *
 * <b>Formatos soportados (mínimo 6 por tipo, requisito del proyecto):</b>
 * <ul>
 *   <li>Audio: mp3, wav, aac, flac, ogg, m4a</li>
 *   <li>Video: mp4, mkv, avi, mov, webm, flv</li>
 *   <li>Imagen: jpg, png, webp, gif, bmp, tiff</li>
 * </ul>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@Service
public class CloudConvertService {

	/** Formatos de audio soportados. */
	public static final Set<String> FORMATOS_AUDIO = Set.of(
			"mp3", "wav", "aac", "flac", "ogg", "m4a");

	/** Formatos de video soportados. */
	public static final Set<String> FORMATOS_VIDEO = Set.of(
			"mp4", "mkv", "avi", "mov", "webm", "flv");

	/** Formatos de imagen soportados. */
	public static final Set<String> FORMATOS_IMAGEN = Set.of(
			"jpg", "jpeg", "png", "webp", "gif", "bmp", "tiff");

	/** URL base de la API de CloudConvert (sandbox por defecto). */
	private static final String URL_BASE_SANDBOX = "https://api.sandbox.cloudconvert.com/v2";

	/** URL base de la API de CloudConvert en producción. */
	private static final String URL_BASE_PRODUCCION = "https://api.cloudconvert.com/v2";

	/** Tiempo máximo de espera del job en milisegundos (10 minutos). */
	private static final long TIMEOUT_MILIS = 10L * 60L * 1000L;

	/** Intervalo entre consultas de polling al estado del job (3 segundos). */
	private static final long INTERVALO_POLLING_MILIS = 3000L;

	/**
	 * API key de CloudConvert. Se lee desde la propiedad opcional
	 * {@code cloudconvert.api.key} en application.properties; si no existe,
	 * se intenta leer la variable de entorno {@code CLOUDCONVERT_API_KEY}.
	 * <p>
	 * <b>IMPORTANTE:</b> las API keys de sandbox y producción son distintas.
	 * Si {@code cloudconvert.sandbox=true}, la key debe ser obtenida en
	 * <a href="https://sandbox.cloudconvert.com/dashboard/api/v2/keys">
	 * https://sandbox.cloudconvert.com/dashboard/api/v2/keys</a>
	 * (NO en cloudconvert.com normal).
	 * </p>
	 */
	@Value("${cloudconvert.api.key:}")
	private String apiKeyDesdeProperties;

	/**
	 * Indica si se debe usar el ambiente sandbox de CloudConvert
	 * (sin cobro de créditos, ideal para pruebas y entrega académica).
	 * Por defecto activado.
	 */
	@Value("${cloudconvert.sandbox:true}")
	private boolean sandbox;

	/**
	 * API key final que se usará para autenticarse contra CloudConvert.
	 * Se resuelve en {@link #inicializar()} dando prioridad a la propiedad
	 * sobre la variable de entorno.
	 */
	private String apiKey;

	/**
	 * Inicializa la API key resolviendo primero la propiedad de
	 * application.properties y, si está vacía, la variable de entorno
	 * {@code CLOUDCONVERT_API_KEY}. Imprime un log en consola con el
	 * resultado para facilitar diagnóstico.
	 */
	@jakarta.annotation.PostConstruct
	public void inicializar() {
		if (apiKeyDesdeProperties != null && !apiKeyDesdeProperties.isBlank()) {
			this.apiKey = apiKeyDesdeProperties.trim();
		} else {
			String fromEnv = System.getenv("CLOUDCONVERT_API_KEY");
			this.apiKey = fromEnv != null ? fromEnv.trim() : "";
		}
		String ambiente = sandbox ? "SANDBOX" : "PRODUCCIÓN";
		if (apiKey == null || apiKey.isEmpty()) {
			System.err.println("[CloudConvert] ⚠ API key NO configurada. "
					+ "Configura cloudconvert.api.key en application.properties "
					+ "o la variable de entorno CLOUDCONVERT_API_KEY.");
		} else {
			System.out.println("[CloudConvert] ✓ API key cargada (ambiente: "
					+ ambiente + ", longitud: " + apiKey.length() + " caracteres, "
					+ "termina en: ..." + apiKey.substring(Math.max(0, apiKey.length() - 6)) + ")");
			System.out.println("[CloudConvert] IMPORTANTE: si recibes 401 Unauthorized, "
					+ "verifica que la key sea de " + ambiente + " (sandbox.cloudconvert.com "
					+ "para sandbox, cloudconvert.com para producción).");
		}
	}

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private HistorialConversionService historialConversionService;

	@Autowired
	private LanzadorDeExcepcion lanzador;
	
	@Autowired
	private AuditoriaService auditoriaService;

	private final Gson gson = new Gson();

	/**
	 * Constructor sin argumentos requerido por Spring para la inyección
	 * de dependencias.
	 */
	public CloudConvertService() {
	}

	/**
	 * Convierte un archivo al formato indicado utilizando la API de
	 * CloudConvert y registra la operación en el historial del usuario.
	 * <p>
	 * El flujo interno es: validar entradas → crear job y subir archivo →
	 * hacer polling hasta que finalice → extraer URL de descarga →
	 * guardar en historial y auditoría.
	 * </p>
	 *
	 * @param archivo        archivo subido por el usuario
	 * @param formatoDestino formato al que se desea convertir (ej: mp4, mp3)
	 * @param tipoArchivo    categoría del archivo (AUDIO, VIDEO o IMAGEN)
	 * @param usuarioId      ID del usuario que solicita la conversión
	 * @return URL de descarga del archivo convertido
	 * @throws ArchivoVacioException          si el archivo está vacío o es nulo
	 * @throws FormatoArchivoInvalidoException si el formato no es soportado
	 * @throws ApiExternaException             si falla la comunicación con CloudConvert
	 * @throws ConversionFallidaException      si el job de conversión termina en error
	 */
	
	public String convertirArchivo(MultipartFile archivo, String formatoDestino,
			TipoArchivo tipoArchivo, Long usuarioId) {

		validarEntradaConversion(archivo, formatoDestino, tipoArchivo);

		String formatoDestinoNormalizado = formatoDestino.trim().toLowerCase();
		validarFormatoSegunTipo(formatoDestinoNormalizado, tipoArchivo);

		String nombreOriginal = archivo.getOriginalFilename() != null
				? archivo.getOriginalFilename()
				: "archivo";
		String formatoOrigen = obtenerExtension(nombreOriginal);

		try {
			String urlBase = sandbox ? URL_BASE_SANDBOX : URL_BASE_PRODUCCION;
			String jobId = crearJobYSubirArchivo(urlBase, archivo, formatoDestinoNormalizado);
			JsonObject jobFinal = esperarJob(urlBase, jobId);
			String urlDescarga = extraerUrlDescarga(jobFinal, formatoDestinoNormalizado);
			String nombreConvertido = extraerNombreConvertido(jobFinal, formatoDestinoNormalizado);

			registrarHistorialExitoso(tipoArchivo, formatoOrigen, formatoDestinoNormalizado,
					nombreOriginal, nombreConvertido, urlDescarga, usuarioId);

			return urlDescarga;

		} catch (ArchivoVacioException | FormatoArchivoInvalidoException
				| ApiExternaException | ConversionFallidaException e) {
			registrarHistorialFallido(tipoArchivo, formatoOrigen,
					formatoDestinoNormalizado, nombreOriginal, usuarioId);
			throw e;
		} catch (RestClientException e) {
			registrarHistorialFallido(tipoArchivo, formatoOrigen,
					formatoDestinoNormalizado, nombreOriginal, usuarioId);
			lanzador.lanzarApiExterna("Error de comunicación HTTP: " + e.getMessage());
			return null;
		} catch (Exception e) {
			registrarHistorialFallido(tipoArchivo, formatoOrigen,
					formatoDestinoNormalizado, nombreOriginal, usuarioId);
			lanzador.lanzarConversionFallida(e.getMessage());
			return null;
		}
	}

	/**
	 * Devuelve los formatos soportados según el tipo de archivo indicado.
	 *
	 * @param tipoArchivo categoría del archivo (AUDIO, VIDEO o IMAGEN)
	 * @return conjunto de extensiones soportadas para ese tipo
	 * @throws FormatoArchivoInvalidoException si el tipo de archivo es {@code null}
	 */
	public Set<String> getFormatosDisponibles(TipoArchivo tipoArchivo) {
		if (tipoArchivo == null) {
			lanzador.lanzarFormatoArchivoInvalido(
					"El tipo de archivo es obligatorio. Opciones: AUDIO, VIDEO, IMAGEN.");
		}
		return switch (tipoArchivo) {
			case AUDIO -> FORMATOS_AUDIO;
			case VIDEO -> FORMATOS_VIDEO;
			case IMAGEN -> FORMATOS_IMAGEN;
		};
	}

	/**
	 * Valida los parámetros de entrada antes de iniciar la conversión.
	 *
	 * @param archivo        archivo a convertir
	 * @param formatoDestino formato destino
	 * @param tipoArchivo    tipo de archivo
	 * @throws ArchivoVacioException          si el archivo está vacío o es nulo
	 * @throws FormatoArchivoInvalidoException si el formato o el tipo son nulos/vacíos
	 * @throws ApiExternaException             si la API key no está configurada
	 */
	private void validarEntradaConversion(MultipartFile archivo, String formatoDestino,
			TipoArchivo tipoArchivo) {
		if (archivo == null || archivo.isEmpty()) {
			lanzador.lanzarArchivoVacio("El archivo enviado está vacío o es nulo.");
		}
		if (formatoDestino == null || formatoDestino.isBlank()) {
			lanzador.lanzarFormatoArchivoInvalido(
					"El formato destino no puede estar vacío.");
		}
		if (tipoArchivo == null) {
			lanzador.lanzarFormatoArchivoInvalido(
					"El tipo de archivo es obligatorio. Opciones: AUDIO, VIDEO, IMAGEN.");
		}
		if (apiKey == null || apiKey.isBlank()) {
			lanzador.lanzarApiExterna(
					"No se ha configurado la API key de CloudConvert. "
							+ "Configure la variable de entorno CLOUDCONVERT_API_KEY "
							+ "o agregue la propiedad cloudconvert.api.key al sistema.");
		}
	}

	/**
	 * Crea el job en CloudConvert, sube el archivo a la tarea de importación
	 * y devuelve el ID del job creado.
	 *
	 * @param urlBase                   URL base de la API
	 * @param archivo                   archivo a subir
	 * @param formatoDestinoNormalizado formato destino en minúsculas
	 * @return ID del job creado
	 * @throws Exception si falla la creación del job o la subida del archivo
	 */
	private String crearJobYSubirArchivo(String urlBase, MultipartFile archivo,
			String formatoDestinoNormalizado) throws Exception {
		JsonObject jobBody = construirCuerpoJob(formatoDestinoNormalizado);
		JsonObject jobCreado = postJson(urlBase + "/jobs", jobBody);
		JsonObject dataJob = jobCreado.getAsJsonObject("data");
		String jobId = dataJob.get("id").getAsString();

		JsonObject tareaImport = encontrarTarea(dataJob, "import-archivo");
		if (tareaImport == null) {
			lanzador.lanzarApiExterna(
					"No se encontró la tarea de importación en el job creado.");
		}
		subirArchivoATareaImport(tareaImport, archivo);
		return jobId;
	}

	/**
	 * Extrae la URL de descarga del resultado del job finalizado.
	 *
	 * @param jobFinal                  respuesta final del job
	 * @param formatoDestinoNormalizado formato destino en minúsculas (para mensajes de error)
	 * @return URL de descarga del archivo convertido
	 * @throws ConversionFallidaException si no se encuentra la tarea de exportación
	 *                                    o si la API no retornó ningún archivo
	 */
	private String extraerUrlDescarga(JsonObject jobFinal, String formatoDestinoNormalizado) {
		JsonObject tareaExport = encontrarTarea(
				jobFinal.getAsJsonObject("data"), "export-archivo");
		if (tareaExport == null) {
			lanzador.lanzarConversionFallida(
					"No se encontró la tarea de exportación en el job finalizado.");
		}
		JsonObject result = tareaExport.getAsJsonObject("result");
		JsonArray archivos = result.getAsJsonArray("files");
		if (archivos == null || archivos.size() == 0) {
			lanzador.lanzarConversionFallida(
					"La API externa no retornó ningún archivo convertido.");
		}
		return archivos.get(0).getAsJsonObject().get("url").getAsString();
	}

	/**
	 * Extrae el nombre del archivo convertido del resultado del job.
	 * Si no se puede determinar, retorna un nombre genérico con el formato destino.
	 *
	 * @param jobFinal                  respuesta final del job
	 * @param formatoDestinoNormalizado formato destino en minúsculas
	 * @return nombre del archivo convertido
	 */
	private String extraerNombreConvertido(JsonObject jobFinal, String formatoDestinoNormalizado) {
		JsonObject tareaExport = encontrarTarea(
				jobFinal.getAsJsonObject("data"), "export-archivo");
		if (tareaExport == null) {
			return "archivo_convertido." + formatoDestinoNormalizado;
		}
		JsonObject result = tareaExport.getAsJsonObject("result");
		JsonArray archivos = result.getAsJsonArray("files");
		if (archivos == null || archivos.size() == 0) {
			return "archivo_convertido." + formatoDestinoNormalizado;
		}
		JsonObject archivoConvertido = archivos.get(0).getAsJsonObject();
		return archivoConvertido.has("filename")
				? archivoConvertido.get("filename").getAsString()
				: "archivo_convertido." + formatoDestinoNormalizado;
	}

	/**
	 * Registra una conversión exitosa en el historial del usuario y
	 * genera el registro de auditoría correspondiente.
	 *
	 * @param tipoArchivo      categoría del archivo convertido
	 * @param formatoOrigen    extensión del archivo original
	 * @param formatoDestino   extensión del archivo convertido
	 * @param nombreOriginal   nombre original del archivo subido
	 * @param nombreConvertido nombre del archivo resultante
	 * @param urlDescarga      URL de descarga del archivo convertido
	 * @param usuarioId        ID del usuario propietario
	 */
	private void registrarHistorialExitoso(TipoArchivo tipoArchivo, String formatoOrigen,
			String formatoDestino, String nombreOriginal, String nombreConvertido,
			String urlDescarga, Long usuarioId) {
		HistorialConversionDTO historial = new HistorialConversionDTO(
				LocalDateTime.now(),
				tipoArchivo,
				formatoOrigen,
				formatoDestino,
				nombreOriginal,
				nombreConvertido,
				null,
				urlDescarga,
				EstadoConversion.COMPLETADO,
				usuarioId);
		historialConversionService.create(historial);
		
		AuditoriaDTO auditoria = auditoriaService.preAccion(TipoAccion.CONVERSION,
                "Conversión exitosa: " + nombreOriginal
                        + " → " + formatoDestino
                        + " (" + tipoArchivo.name() + ")");
        auditoriaService.create(auditoria);
	}

	/**
	 * Construye el cuerpo JSON del job de CloudConvert con las tres tareas
	 * necesarias: import/upload, convert y export/url.
	 *
	 * @param formatoDestino formato al que se convertirá el archivo
	 * @return {@link JsonObject} con la estructura del job
	 */
	private JsonObject construirCuerpoJob(String formatoDestino) {
		Map<String, Object> tareas = new HashMap<>();

		Map<String, Object> importTask = new HashMap<>();
		importTask.put("operation", "import/upload");
		tareas.put("import-archivo", importTask);

		Map<String, Object> convertTask = new HashMap<>();
		convertTask.put("operation", "convert");
		convertTask.put("input", "import-archivo");
		convertTask.put("output_format", formatoDestino);
		tareas.put("convert-archivo", convertTask);

		Map<String, Object> exportTask = new HashMap<>();
		exportTask.put("operation", "export/url");
		exportTask.put("input", "convert-archivo");
		tareas.put("export-archivo", exportTask);

		Map<String, Object> body = new HashMap<>();
		body.put("tasks", tareas);
		return gson.toJsonTree(body).getAsJsonObject();
	}

	/**
	 * Realiza una petición POST a la API de CloudConvert con cuerpo JSON
	 * y cabecera de autorización Bearer.
	 *
	 * @param url  URL del endpoint
	 * @param body cuerpo JSON de la petición
	 * @return respuesta de la API como {@link JsonObject}
	 * @throws ApiExternaException si la respuesta no es 2xx
	 */
	private JsonObject postJson(String url, JsonObject body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);
		HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					url, HttpMethod.POST, entity, String.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				lanzador.lanzarApiExterna("Respuesta HTTP " + response.getStatusCode()
						+ " al crear el job. Body: " + response.getBody());
			}
			return gson.fromJson(response.getBody(), JsonObject.class);
		} catch (HttpStatusCodeException e) {
			String errorBody = e.getResponseBodyAsString();
			String mensaje = "CloudConvert respondió " + e.getStatusCode();
			if (e.getStatusCode().value() == 401) {
				mensaje += " (Unauthorized): la API key NO es válida para el ambiente "
						+ (sandbox ? "SANDBOX" : "PRODUCCIÓN")
						+ ". Verifica que la key sea de "
						+ (sandbox
								? "https://sandbox.cloudconvert.com/dashboard/api/v2/keys"
								: "https://cloudconvert.com/dashboard/api/v2/keys")
						+ ". Recuerda: las keys de sandbox y producción son DISTINTAS.";
			} else if (e.getStatusCode().value() == 402) {
				mensaje += " (Payment Required): se agotaron los créditos.";
			} else if (e.getStatusCode().value() == 422) {
				mensaje += " (Unprocessable): la configuración del job es inválida.";
			}
			if (errorBody != null && !errorBody.isEmpty()) {
				mensaje += " Detalle de CloudConvert: " + errorBody;
			}
			lanzador.lanzarApiExterna(mensaje);
			return null;
		}
	}

	/**
	 * Realiza una petición GET a la API de CloudConvert con cabecera de
	 * autorización Bearer para consultar el estado de un job.
	 *
	 * @param url URL del endpoint del job
	 * @return respuesta de la API como {@link JsonObject}
	 * @throws ApiExternaException si la respuesta no es 2xx
	 */
	private JsonObject getJson(String url) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(apiKey);
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(
					url, HttpMethod.GET, entity, String.class);
			if (!response.getStatusCode().is2xxSuccessful()) {
				lanzador.lanzarApiExterna("Respuesta HTTP " + response.getStatusCode()
						+ " al consultar el job. Body: " + response.getBody());
			}
			return gson.fromJson(response.getBody(), JsonObject.class);
		} catch (HttpStatusCodeException e) {
			String errorBody = e.getResponseBodyAsString();
			lanzador.lanzarApiExterna("CloudConvert respondió " + e.getStatusCode()
					+ " al consultar el job. Detalle: " + errorBody);
			return null;
		}
	}

	/**
	 * Busca una tarea dentro del objeto {@code data} del job por su nombre.
	 *
	 * @param data        objeto {@code data} del job de CloudConvert
	 * @param nombreTarea nombre de la tarea a buscar (ej. "import-archivo")
	 * @return {@link JsonObject} de la tarea encontrada, o {@code null} si no existe
	 */
	private JsonObject encontrarTarea(JsonObject data, String nombreTarea) {
		if (data == null || !data.has("tasks")) {
			return null;
		}
		JsonArray tasks = data.getAsJsonArray("tasks");
		for (JsonElement t : tasks) {
			JsonObject tarea = t.getAsJsonObject();
			if (tarea.has("name") && nombreTarea.equals(tarea.get("name").getAsString())) {
				return tarea;
			}
		}
		return null;
	}

	/**
	 * Sube el archivo binario al endpoint de importación de CloudConvert
	 * usando multipart/form-data, incluyendo los parámetros del formulario
	 * que devuelve la tarea de importación.
	 *
	 * @param tareaImport objeto JSON de la tarea import-archivo del job
	 * @param archivo     archivo a subir
	 * @throws ApiExternaException si la respuesta del servidor no es 2xx
	 * @throws Exception           si ocurre algún error de I/O al leer el archivo
	 */
	private void subirArchivoATareaImport(JsonObject tareaImport, MultipartFile archivo)
			throws Exception {

		JsonObject result = tareaImport.getAsJsonObject("result");
		JsonObject form = result.getAsJsonObject("form");
		String urlSubida = form.get("url").getAsString();
		JsonObject parametros = form.getAsJsonObject("parameters");

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		
		for (Map.Entry<String, JsonElement> entry : parametros.entrySet()) {
			body.add(entry.getKey(), entry.getValue().getAsString());
		}
		ByteArrayResource recurso = new ByteArrayResource(archivo.getBytes()) {
			@Override
			public String getFilename() {
				return archivo.getOriginalFilename();
			}
		};
		body.add("file", recurso);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				urlSubida, HttpMethod.POST, entity, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			lanzador.lanzarApiExterna("Respuesta HTTP " + response.getStatusCode()
					+ " al subir el archivo a la API externa.");
		}
	}

	/**
	 * Hace polling al estado del job de CloudConvert hasta que finaliza
	 * ({@code finished}), falla ({@code error}) o se supera el tiempo máximo.
	 *
	 * @param urlBase URL base de la API
	 * @param jobId   ID del job a consultar
	 * @return {@link JsonObject} con el estado final del job
	 * @throws InterruptedException   si el hilo es interrumpido durante el polling
	 * @throws ConversionFallidaException si el job termina en error o se supera el timeout
	 */
	private JsonObject esperarJob(String urlBase, String jobId) throws InterruptedException {
		long inicio = System.currentTimeMillis();
		while (true) {
			JsonObject estadoJob = getJson(urlBase + "/jobs/" + jobId);
			JsonObject data = estadoJob.getAsJsonObject("data");
			String estado = data.get("status").getAsString();
			if ("finished".equalsIgnoreCase(estado)) {
				return estadoJob;
			}
			if ("error".equalsIgnoreCase(estado)) {
				String mensaje = "El job de conversión terminó en estado de error.";
				JsonArray tareas = data.getAsJsonArray("tasks");
				for (JsonElement t : tareas) {
					JsonObject tarea = t.getAsJsonObject();
					if ("error".equalsIgnoreCase(tarea.get("status").getAsString())
							&& tarea.has("message") && !tarea.get("message").isJsonNull()) {
						mensaje = "Error reportado por la API externa: "
								+ tarea.get("message").getAsString();
						break;
					}
				}
				lanzador.lanzarConversionFallida(mensaje);
			}
			if (System.currentTimeMillis() - inicio > TIMEOUT_MILIS) {
				lanzador.lanzarConversionFallida(
						"Se superó el tiempo máximo de espera para la conversión.");
			}
			Thread.sleep(INTERVALO_POLLING_MILIS);
		}
	}

	/**
	 * Verifica que el formato destino sea válido para el tipo de archivo indicado.
	 *
	 * @param formato     extensión del formato destino en minúsculas
	 * @param tipoArchivo categoría del archivo (AUDIO, VIDEO o IMAGEN)
	 * @throws FormatoArchivoInvalidoException si el formato no está en la lista permitida
	 */
	private void validarFormatoSegunTipo(String formato, TipoArchivo tipoArchivo) {
		boolean valido = switch (tipoArchivo) {
			case AUDIO -> FORMATOS_AUDIO.contains(formato);
			case VIDEO -> FORMATOS_VIDEO.contains(formato);
			case IMAGEN -> FORMATOS_IMAGEN.contains(formato);
		};
		if (!valido) {
			Set<String> opciones = switch (tipoArchivo) {
				case AUDIO -> FORMATOS_AUDIO;
				case VIDEO -> FORMATOS_VIDEO;
				case IMAGEN -> FORMATOS_IMAGEN;
			};
			lanzador.lanzarFormatoArchivoInvalido("El formato '" + formato
					+ "' no es válido para el tipo " + tipoArchivo
					+ ". Formatos permitidos: " + String.join(", ", opciones));
		}
	}

	/**
	 * Extrae la extensión del nombre de archivo proporcionado en minúsculas.
	 * Si el nombre no contiene punto, retorna una cadena vacía.
	 *
	 * @param nombreArchivo nombre del archivo (ej. "video.mp4")
	 * @return extensión en minúsculas (ej. "mp4"), o cadena vacía si no hay extensión
	 */
	private static String obtenerExtension(String nombreArchivo) {
		if (nombreArchivo == null || !nombreArchivo.contains(".")) {
			return "";
		}
		return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
	}

	/**
	 * Registra una conversión fallida en el historial del usuario y
	 * genera el registro de auditoría correspondiente.
	 * Si este registro falla internamente, la excepción se ignora para
	 * no ocultar la excepción original de la conversión.
	 *
	 * @param tipoArchivo    categoría del archivo
	 * @param formatoOrigen  extensión del archivo original
	 * @param formatoDestino extensión del formato destino
	 * @param nombreOriginal nombre del archivo original
	 * @param usuarioId      ID del usuario propietario
	 */
	private void registrarHistorialFallido(TipoArchivo tipoArchivo, String formatoOrigen,
			String formatoDestino, String nombreOriginal, Long usuarioId) {
		try {
			HistorialConversionDTO historialFallido = new HistorialConversionDTO(
					LocalDateTime.now(),
					tipoArchivo,
					formatoOrigen != null ? formatoOrigen : "",
					formatoDestino != null ? formatoDestino : "",
					nombreOriginal,
					null, null, null,
					EstadoConversion.FALLIDO,
					usuarioId);
			historialConversionService.create(historialFallido);
			
			AuditoriaDTO auditoriaFallida = auditoriaService.preAccion(TipoAccion.CONVERSION,
                    "Conversión FALLIDA: " + nombreOriginal
                            + " → " + formatoDestino
                            + " (" + tipoArchivo.name() + ")");
            auditoriaService.create(auditoriaFallida);
			
		} catch (Exception ignorado) {
			
		}
	}
}