
package co.edu.unbosque.proyectofinal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
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
 * {@code SecurityConfig}.
 * </p>
 *
 * <h3>Correcciones aplicadas (DeepSource JAVA-S1061):</h3>
 * <p>
 * Los endpoints {@code miPerfil} y {@code actualizar} recibían anteriormente la
 * entidad JPA {@code Usuario} directamente a través de
 * {@code @AuthenticationPrincipal Usuario usuarioAutenticado}. Esto representa
 * un riesgo de seguridad reconocido (CWE-501: Trust Boundary Violation) porque
 * el objeto de persistencia es manipulado directamente en la capa web, sin
 * pasar por las validaciones del servicio.
 * </p>
 * <p>
 * <strong>Solución:</strong> Se reemplazó la entidad {@code Usuario} por la
 * interfaz {@link UserDetails} de Spring Security. Esta interfaz solo expone
 * información pública del principal (nombre de usuario, roles y estado de la
 * cuenta) sin exponer campos de la entidad JPA como contraseña hasheada,
 * relaciones con otras entidades, etc. A partir del nombre de usuario se delega
 * a {@link UsuarioService} la obtención del {@link UsuarioDTO} con el ID
 * necesario para las operaciones.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 3.1
 */
@RestController
@RequestMapping("/usuario")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

	private final UsuarioService service;

	public UsuarioController(UsuarioService service) {
		this.service = service;
	}

	/**
	 * Devuelve el perfil del usuario que envió el token JWT.
	 * <p>
	 * <strong>Corrección JAVA-S1061:</strong> Se usa {@link UserDetails} en lugar
	 * de la entidad JPA {@code Usuario}. El nombre de usuario se extrae con
	 * {@link UserDetails#getUsername()} y se resuelve el DTO completo a través de
	 * {@link UsuarioService#findByNombreUsuario(String)}, manteniendo el objeto de
	 * persistencia dentro de la capa de servicio.
	 * </p>
	 *
	 * @param userDetails principal inyectado por Spring Security desde el JWT.
	 * @return {@link UsuarioDTO} con los datos del usuario autenticado.
	 */
	@GetMapping("/miPerfil")
	@Operation(summary = "Consultar el perfil del usuario autenticado")
	public ResponseEntity<UsuarioDTO> miPerfil(@AuthenticationPrincipal UserDetails userDetails) {

		
		UsuarioDTO usuario = service.findByNombreUsuario(userDetails.getUsername());
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	/**
	 * Actualiza los datos del usuario autenticado.
	 * <p>
	 * El correo y el rol NO pueden ser modificados por el propio usuario; estos
	 * valores se preservan desde la base de datos. La contraseña se actualiza solo
	 * si se envía con un valor no vacío.
	 * </p>
	 * <p>
	 * <strong>Corrección JAVA-S1061:</strong> Igual que en {@code miPerfil}, se
	 * reemplazó {@code @AuthenticationPrincipal Usuario} por
	 * {@code @AuthenticationPrincipal UserDetails} para evitar recibir el objeto de
	 * persistencia directamente en la capa web. El ID se obtiene consultando al
	 * servicio con el nombre de usuario del principal.
	 * </p>
	 *
	 * @param userDetails principal inyectado por Spring Security desde el JWT.
	 * @param datos       nuevos datos enviados en el cuerpo de la petición.
	 * @return mensaje de confirmación con {@code 200 OK}.
	 */
	@PutMapping("/actualizar")
	@Operation(summary = "Actualizar los datos del usuario autenticado", description = "El usuario actualiza SUS propios datos. El id se obtiene "
			+ "del token JWT. El correo y el rol no se pueden modificar por el "
			+ "propio usuario. La contraseña se actualiza solo si se envía con " + "un valor distinto de vacío.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado") })
	public ResponseEntity<String> actualizar(@AuthenticationPrincipal UserDetails userDetails,
			@RequestBody UsuarioDTO datos) {

		
		UsuarioDTO actual = service.findByNombreUsuario(userDetails.getUsername());
		datos.setId(actual.getId());
		datos.setCorreo(actual.getCorreo());
		datos.setRol(actual.getRol());
		service.updateById(actual.getId(), datos);
		return new ResponseEntity<>("Usuario actualizado correctamente.", HttpStatus.OK);
	}

	@GetMapping("/listar")
	@Operation(summary = "Listar todos los usuarios (SOLO ADMIN)")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "403", description = "Solo administradores") })
	public ResponseEntity<List<UsuarioDTO>> listar() {
		List<UsuarioDTO> lista = service.getAll();
		if (lista.isEmpty()) {
			return new ResponseEntity<>(lista, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@DeleteMapping("/eliminar")
	@Operation(summary = "Eliminar un usuario por id (SOLO ADMIN)", description = "La cuenta admin no puede ser eliminada.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Usuario eliminado"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "403", description = "Solo administradores"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado") })
	public ResponseEntity<String> eliminar(@RequestParam Long id) {
		service.deleteById(id);
		return new ResponseEntity<>("Usuario eliminado correctamente.", HttpStatus.OK);
	}

	@GetMapping("/contar")
	@Operation(summary = "Contar usuarios registrados (SOLO ADMIN)")
	public ResponseEntity<Long> contar() {
		return new ResponseEntity<>(service.count(), HttpStatus.OK);
	}

	/**
	 * Permite al administrador editar cualquier usuario, incluyendo cambiar su ROL
	 * (USUARIO ↔ ADMIN), nombre, apellido, correo, nombreUsuario, teléfono y
	 * contraseña.
	 *
	 * <p>
	 * A diferencia de {@code /usuario/actualizar}, aquí el admin puede modificar el
	 * correo y el rol. La contraseña solo se actualiza si el campo llega con valor
	 * no vacío.
	 * </p>
	 *
	 * @param id    id del usuario a editar (query param).
	 * @param datos nuevos datos enviados en el body.
	 * @return mensaje de confirmación con {@code 200 OK}.
	 */
	@PutMapping("/admin/editar")
	@Operation(summary = "Editar cualquier usuario incluyendo rol (SOLO ADMIN)", description = "El admin puede cambiar nombre, apellido, correo, "
			+ "nombreUsuario, teléfono, rol y contraseña de cualquier "
			+ "usuario. La contraseña solo se actualiza si se envía "
			+ "con valor no vacío. La cuenta 'admin' no puede ser modificada.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
			@ApiResponse(responseCode = "400", description = "Datos inválidos"),
			@ApiResponse(responseCode = "401", description = "Token inválido o ausente"),
			@ApiResponse(responseCode = "403", description = "Solo administradores"),
			@ApiResponse(responseCode = "404", description = "Usuario no encontrado") })
	public ResponseEntity<String> adminEditarUsuario(@RequestParam Long id, @RequestBody UsuarioDTO datos) {

		datos.setId(id);
		service.updateById(id, datos);
		return new ResponseEntity<>("Usuario actualizado correctamente.", HttpStatus.OK);
	}
}