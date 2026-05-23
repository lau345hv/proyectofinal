package co.edu.unbosque.proyectofinal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador REST para la gestión de usuarios autenticados.
 * <p>
 * Todos los endpoints aquí requieren autenticación mediante JWT. La
 * autorización por rol (USUARIO vs ADMIN) está configurada en
 * {@code SecurityConfig}. El usuario autenticado se inyecta
 * automáticamente con {@link AuthenticationPrincipal} a partir del
 * token enviado en el header {@code Authorization: Bearer <token>}.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.0
 */
@RestController
@RequestMapping("/usuario")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private final UsuarioService service;

	public UsuarioController(UsuarioService service) {
		this.service = service;
	}

	// =====================================================================
	// ENDPOINTS PARA EL USUARIO AUTENTICADO (cualquier rol)
	// =====================================================================

	@GetMapping("/miPerfil")
	@Operation(summary = "Consultar el perfil del usuario autenticado")
	public ResponseEntity<UsuarioDTO> miPerfil(@AuthenticationPrincipal Usuario usuarioAutenticado) {
		UsuarioDTO usuario = service.findById(usuarioAutenticado.getId());
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar los datos del usuario autenticado",
			description = "El usuario actualiza SUS propios datos. El id se obtiene "
					+ "del token JWT. El correo y el rol no se pueden modificar por el "
					+ "propio usuario. La contraseña se actualiza solo si se envía con "
					+ "un valor distinto de vacío.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public ResponseEntity<String> actualizar(
			@AuthenticationPrincipal Usuario usuarioAutenticado,
			@RequestBody UsuarioDTO datos) {
		UsuarioDTO actual = service.findById(usuarioAutenticado.getId());
		datos.setId(actual.getId());
		datos.setCorreo(actual.getCorreo());
		datos.setRol(actual.getRol());
		service.updateById(actual.getId(), datos);
		return new ResponseEntity<>("Usuario actualizado correctamente.", HttpStatus.OK);
	}

	// =====================================================================
	// ENDPOINTS SOLO PARA ADMIN
	// =====================================================================

	@GetMapping("/listar")
	@Operation(summary = "Listar todos los usuarios (SOLO ADMIN)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de usuarios"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "403", description = "Solo administradores")
	})
	public ResponseEntity<List<UsuarioDTO>> listar() {
		List<UsuarioDTO> lista = service.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@DeleteMapping("/eliminar")
	@Operation(summary = "Eliminar un usuario por id (SOLO ADMIN)",
			description = "La cuenta admin no puede ser eliminada.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuario eliminado"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "403", description = "Solo administradores"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado")
	})
	public ResponseEntity<String> eliminar(@RequestParam Long id) {
		service.deleteById(id);
		return new ResponseEntity<>("Usuario eliminado correctamente.", HttpStatus.OK);
	}

	@GetMapping("/contar")
	@Operation(summary = "Contar usuarios registrados (SOLO ADMIN)")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(service.count(), HttpStatus.OK);
	}
}