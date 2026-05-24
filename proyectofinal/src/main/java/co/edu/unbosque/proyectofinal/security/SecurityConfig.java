package co.edu.unbosque.proyectofinal.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración central de Spring Security para la plataforma de
 * conversión de archivos.
 *
 * <p>Define la cadena de filtros de seguridad, la política de sesiones
 * sin estado (JWT), las reglas de autorización por rol y la configuración
 * CORS. También registra los beans de infraestructura necesarios para la
 * autenticación.</p>
 *
 * <h3>Resumen de reglas de acceso:</h3>
 * <ul>
 *   <li><strong>Público (sin autenticación):</strong>
 *       {@code /autenticacion/**}, Swagger UI, {@code GET /conversion/formatos}.</li>
 *   <li><strong>USUARIO o ADMIN:</strong>
 *       {@code POST /conversion/convertir},
 *       {@code GET /usuario/miPerfil},
 *       {@code PUT /usuario/actualizar},
 *       {@code GET /historial/misConversiones},
 *       {@code GET /historial/descargarOriginal}.</li>
 *   <li><strong>Solo ADMIN:</strong>
 *       {@code /usuario/listar}, {@code /usuario/contar},
 *       {@code DELETE /usuario/eliminar},
 *       {@code PUT /usuario/admin/editar},
 *       {@code /historial/**}, {@code /diagnostico/**},
 *       {@code /auditoria/**}.</li>
 *   <li><strong>Cualquier otra ruta:</strong> requiere autenticación.</li>
 * </ul>
 *
 * <h3>Política de sesiones:</h3>
 * <p>Se usa {@link SessionCreationPolicy#STATELESS}: no se crea ni usa
 * sesión HTTP. La identidad del usuario se verifica en cada petición
 * mediante el token JWT procesado por {@link JwtAuthenticationFilter}.</p>
 *
 * @author Equipo de desarrollo
 * @version 2.3
 * @see JwtAuthenticationFilter
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/** Filtro que valida el token JWT en cada petición entrante. */
	private final JwtAuthenticationFilter jwtAuthFilter;

	/** Servicio para cargar los detalles del usuario durante la autenticación. */
	private final UserDetailsService userDetailsService;

	/** Codificador de contraseñas utilizado por el proveedor de autenticación. */
	private final PasswordEncoder passwordEncoder;

	/**
	 * Construye la configuración de seguridad con las dependencias requeridas.
	 *
	 * @param jwtAuthFilter      filtro JWT que intercepta las peticiones entrantes.
	 * @param userDetailsService servicio para cargar detalles del usuario.
	 * @param passwordEncoder    codificador de contraseñas.
	 */
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
			UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Define la cadena de filtros de seguridad HTTP.
	 *
	 * <p>Configura CORS, deshabilita CSRF (innecesario en APIs sin estado),
	 * establece las reglas de autorización por rol, aplica la política de
	 * sesiones sin estado y registra {@link JwtAuthenticationFilter} antes
	 * del filtro estándar de autenticación por formulario.</p>
	 *
	 * @param http objeto {@link HttpSecurity} provisto por Spring Security.
	 * @return la cadena de filtros construida y lista para usar.
	 * @throws Exception si ocurre algún error durante la configuración.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/autenticacion/**").permitAll()
						.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs",
								"/v3/api-docs/**",
								"/v3/api-docs.yaml",
								"/swagger-resources/**",
								"/webjars/**",
								"/").permitAll()
						.requestMatchers(HttpMethod.GET, "/conversion/formatos").permitAll()
						.requestMatchers(HttpMethod.POST, "/conversion/convertir")
								.hasAnyRole("USUARIO", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/usuario/miPerfil")
								.hasAnyRole("USUARIO", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/usuario/actualizar")
								.hasAnyRole("USUARIO", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/historial/misConversiones")
								.hasAnyRole("USUARIO", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/historial/descargarOriginal")
								.hasAnyRole("USUARIO", "ADMIN")

						.requestMatchers(HttpMethod.GET, "/usuario/listar").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/usuario/contar").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/usuario/eliminar").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/usuario/admin/editar").hasRole("ADMIN")
						.requestMatchers("/historial/**").hasRole("ADMIN")
						.requestMatchers("/diagnostico/**").hasRole("ADMIN")
						.requestMatchers("/auditoria/**").hasRole("ADMIN")

						.anyRequest().authenticated())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Crea y configura la fuente de configuración CORS.
	 *
	 * <p>Permite peticiones desde cualquier origen usando
	 * {@code setAllowedOriginPatterns("*")}, lo cual es compatible con
	 * {@code allowCredentials(true)} y permite el envío del token JWT
	 * en el encabezado Authorization desde cualquier dominio, incluyendo
	 * entornos de producción como el servidor Tomcat del profesor.</p>
	 *
	 * @return fuente de configuración CORS registrada para todas las rutas
	 *         ({@code /**}).
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuracion = new CorsConfiguration();
		
		configuracion.setAllowedOriginPatterns(Arrays.asList("*"));
		configuracion.setAllowedMethods(Arrays.asList(
				"GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuracion.setAllowedHeaders(Arrays.asList("*"));
		configuracion.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
		fuente.registerCorsConfiguration("/**", configuracion);
		return fuente;
	}

	/**
	 * Crea el proveedor de autenticación basado en DAO.
	 *
	 * <p>Utiliza {@link UserDetailsService} para cargar el usuario desde la
	 * base de datos y {@link PasswordEncoder} para verificar la contraseña.</p>
	 *
	 * @return proveedor de autenticación configurado.
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider =
				new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	/**
	 * Expone el {@link AuthenticationManager} de Spring Security como bean
	 * para que pueda ser inyectado en el servicio de autenticación.
	 *
	 * @param config configuración de autenticación provista por Spring.
	 * @return el {@link AuthenticationManager} activo.
	 * @throws Exception si ocurre un error al obtener el manager.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
			throws Exception {
		return config.getAuthenticationManager();
	}
}