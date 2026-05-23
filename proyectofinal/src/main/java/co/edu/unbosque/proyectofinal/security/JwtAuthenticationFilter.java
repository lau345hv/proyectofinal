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
 * para validar el token Bearer y establecer el contexto de seguridad de
 * Spring.
 * <p>
 * Extiende {@link OncePerRequestFilter} para garantizar que el filtro se
 * ejecuta exactamente una vez por solicitud. El flujo de procesamiento es:
 * </p>
 * <ol>
 *   <li>Extrae el token JWT del encabezado {@code Authorization}
 *       (formato {@code Bearer <token>})</li>
 *   <li>Guarda el token en un {@link ThreadLocal} para que el servicio de
 *       auditoría pueda recuperarlo sin necesidad de pasarlo como
 *       parámetro.</li>
 *   <li>Extrae el nombre de usuario del token usando {@link JwtUtil}</li>
 *   <li>Si el usuario existe y el token es válido, establece la
 *       autenticación en el {@link SecurityContextHolder}</li>
 *   <li>Continúa la cadena de filtros y al terminar limpia el
 *       {@link ThreadLocal} para evitar memory leaks.</li>
 * </ol>
 *
 * @author Equipo de desarrollo
 * @version 2.1
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/**
	 * Almacena el token JWT del hilo activo para que los servicios (en
	 * especial {@code AuditoriaService}) puedan recuperarlo sin acoplarse
	 * a la capa HTTP.
	 */
	private static final ThreadLocal<String> TOKEN_HILO = new ThreadLocal<>();

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Devuelve el token JWT asociado al hilo de la petición HTTP actual.
	 * Retorna {@code null} si la petición es anónima o no lleva token.
	 *
	 * @return token JWT en crudo (sin el prefijo "Bearer "), o {@code null}.
	 */
	public String getTokenJwt() {
		return TOKEN_HILO.get();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String encabezadoAutorizacion = request.getHeader("Authorization");

		String nombreUsuario = null;
		String jwt = null;

		if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith("Bearer ")) {
			jwt = encabezadoAutorizacion.substring(7);
			TOKEN_HILO.set(jwt);
			try {
				nombreUsuario = jwtUtil.extractUsername(jwt);
			} catch (Exception e) {
				logger.error("Error al extraer el nombre de usuario del token JWT", e);
			}
		}

		if (nombreUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.userDetailsService.loadUserByUsername(nombreUsuario);

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
		}
	}
}