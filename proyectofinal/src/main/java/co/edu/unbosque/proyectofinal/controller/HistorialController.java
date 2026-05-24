package co.edu.unbosque.proyectofinal.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectofinal.dto.HistorialConversionDTO;
import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.service.HistorialConversionService;
import co.edu.unbosque.proyectofinal.service.UsuarioService;
import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador REST que expone los endpoints para consultar y administrar
 * el historial de conversiones de archivos en el sistema.
 *
 * <p>Los endpoints están divididos según el rol del usuario autenticado:</p>
 * <ul>
 *   <li><strong>USUARIO:</strong> puede consultar únicamente sus propias
 *       conversiones a través de {@code GET /historial/misConversiones} y
 *       descargar el archivo original de una conversión propia mediante
 *       {@code GET /historial/descargarOriginal}.</li>
 *   <li><strong>ADMIN:</strong> tiene acceso a todos los endpoints de listado,
 *       filtrado, resumen agregado, conteo y eliminación de registros.</li>
 * </ul>
 *
 * <p>Todos los endpoints requieren autenticación mediante token JWT
 * (esquema {@code Bearer}).</p>
 *
 * <h3>Nota de seguridad (corrección JAVA-S1061):</h3>
 * <p>Los métodos que necesitan identificar al usuario autenticado reciben
 * {@link org.springframework.security.core.userdetails.UserDetails} como
 * principal, en lugar de la entidad JPA {@code Usuario}. Esto evita exponer
 * el objeto de persistencia en la capa web. El nombre de usuario se extrae
 * de {@link UserDetails#getUsername()} y se delega a
 * {@link UsuarioService} la resolución del ID real.</p>
 *
 * @author Equipo de desarrollo
 * @version 3.1
 * @see HistorialConversionService
 * @see UsuarioService
 */
@RestController
@RequestMapping("/historial")
@SecurityRequirement(name = "bearerAuth")
public class HistorialController {

	private final HistorialConversionService service;
	private final UsuarioService usuarioService;
	private final LanzadorDeExcepcion lanzador;

	/**
	 * Construye una nueva instancia del controlador con las dependencias requeridas.
	 *
	 * @param service        servicio que gestiona la lógica de negocio del historial
	 *                       de conversiones.
	 * @param usuarioService servicio que gestiona la lógica de negocio de usuarios;
	 *                       utilizado para resolver el ID a partir del nombre de usuario.
	 * @param lanzador       componente utilitario para lanzar excepciones de negocio
	 *                       estandarizadas (p. ej., opción de enumeración no válida).
	 */
	public HistorialController(HistorialConversionService service, UsuarioService usuarioService,
			LanzadorDeExcepcion lanzador) {
		this.service = service;
		this.usuarioService = usuarioService;
		this.lanzador = lanzador;
	}

	/**
	 * Devuelve el historial de conversiones perteneciente al usuario que envió
	 * el token JWT.
	 *
	 * <p><strong>Corrección JAVA-S1061:</strong> se recibe {@link UserDetails}
	 * en lugar de la entidad {@code Usuario} para no exponer el objeto de
	 * persistencia en la capa web. El nombre de usuario se obtiene mediante
	 * {@link UserDetails#getUsername()} y se resuelve el ID a través de
	 * {@link UsuarioService#findByNombreUsuario(String)}.</p>
	 *
	 * @param userDetails principal inyectado automáticamente por Spring Security
	 *                    a partir del JWT. Contiene el nombre de usuario pero
	 *                    <em>no</em> la entidad JPA completa.
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} del
	 *         usuario autenticado, o {@code 204 No Content} si no tiene
	 *         conversiones registradas.
	 */
	@GetMapping("/misConversiones")
	@Operation(summary = "Ver el historial del usuario autenticado",
			description = "Devuelve únicamente las conversiones del propio usuario.")
	public ResponseEntity<List<HistorialConversionDTO>> misConversiones(
			@AuthenticationPrincipal UserDetails userDetails) {

		String nombreUsuario = userDetails.getUsername();
		UsuarioDTO usuarioDTO = usuarioService.findByNombreUsuario(nombreUsuario);

		List<HistorialConversionDTO> lista = service.findByUsuarioId(usuarioDTO.getId());
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve la URL de descarga del archivo <em>original</em> asociado a una
	 * conversión específica del usuario autenticado.
	 *
	 * <p>El usuario solo puede consultar sus propias conversiones. Si el registro
	 * pertenece a otro usuario se responde con {@code 403 Forbidden}. Si la URL
	 * del archivo original no está disponible (p. ej., la conversión falló o la
	 * tarea de exportación no finalizó), se responde con {@code 404 Not Found}.</p>
	 *
	 * @param id          identificador del registro de historial a consultar.
	 * @param userDetails principal del usuario autenticado, inyectado
	 *                    automáticamente por Spring Security.
	 * @return {@code 200 OK} con la URL del archivo original como texto plano;
	 *         {@code 403 Forbidden} si la conversión no pertenece al usuario
	 *         autenticado; {@code 404 Not Found} si la URL no está disponible.
	 */
	@GetMapping("/descargarOriginal")
	@Operation(summary = "Obtener URL de descarga del archivo original (usuario autenticado)",
			description = "Devuelve la URL del archivo original guardado en el historial. "
					+ "El usuario solo puede acceder a sus propias conversiones.")
	public ResponseEntity<String> descargarOriginal(
			@RequestParam Long id,
			@AuthenticationPrincipal UserDetails userDetails) {

		String nombreUsuario = userDetails.getUsername();
		UsuarioDTO usuarioDTO = usuarioService.findByNombreUsuario(nombreUsuario);

		HistorialConversionDTO conversion = service.findById(id);

		if (!conversion.getUsuarioId().equals(usuarioDTO.getId())) {
			return new ResponseEntity<>(
					"No tienes permiso para acceder a esta conversión.",
					HttpStatus.FORBIDDEN);
		}

		String urlOriginal = conversion.getRutaArchivoOriginal();
		if (urlOriginal == null || urlOriginal.isBlank()) {
			return new ResponseEntity<>(
					"El archivo original no está disponible para esta conversión.",
					HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(urlOriginal, HttpStatus.OK);
	}

	/**
	 * Devuelve un resumen agregado del estado general del sistema.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * <p>El mapa de respuesta contiene las siguientes claves:</p>
	 * <ul>
	 *   <li>{@code totalUsuarios} – cantidad total de usuarios registrados.</li>
	 *   <li>{@code totalConversiones} – cantidad total de conversiones en el sistema.</li>
	 *   <li>{@code conversionesPorTipo} – mapa con el conteo de conversiones agrupadas
	 *       por cada valor de {@link TipoArchivo}.</li>
	 *   <li>{@code conversionesPorEstado} – mapa con el conteo de conversiones agrupadas
	 *       por cada valor de {@link EstadoConversion}.</li>
	 *   <li>{@code usuariosPorRol} – mapa con el conteo de usuarios agrupados por cada
	 *       valor de {@link RolUsuario}.</li>
	 * </ul>
	 *
	 * @return {@code 200 OK} con el mapa de totales y desgloses agregados.
	 */
	@GetMapping("/resumen")
	@Operation(summary = "Resumen general del sistema (SOLO ADMIN)",
			description = "Devuelve totales agregados de usuarios y conversiones, "
					+ "desglosados por tipo de archivo, estado y rol de usuario.")
	public ResponseEntity<Map<String, Object>> resumen() {
		Map<String, Object> resumen = new HashMap<>();
		resumen.put("totalUsuarios", usuarioService.count());
		resumen.put("totalConversiones", service.count());

		Map<String, Long> porTipo = new HashMap<>();
		for (TipoArchivo tipo : TipoArchivo.values()) {
			porTipo.put(tipo.name(), (long) service.findByTipoArchivo(tipo).size());
		}
		resumen.put("conversionesPorTipo", porTipo);

		Map<String, Long> porEstado = new HashMap<>();
		for (EstadoConversion estado : EstadoConversion.values()) {
			porEstado.put(estado.name(), (long) service.findByEstado(estado).size());
		}
		resumen.put("conversionesPorEstado", porEstado);

		Map<String, Long> usuariosPorRol = new HashMap<>();
		for (RolUsuario rol : RolUsuario.values()) {
			usuariosPorRol.put(rol.name(), (long) usuarioService.findByRol(rol).size());
		}
		resumen.put("usuariosPorRol", usuariosPorRol);

		return new ResponseEntity<>(resumen, HttpStatus.OK);
	}

	/**
	 * Devuelve la lista completa de todos los registros del historial de conversiones.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @return {@code 200 OK} con la lista de todos los {@link HistorialConversionDTO},
	 *         o {@code 204 No Content} si no existe ningún registro.
	 */
	@GetMapping("/listar")
	@Operation(summary = "Listar todo el historial (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> listar() {
		List<HistorialConversionDTO> lista = service.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Busca y devuelve un registro del historial a partir de su identificador único.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param id identificador del registro de historial a buscar.
	 * @return {@code 200 OK} con el {@link HistorialConversionDTO} correspondiente.
	 * @throws co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException
	 *         si no existe ningún registro con el ID proporcionado.
	 */
	@GetMapping("/buscarPorId")
	@Operation(summary = "Buscar un registro de historial por id (SOLO ADMIN)")
	public ResponseEntity<HistorialConversionDTO> buscarPorId(@RequestParam Long id) {
		HistorialConversionDTO encontrado = service.findById(id);
		return new ResponseEntity<>(encontrado, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial asociados a un usuario específico.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param usuarioId identificador del usuario cuyo historial se desea consultar.
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} del usuario,
	 *         o {@code 204 No Content} si el usuario no tiene conversiones registradas.
	 */
	@GetMapping("/porUsuario")
	@Operation(summary = "Listar historial de un usuario por id (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porUsuario(@RequestParam Long usuarioId) {
		List<HistorialConversionDTO> lista = service.findByUsuarioId(usuarioId);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial filtrados por tipo de archivo.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param tipoArchivo tipo de archivo a filtrar. Valores aceptados (insensible a
	 *                    mayúsculas): {@code AUDIO}, {@code VIDEO}, {@code IMAGEN}.
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} del tipo
	 *         indicado, o {@code 204 No Content} si no hay registros para ese tipo.
	 * @throws co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException
	 *         si el valor de {@code tipoArchivo} no corresponde a ningún valor
	 *         del enum {@link TipoArchivo}.
	 */
	@GetMapping("/porTipo")
	@Operation(summary = "Filtrar historial por tipo de archivo (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porTipo(
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = { "AUDIO", "VIDEO", "IMAGEN" }))
				String tipoArchivo) {
		TipoArchivo tipo;
		try {
			tipo = TipoArchivo.valueOf(tipoArchivo.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("tipoArchivo", tipoArchivo, "AUDIO, VIDEO, IMAGEN");
			return null;
		}
		List<HistorialConversionDTO> lista = service.findByTipoArchivo(tipo);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial filtrados por estado de conversión.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param estado estado de conversión a filtrar. Valores aceptados (insensible a
	 *               mayúsculas): {@code PENDIENTE}, {@code EN_PROCESO},
	 *               {@code COMPLETADO}, {@code FALLIDO}.
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} del estado
	 *         indicado, o {@code 204 No Content} si no hay registros para ese estado.
	 * @throws co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException
	 *         si el valor de {@code estado} no corresponde a ningún valor del
	 *         enum {@link EstadoConversion}.
	 */
	@GetMapping("/porEstado")
	@Operation(summary = "Filtrar historial por estado (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porEstado(
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = {
						"PENDIENTE", "EN_PROCESO", "COMPLETADO", "FALLIDO" }))
				String estado) {
		EstadoConversion est;
		try {
			est = EstadoConversion.valueOf(estado.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("estado", estado,
					"PENDIENTE, EN_PROCESO, COMPLETADO, FALLIDO");
			return null;
		}
		List<HistorialConversionDTO> lista = service.findByEstado(est);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial filtrados por formato de origen.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param formatoOrigen cadena que representa el formato de origen a filtrar
	 *                      (p. ej., {@code "mp3"}, {@code "mp4"}, {@code "png"}).
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} que
	 *         coinciden con el formato de origen, o {@code 204 No Content} si no
	 *         hay registros para ese formato.
	 */
	@GetMapping("/porFormatoOrigen")
	@Operation(summary = "Filtrar historial por formato origen (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porFormatoOrigen(
			@RequestParam String formatoOrigen) {
		List<HistorialConversionDTO> lista = service.findByFormatoOrigen(formatoOrigen);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial filtrados por formato de destino.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param formatoDestino cadena que representa el formato de destino a filtrar
	 *                       (p. ej., {@code "wav"}, {@code "avi"}, {@code "jpg"}).
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} que
	 *         coinciden con el formato de destino, o {@code 204 No Content} si no
	 *         hay registros para ese formato.
	 */
	@GetMapping("/porFormatoDestino")
	@Operation(summary = "Filtrar historial por formato destino (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porFormatoDestino(
			@RequestParam String formatoDestino) {
		List<HistorialConversionDTO> lista = service.findByFormatoDestino(formatoDestino);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial de un usuario filtrados además
	 * por tipo de archivo.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param usuarioId   identificador del usuario a filtrar.
	 * @param tipoArchivo tipo de archivo a filtrar. Valores aceptados (insensible a
	 *                    mayúsculas): {@code AUDIO}, {@code VIDEO}, {@code IMAGEN}.
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} que
	 *         cumplen ambos criterios, o {@code 204 No Content} si no hay resultados.
	 * @throws co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException
	 *         si el valor de {@code tipoArchivo} no corresponde a ningún valor
	 *         del enum {@link TipoArchivo}.
	 */
	@GetMapping("/porUsuarioYTipo")
	@Operation(summary = "Filtrar historial por usuario y tipo (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porUsuarioYTipo(
			@RequestParam Long usuarioId,
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = { "AUDIO", "VIDEO", "IMAGEN" }))
				String tipoArchivo) {
		TipoArchivo tipo;
		try {
			tipo = TipoArchivo.valueOf(tipoArchivo.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("tipoArchivo", tipoArchivo, "AUDIO, VIDEO, IMAGEN");
			return null;
		}
		List<HistorialConversionDTO> lista =
				service.findByUsuarioIdAndTipoArchivo(usuarioId, tipo);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial de un usuario filtrados además
	 * por estado de conversión.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param usuarioId identificador del usuario a filtrar.
	 * @param estado    estado de conversión a filtrar. Valores aceptados (insensible
	 *                  a mayúsculas): {@code PENDIENTE}, {@code EN_PROCESO},
	 *                  {@code COMPLETADO}, {@code FALLIDO}.
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} que
	 *         cumplen ambos criterios, o {@code 204 No Content} si no hay resultados.
	 * @throws co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException
	 *         si el valor de {@code estado} no corresponde a ningún valor del
	 *         enum {@link EstadoConversion}.
	 */
	@GetMapping("/porUsuarioYEstado")
	@Operation(summary = "Filtrar historial por usuario y estado (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porUsuarioYEstado(
			@RequestParam Long usuarioId,
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = {
						"PENDIENTE", "EN_PROCESO", "COMPLETADO", "FALLIDO" }))
				String estado) {
		EstadoConversion est;
		try {
			est = EstadoConversion.valueOf(estado.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("estado", estado,
					"PENDIENTE, EN_PROCESO, COMPLETADO, FALLIDO");
			return null;
		}
		List<HistorialConversionDTO> lista =
				service.findByUsuarioIdAndEstado(usuarioId, est);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Devuelve todos los registros del historial cuya fecha de conversión se
	 * encuentra dentro del rango {@code [inicio, fin]} (ambos inclusive).
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * <p>Las fechas deben seguir el formato ISO 8601:
	 * {@code yyyy-MM-dd'T'HH:mm:ss} (p. ej., {@code 2025-01-01T00:00:00}).</p>
	 *
	 * @param inicio fecha y hora de inicio del rango (inclusive).
	 * @param fin    fecha y hora de fin del rango (inclusive).
	 * @return {@code 200 OK} con la lista de {@link HistorialConversionDTO} dentro
	 *         del rango indicado, o {@code 204 No Content} si no hay resultados.
	 */
	@GetMapping("/porFechas")
	@Operation(summary = "Filtrar historial por rango de fechas (SOLO ADMIN)",
			description = "El formato es ISO 8601: yyyy-MM-ddTHH:mm:ss")
	public ResponseEntity<List<HistorialConversionDTO>> porFechas(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
				@Parameter(description = "Fecha y hora de inicio",
						schema = @Schema(type = "string", format = "date-time",
								example = "2025-01-01T00:00:00"))
				LocalDateTime inicio,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
				@Parameter(description = "Fecha y hora de fin",
						schema = @Schema(type = "string", format = "date-time",
								example = "2025-12-31T23:59:59"))
				LocalDateTime fin) {
		List<HistorialConversionDTO> lista = service.findByFechaConversionBetween(inicio, fin);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	/**
	 * Elimina de forma permanente un registro del historial identificado por su ID.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @param id identificador del registro de historial a eliminar.
	 * @return {@code 200 OK} con un mensaje de confirmación si la eliminación
	 *         fue exitosa.
	 * @throws co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException
	 *         si no existe ningún registro con el ID proporcionado.
	 */
	@DeleteMapping("/eliminar")
	@Operation(summary = "Eliminar un registro del historial por id (SOLO ADMIN)")
	public ResponseEntity<String> eliminar(@RequestParam Long id) {
		service.deleteById(id);
		return new ResponseEntity<>("Registro del historial eliminado correctamente.",
				HttpStatus.OK);
	}

	/**
	 * Devuelve el número total de registros existentes en el historial de conversiones.
	 *
	 * <p><strong>Acceso restringido a ADMIN.</strong></p>
	 *
	 * @return {@code 200 OK} con el conteo total de registros como valor {@code Long}.
	 */
	@GetMapping("/contar")
	@Operation(summary = "Contar registros del historial (SOLO ADMIN)")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(service.count(), HttpStatus.OK);
	}
}