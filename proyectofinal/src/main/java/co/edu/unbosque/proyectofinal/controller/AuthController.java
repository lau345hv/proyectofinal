package co.edu.unbosque.proyectofinal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.security.JwtUtil;
import co.edu.unbosque.proyectofinal.service.UsuarioService;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de la autenticación y el registro de
 * usuarios.
 * <p>
 * Expone los endpoints públicos bajo la ruta {@code /autenticacion}:
 * </p>
 * <ul>
 *   <li>{@code POST /autenticacion/solicitar-codigo}: envía un código
 *       de verificación al correo del nuevo usuario.</li>
 *   <li>{@code POST /autenticacion/register}: registra un nuevo usuario
 *       tras validar el código y devuelve un token JWT.</li>
 *   <li>{@code POST /autenticacion/login}: valida credenciales y devuelve
 *       un token JWT que debe usarse en peticiones protegidas.</li>
 * </ul>
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
@RestController
@RequestMapping("/autenticacion")
@Tag(name = "Autenticación",
		description = "Endpoints públicos para registro, login y verificación de correo")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UsuarioService usuarioService;

	public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UsuarioService usuarioService) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.usuarioService = usuarioService;
	}

	@PostMapping("/solicitar-codigo")
	@Operation(summary = "PASO 1 - Solicitar código de verificación por correo",
			description = "Envía un código de 6 dígitos al correo indicado para "
					+ "verificar su existencia antes de crear la cuenta.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Código enviado al correo"),
			@ApiResponse(responseCode = "400",
					description = "Correo inválido, no existe o ya está registrado")
	})
	public ResponseEntity<String> solicitarCodigo(@RequestParam String correo) {
		usuarioService.solicitarCodigoRegistro(correo);
		return new ResponseEntity<>(
				"Se ha enviado un código de verificación al correo " + correo
						+ ". Revise su bandeja de entrada (también la carpeta de spam). "
						+ "El código es válido por 10 minutos.",
				HttpStatus.OK);
	}

	@PostMapping("/register")
	@Operation(summary = "PASO 2 - Registrar un nuevo usuario cliente",
			description = "Crea una nueva cuenta de cliente. Requiere el código de "
					+ "verificación enviado al correo. El rol se asigna automáticamente "
					+ "como USUARIO. Devuelve un token JWT para usar inmediatamente.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Usuario creado y autenticado"),
			@ApiResponse(responseCode = "400",
					description = "Datos inválidos o código incorrecto"),
			@ApiResponse(responseCode = "409", description = "Usuario o correo ya existen")
	})
	public ResponseEntity<AuthResponse> register(@RequestBody UsuarioDTO registerRequest,
			@RequestParam String codigoVerificacion) {
		registerRequest.setRol(RolUsuario.USUARIO);
		usuarioService.crearConVerificacion(registerRequest, codigoVerificacion);
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						registerRequest.getNombreUsuario(), registerRequest.getContrasena()));
		Usuario usuario = (Usuario) authentication.getPrincipal();
		String jwt = jwtUtil.generateToken(usuario);
		AuthResponse respuesta = new AuthResponse(jwt, usuario.getRol().name(),
				usuario.getId(), usuario.getNombreUsuario());
		return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
	}

	@PostMapping("/login")
	@Operation(summary = "Iniciar sesión",
			description = "Valida credenciales y devuelve un token JWT que debe ser "
					+ "enviado en el header 'Authorization: Bearer <token>' en todas "
					+ "las peticiones protegidas.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
			@ApiResponse(responseCode = "401", description = "Credenciales inválidas")
	})
	public ResponseEntity<AuthResponse> login(@RequestBody UsuarioDTO loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						loginRequest.getNombreUsuario(), loginRequest.getContrasena()));
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String jwt = jwtUtil.generateToken(userDetails);
		String rol = null;
		Long id = null;
		String nombreUsuario = null;
		if (userDetails instanceof Usuario u) {
			rol = u.getRol().name();
			id = u.getId();
			nombreUsuario = u.getNombreUsuario();
		}
		return new ResponseEntity<>(new AuthResponse(jwt, rol, id, nombreUsuario), HttpStatus.OK);
	}

	/**
	 * Clase interna estática que representa la respuesta de autenticación
	 * enviada al cliente tras un login o registro exitoso.
	 * <p>
	 * Contiene el token JWT generado, el rol del usuario, su ID y su
	 * nombre de usuario.
	 * </p>
	 */
	private static class AuthResponse {

		private final String token;
		private final String rol;
		private final Long id;
		private final String nombreUsuario;

		public AuthResponse(String token, String rol, Long id, String nombreUsuario) {
			this.token = token;
			this.rol = rol;
			this.id = id;
			this.nombreUsuario = nombreUsuario;
		}

		public String getToken() {
			return token;
		}

		public String getRol() {
			return rol;
		}

		public Long getId() {
			return id;
		}

		public String getNombreUsuario() {
			return nombreUsuario;
		}
	}
}