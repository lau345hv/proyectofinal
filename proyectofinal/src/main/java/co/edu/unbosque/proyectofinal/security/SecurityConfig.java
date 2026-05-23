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
 * Clase de configuración central de Spring Security para la plataforma
 * de conversión de archivos.
 *
 * @author Equipo de desarrollo
 * @version 2.2
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
			UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						// Endpoints publicos
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

						// Conversion: cualquier usuario autenticado
						.requestMatchers(HttpMethod.POST, "/conversion/convertir")
								.hasAnyRole("USUARIO", "ADMIN")

						// Endpoints propios del usuario autenticado (USUARIO o ADMIN)
						.requestMatchers(HttpMethod.GET, "/usuario/miPerfil")
								.hasAnyRole("USUARIO", "ADMIN")
						.requestMatchers(HttpMethod.PUT, "/usuario/actualizar")
								.hasAnyRole("USUARIO", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/historial/misConversiones")
								.hasAnyRole("USUARIO", "ADMIN")

						// Endpoints de administracion (SOLO ADMIN)
						.requestMatchers(HttpMethod.GET, "/usuario/listar").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/usuario/contar").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/usuario/eliminar").hasRole("ADMIN")
						.requestMatchers("/historial/**").hasRole("ADMIN")
						.requestMatchers("/diagnostico/**").hasRole("ADMIN")

						.anyRequest().authenticated())
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuracion = new CorsConfiguration();
		configuracion.setAllowedOrigins(Arrays.asList(
				"http://localhost:4200",
				"http://localhost:8080"));
		configuracion.setAllowedMethods(Arrays.asList(
				"GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuracion.setAllowedHeaders(Arrays.asList("*"));
		configuracion.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
		fuente.registerCorsConfiguration("/**", configuracion);
		return fuente;
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder);
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
			throws Exception {
		return config.getAuthenticationManager();
	}
}