package co.edu.unbosque.proyectofinal.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectofinal.dto.AuditoriaDTO;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.service.AuditoriaService;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador REST para la gestión y consulta de los registros de
 * auditoría del sistema.
 * <p>
 * Todos los endpoints son exclusivos del rol {@code ADMIN}. La ruta
 * base {@code /auditoria/**} ya está protegida en {@code SecurityConfig}
 * con {@code hasRole("ADMIN")}, por lo que no se requieren anotaciones
 * de seguridad adicionales en los métodos.
 * </p>
 * <p>
 * Los registros de auditoría son de <b>solo lectura</b> desde este
 * controlador: se crean automáticamente desde los servicios de negocio
 * (conversión, usuario, historial) y únicamente el admin puede
 * consultarlos o eliminarlos si es necesario.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@RestController
@RequestMapping("/auditoria")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaController {

	private final AuditoriaService auditoriaService;
	private final LanzadorDeExcepcion lanzador;

	public AuditoriaController(AuditoriaService auditoriaService,
			LanzadorDeExcepcion lanzador) {
		this.auditoriaService = auditoriaService;
		this.lanzador = lanzador;
	}

	@GetMapping("/listar")
	@Operation(summary = "Listar todos los registros de auditoría (SOLO ADMIN)")
	public ResponseEntity<List<AuditoriaDTO>> listar() {
		List<AuditoriaDTO> lista = auditoriaService.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/buscarPorId")
	@Operation(summary = "Buscar un registro de auditoría por id (SOLO ADMIN)")
	public ResponseEntity<AuditoriaDTO> buscarPorId(@RequestParam Long id) {
		AuditoriaDTO encontrado = auditoriaService.findById(id);
		return new ResponseEntity<>(encontrado, HttpStatus.OK);
	}

	@GetMapping("/porUsuario")
	@Operation(summary = "Listar auditoría de un usuario por su id (SOLO ADMIN)")
	public ResponseEntity<List<AuditoriaDTO>> porUsuario(@RequestParam Long usuarioId) {
		List<AuditoriaDTO> lista = auditoriaService.findByUsuarioId(usuarioId);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/porNombreUsuario")
	@Operation(summary = "Listar auditoría filtrando por nombre de usuario (SOLO ADMIN)")
	public ResponseEntity<List<AuditoriaDTO>> porNombreUsuario(
			@RequestParam String nombreUsuario) {
		List<AuditoriaDTO> lista = auditoriaService.findByNombreUsuario(nombreUsuario);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/porTipoAccion")
	@Operation(summary = "Filtrar auditoría por tipo de acción (SOLO ADMIN)")
	public ResponseEntity<List<AuditoriaDTO>> porTipoAccion(
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = {
						"CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "CONVERSION" }))
				String tipoAccion) {
		TipoAccion tipo;
		try {
			tipo = TipoAccion.valueOf(tipoAccion.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("tipoAccion", tipoAccion,
					"CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, CONVERSION");
			return null;
		}
		List<AuditoriaDTO> lista = auditoriaService.findByTipoAccion(tipo);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/porRol")
	@Operation(summary = "Filtrar auditoría por rol del usuario (SOLO ADMIN)")
	public ResponseEntity<List<AuditoriaDTO>> porRol(
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = { "USUARIO", "ADMIN" }))
				String rolUsuario) {
		RolUsuario rol;
		try {
			rol = RolUsuario.valueOf(rolUsuario.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("rolUsuario", rolUsuario, "USUARIO, ADMIN");
			return null;
		}
		List<AuditoriaDTO> lista = auditoriaService.findByRolUsuario(rol);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/porFechas")
	@Operation(summary = "Filtrar auditoría por rango de fechas (SOLO ADMIN)",
			description = "El formato es ISO 8601: yyyy-MM-ddTHH:mm:ss")
	public ResponseEntity<List<AuditoriaDTO>> porFechas(
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
		List<AuditoriaDTO> lista = auditoriaService.findByFechaBetween(inicio, fin);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/porUsuarioYAccion")
	@Operation(summary = "Filtrar auditoría por usuario y tipo de acción (SOLO ADMIN)")
	public ResponseEntity<List<AuditoriaDTO>> porUsuarioYAccion(
			@RequestParam Long usuarioId,
			@RequestParam
				@Parameter(schema = @Schema(allowableValues = {
						"CREATE", "READ", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "CONVERSION" }))
				String tipoAccion) {
		TipoAccion tipo;
		try {
			tipo = TipoAccion.valueOf(tipoAccion.toUpperCase());
		} catch (IllegalArgumentException e) {
			lanzador.lanzarOpcionNoValida("tipoAccion", tipoAccion,
					"CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, CONVERSION");
			return null;
		}
		List<AuditoriaDTO> lista =
				auditoriaService.findByUsuarioIdAndTipoAccion(usuarioId, tipo);
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@GetMapping("/contar")
	@Operation(summary = "Contar total de registros de auditoría (SOLO ADMIN)")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(auditoriaService.count(), HttpStatus.OK);
	}

	@DeleteMapping("/eliminar")
	@Operation(summary = "Eliminar un registro de auditoría por id (SOLO ADMIN)")
	public ResponseEntity<String> eliminar(@RequestParam Long id) {
		auditoriaService.deleteById(id);
		return new ResponseEntity<>("Registro de auditoría eliminado correctamente.",
				HttpStatus.OK);
	}
}