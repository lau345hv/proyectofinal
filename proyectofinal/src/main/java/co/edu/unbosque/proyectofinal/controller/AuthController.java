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

import co.edu.unbosque.proyectofinal.dto.AuditoriaDTO;
import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.security.JwtUtil;
import co.edu.unbosque.proyectofinal.service.AuditoriaService;
import co.edu.unbosque.proyectofinal.service.UsuarioService;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST encargado de la autenticación y el registro de usuarios.
 *
 * <p>Expone los endpoints públicos bajo la ruta base {@code /autenticacion}:</p>
 * <ul>
 *   <li>{@code POST /autenticacion/solicitar-codigo}: envía un código de verificación
 *       de 6 dígitos al correo del futuro usuario como primer paso del registro.</li>
 *   <li>{@code POST /autenticacion/register}: completa el registro del usuario
 *       validando el código recibido por correo y devuelve un token JWT.</li>
 *   <li>{@code POST /autenticacion/login}: valida las credenciales del usuario
 *       y devuelve un token JWT para usar en peticiones protegidas.</li>
 *   <li>{@code POST /autenticacion/logout}: registra el cierre de sesión en la
 *       auditoría del sistema.</li>
 * </ul>
 *
 * <p>Todos los endpoints de este controlador son públicos y no requieren
 * autenticación previa, ya que son los puntos de entrada al sistema.</p>
 *
 * @author Equipo de desarrollo
 * @version 1.1
 * @see UsuarioService
 * @see AuditoriaService
 * @see JwtUtil
 */
@RestController
@RequestMapping("/autenticacion")
@Tag(name = "Autenticación",
        description = "Endpoints públicos para registro, login y verificación de correo")
public class AuthController {

    /** Gestor de autenticación de Spring Security. */
    private final AuthenticationManager authenticationManager;

    /** Utilidad para generación y validación de tokens JWT. */
    private final JwtUtil jwtUtil;

    /** Servicio que encapsula la lógica de negocio relacionada con usuarios. */
    private final UsuarioService usuarioService;

    /** Servicio que registra las acciones relevantes en el log de auditoría. */
    private final AuditoriaService auditoriaService;

    /**
     * Construye el controlador inyectando sus dependencias.
     *
     * @param authenticationManager gestor de autenticación de Spring Security
     * @param jwtUtil               utilidad para generar y validar tokens JWT
     * @param usuarioService        servicio con la lógica de negocio de usuarios
     * @param auditoriaService      servicio de registro de auditoría
     */
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            UsuarioService usuarioService, AuditoriaService auditoriaService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    /**
     * <b>Paso 1 del registro</b>: solicita el envío de un código de verificación
     * al correo indicado.
     *
     * <p>El código generado tiene 6 dígitos y una validez de 10 minutos. Se utiliza
     * en el paso 2 ({@link #register(UsuarioDTO, String)}) para confirmar que el
     * correo existe y le pertenece al solicitante.</p>
     *
     * @param correo dirección de correo electrónico a la que se enviará el código;
     *               debe ser válida y no estar registrada previamente en el sistema
     * @return {@code 200 OK} con un mensaje informativo si el código se envió
     *         correctamente, o {@code 400 Bad Request} si el correo es inválido,
     *         no existe o ya está registrado
     */
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

    /**
     * <b>Paso 2 del registro</b>: crea la cuenta del nuevo usuario y devuelve
     * un token JWT listo para usar.
     *
     * <p>El flujo interno es el siguiente:</p>
     * <ol>
     *   <li>Se asigna automáticamente el rol {@link RolUsuario#USUARIO}.</li>
     *   <li>Se delega en {@link UsuarioService#crearConVerificacion(UsuarioDTO, String)}
     *       la validación del código y la persistencia del usuario.</li>
     *   <li>Se autentica al usuario recién creado para obtener el principal.</li>
     *   <li>Se genera un token JWT con {@link JwtUtil#generateToken(UserDetails)}.</li>
     *   <li>Se registra el evento {@link TipoAccion#CREATE} en auditoría.</li>
     * </ol>
     *
     * @param registerRequest objeto con los datos del nuevo usuario (nombre, apellido,
     *                        correo, nombreUsuario, contraseña y teléfono); el campo
     *                        {@code rol} es ignorado y sobreescrito internamente
     * @param codigoVerificacion código de 6 dígitos enviado al correo en el paso 1
     * @return {@code 201 Created} con un {@link AuthResponse} que contiene el token
     *         JWT, el rol, el id y el nombre de usuario del nuevo usuario;
     *         {@code 400 Bad Request} si los datos son inválidos o el código es
     *         incorrecto; {@code 409 Conflict} si el correo o nombre de usuario
     *         ya existen
     */
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

        AuditoriaDTO auditoria = new AuditoriaDTO(
                usuario.getId(),
                usuario.getNombreUsuario(),
                RolUsuario.USUARIO,
                TipoAccion.CREATE,
                "Nuevo usuario registrado: " + usuario.getNombreUsuario()
                        + " (" + usuario.getCorreo() + ")",
                "/autenticacion/register",
                java.time.LocalDateTime.now());
        auditoriaService.create(auditoria);

        AuthResponse respuesta = new AuthResponse(jwt, usuario.getRol().name(),
                usuario.getId(), usuario.getNombreUsuario());
        return new ResponseEntity<>(respuesta, HttpStatus.CREATED);
    }

    /**
     * Autentica a un usuario existente y devuelve un token JWT.
     *
     * <p>Spring Security verifica las credenciales contra la base de datos a través
     * del {@link AuthenticationManager}. Si son válidas, se genera un token JWT y
     * se registra el evento {@link TipoAccion#LOGIN} en auditoría.</p>
     *
     * <p>El token devuelto debe incluirse en el encabezado HTTP de las peticiones
     * protegidas con el formato: {@code Authorization: Bearer <token>}.</p>
     *
     * @param loginRequest objeto con las credenciales del usuario; solo se usan
     *                     los campos {@code nombreUsuario} y {@code contrasena}
     * @return {@code 200 OK} con un {@link AuthResponse} que contiene el token JWT,
     *         el rol, el id y el nombre de usuario; {@code 401 Unauthorized} si
     *         las credenciales son incorrectas
     */
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
        RolUsuario rolEnum = null;
        if (userDetails instanceof Usuario u) {
            rol = u.getRol().name();
            rolEnum = u.getRol();
            id = u.getId();
            nombreUsuario = u.getNombreUsuario();
        }

        AuditoriaDTO auditoria = new AuditoriaDTO(
                id,
                nombreUsuario,
                rolEnum,
                TipoAccion.LOGIN,
                "Inicio de sesión: " + nombreUsuario,
                "/autenticacion/login",
                java.time.LocalDateTime.now());
        auditoriaService.create(auditoria);

        return new ResponseEntity<>(new AuthResponse(jwt, rol, id, nombreUsuario), HttpStatus.OK);
    }

    /**
     * Registra el cierre de sesión del usuario autenticado en la auditoría del sistema.
     *
     * <p>Este endpoint no invalida el token JWT en el servidor (la invalidación es
     * responsabilidad del cliente, que debe descartar el token). Su único efecto es
     * crear un registro de auditoría con la acción {@link TipoAccion#LOGOUT}, siempre
     * que la petición incluya un encabezado {@code Authorization: Bearer <token>}
     * válido.</p>
     *
     * @param request objeto de la petición HTTP del que se extrae el encabezado
     *                {@code Authorization} para identificar al usuario que cierra sesión
     * @return {@code 200 OK} con el mensaje {@code "Sesión cerrada correctamente."}
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión",
            description = "Registra el logout del usuario autenticado en la auditoría. "
                    + "Requiere el token JWT vigente en el header Authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout registrado correctamente")
    })
    public ResponseEntity<String> logout(jakarta.servlet.http.HttpServletRequest request) {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            AuditoriaDTO auditoria = auditoriaService.preAccion(
                    TipoAccion.LOGOUT,
                    "Cierre de sesión");
            auditoriaService.create(auditoria);
        }
        return new ResponseEntity<>("Sesión cerrada correctamente.", HttpStatus.OK);
    }

    /**
     * Representa la respuesta devuelta al cliente tras un inicio de sesión o
     * registro exitoso.
     *
     * <p>Contiene el token JWT generado y los datos básicos de identificación del
     * usuario autenticado. El token debe usarse en el encabezado
     * {@code Authorization: Bearer <token>} de las peticiones posteriores.</p>
     */
    private static class AuthResponse {

        /** Token JWT firmado listo para ser usado en peticiones protegidas. */
        private final String token;

        /** Rol del usuario autenticado ({@code "USUARIO"} o {@code "ADMIN"}). */
        private final String rol;

        /** Identificador único del usuario en la base de datos. */
        private final Long id;

        /** Nombre de usuario único con el que se autenticó. */
        private final String nombreUsuario;

        /**
         * Construye una respuesta de autenticación con todos sus campos.
         *
         * @param token         token JWT generado para la sesión
         * @param rol           nombre del rol asignado al usuario
         * @param id            identificador único del usuario
         * @param nombreUsuario nombre de usuario único
         */
        public AuthResponse(String token, String rol, Long id, String nombreUsuario) {
            this.token = token;
            this.rol = rol;
            this.id = id;
            this.nombreUsuario = nombreUsuario;
        }

        /**
         * Devuelve el token JWT de la sesión.
         *
         * @return token JWT como cadena de texto
         */
        public String getToken() {
            return token;
        }

        /**
         * Devuelve el nombre del rol del usuario autenticado.
         *
         * @return rol como cadena ({@code "USUARIO"} o {@code "ADMIN"})
         */
        public String getRol() {
            return rol;
        }

        /**
         * Devuelve el identificador único del usuario.
         *
         * @return id del usuario en la base de datos
         */
        public Long getId() {
            return id;
        }

        /**
         * Devuelve el nombre de usuario único del autenticado.
         *
         * @return nombre de usuario
         */
        public String getNombreUsuario() {
            return nombreUsuario;
        }
    }
}