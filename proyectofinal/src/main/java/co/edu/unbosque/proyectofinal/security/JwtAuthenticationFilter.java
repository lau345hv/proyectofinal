package co.edu.unbosque.proyectofinal.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro de autenticación JWT que intercepta cada petición HTTP entrante
 * para validar el token Bearer y establecer el contexto de seguridad de Spring.
 *
 * <p>Extiende {@link OncePerRequestFilter} para garantizar que el filtro se
 * ejecuta exactamente una vez por solicitud, independientemente del número
 * de dispatchers involucrados.</p>
 *
 * <h3>Flujo de procesamiento:</h3>
 * <ol>
 *   <li>Extrae el token JWT del encabezado {@code Authorization}
 *       (formato {@code Bearer <token>}).</li>
 *   <li>Guarda el token y el URI de la petición en variables {@link ThreadLocal}
 *       para que {@code AuditoriaService} pueda recuperarlos sin acoplarse
 *       a la capa HTTP.</li>
 *   <li>Extrae el nombre de usuario del token usando {@link JwtUtil}.</li>
 *   <li>Si el usuario existe y el token es válido, construye un
 *       {@link UsernamePasswordAuthenticationToken} y lo establece en el
 *       {@link SecurityContextHolder}.</li>
 *   <li>Continúa la cadena de filtros y, al finalizar, limpia ambos
 *       {@link ThreadLocal} para evitar memory leaks en entornos con
 *       pool de hilos.</li>
 * </ol>
 *
 * @author Equipo de desarrollo
 * @version 2.1
 * @see JwtUtil
 * @see OncePerRequestFilter
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/**
	 * Almacena el token JWT del hilo activo para que los servicios
	 * (en especial {@code AuditoriaService}) puedan recuperarlo sin
	 * acoplarse a la capa HTTP.
	 *
	 * <p>Se limpia en el bloque {@code finally} de
	 * {@link #doFilterInternal} para evitar memory leaks.</p>
	 */
	private static final ThreadLocal<String> TOKEN_HILO = new ThreadLocal<>();

	/**
	 * Almacena el URI de la petición actual para que {@code AuditoriaService}
	 * pueda recuperarlo sin acoplarse a la capa HTTP.
	 *
	 * <p>Se limpia en el bloque {@code finally} de
	 * {@link #doFilterInternal} para evitar memory leaks.</p>
	 */
	private static final ThreadLocal<String> ENDPOINT_HILO = new ThreadLocal<>();

	/** Utilidad para operaciones sobre tokens JWT (extracción, validación). */
	private final JwtUtil jwtUtil;

	/** Servicio para cargar los detalles del usuario a partir del nombre de usuario. */
	private final UserDetailsService userDetailsService;

	/**
	 * Construye el filtro con las dependencias requeridas.
	 *
	 * @param jwtUtil            utilidad para operaciones JWT.
	 * @param userDetailsService servicio para cargar detalles del usuario.
	 */
	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Devuelve el token JWT asociado al hilo de la petición HTTP actual.
	 *
	 * <p>Retorna {@code null} si la petición es anónima o no lleva token.</p>
	 *
	 * @return token JWT en crudo (sin el prefijo {@code "Bearer "}),
	 *         o {@code null} si no está disponible.
	 */
	public String getTokenJwt() {
		return TOKEN_HILO.get();
	}

	/**
	 * Devuelve el URI de la petición HTTP actual almacenado en el
	 * {@link ThreadLocal}.
	 *
	 * @return URI de la petición (p. ej., {@code "/autenticacion/login"}),
	 *         o {@code null} si no hay petición activa.
	 */
	public String getEndpointActual() {
		return ENDPOINT_HILO.get();
	}

	/**
	 * Lógica principal del filtro. Se ejecuta exactamente una vez por petición.
	 *
	 * <p>Extrae el token JWT del encabezado {@code Authorization}, valida su
	 * contenido y, si es correcto, establece la autenticación en el
	 * {@link SecurityContextHolder}. Independientemente del resultado,
	 * continúa la cadena de filtros y limpia los {@link ThreadLocal} al finalizar.</p>
	 *
	 * @param request     petición HTTP entrante.
	 * @param response    respuesta HTTP saliente.
	 * @param filterChain cadena de filtros de Spring Security.
	 * @throws ServletException si ocurre un error en el procesamiento del servlet.
	 * @throws IOException      si ocurre un error de entrada/salida.
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String encabezadoAutorizacion = request.getHeader("Authorization");

		String nombreUsuario = null;
		String jwt = null;

		ENDPOINT_HILO.set(request.getRequestURI());

		if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith("Bearer ")) {
			jwt = encabezadoAutorizacion.substring(7);
			TOKEN_HILO.set(jwt);
			try {
				nombreUsuario = jwtUtil.extractUsername(jwt);
			} catch (Exception e) {
				logger.error("Error al extraer el nombre de usuario del token JWT", e);
			}
		}

		if (nombreUsuario != null
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails =
					this.userDetailsService.loadUserByUsername(nombreUsuario);

			if (jwtUtil.validateToken(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken tokenAutenticacion =
						new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
				tokenAutenticacion.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(tokenAutenticacion);
			}
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			TOKEN_HILO.remove();
			ENDPOINT_HILO.remove();
		}
	}
}