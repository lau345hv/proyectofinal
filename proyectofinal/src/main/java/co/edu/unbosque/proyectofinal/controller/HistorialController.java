package co.edu.unbosque.proyectofinal.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectofinal.dto.HistorialConversionDTO;
import co.edu.unbosque.proyectofinal.entity.Usuario;
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
 * Controlador REST para consultar el historial de conversiones.
 * <p>
 * Los usuarios con rol USUARIO solo pueden ver sus propias conversiones
 * a través de {@code /historial/misConversiones}. Los filtros generales,
 * el resumen agregado y la administración del historial están reservados
 * al rol ADMIN.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
@RestController
@RequestMapping("/historial")
@SecurityRequirement(name = "bearerAuth")
public class HistorialController {

	private final HistorialConversionService service;
	private final UsuarioService usuarioService;
	private final LanzadorDeExcepcion lanzador;

	public HistorialController(HistorialConversionService service, UsuarioService usuarioService,
			LanzadorDeExcepcion lanzador) {
		this.service = service;
		this.usuarioService = usuarioService;
		this.lanzador = lanzador;
	}

	// =====================================================================
	// ENDPOINT PARA EL USUARIO AUTENTICADO (cualquier rol)
	// =====================================================================

	@GetMapping("/misConversiones")
	@Operation(summary = "Ver el historial del usuario autenticado",
			description = "Devuelve únicamente las conversiones del propio usuario.")
	public ResponseEntity<List<HistorialConversionDTO>> misConversiones(
			@AuthenticationPrincipal Usuario usuarioAutenticado) {
		List<HistorialConversionDTO> lista = service.findByUsuarioId(usuarioAutenticado.getId());
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	// =====================================================================
	// ENDPOINTS SOLO PARA ADMIN
	// =====================================================================

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

	@GetMapping("/listar")
	@Operation(summary = "Listar todo el historial (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> listar() {
		List<HistorialConversionDTO> lista = service.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/buscarPorId")
	@Operation(summary = "Buscar un registro de historial por id (SOLO ADMIN)")
	public ResponseEntity<HistorialConversionDTO> buscarPorId(@RequestParam Long id) {
		HistorialConversionDTO encontrado = service.findById(id);
		return new ResponseEntity<>(encontrado, HttpStatus.OK);
	}

	@GetMapping("/porUsuario")
	@Operation(summary = "Listar historial de un usuario por id (SOLO ADMIN)")
	public ResponseEntity<List<HistorialConversionDTO>> porUsuario(@RequestParam Long usuarioId) {
		List<HistorialConversionDTO> lista = service.findByUsuarioId(usuarioId);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

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

	@DeleteMapping("/eliminar")
	@Operation(summary = "Eliminar un registro del historial por id (SOLO ADMIN)")
	public ResponseEntity<String> eliminar(@RequestParam Long id) {
		service.deleteById(id);
		return new ResponseEntity<>("Registro del historial eliminado correctamente.",
				HttpStatus.OK);
	}

	@GetMapping("/contar")
	@Operation(summary = "Contar registros del historial (SOLO ADMIN)")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(service.count(), HttpStatus.OK);
	}
}