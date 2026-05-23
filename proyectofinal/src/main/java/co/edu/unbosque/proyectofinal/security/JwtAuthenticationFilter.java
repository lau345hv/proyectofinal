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
 *   <li>Extrae el nombre de usuario del token usando {@link JwtUtil}</li>
 *   <li>Si el usuario existe y el token es válido, establece la
 *       autenticación en el {@link SecurityContextHolder}</li>
 *   <li>Continúa la cadena de filtros</li>
 * </ol>
 *
 * @author Equipo de desarrollo
 * @version 2.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		final String encabezadoAutorizacion = request.getHeader("Authorization");

		String nombreUsuario = null;
		String jwt = null;

		if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith("Bearer ")) {
			jwt = encabezadoAutorizacion.substring(7);
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

		filterChain.doFilter(request, response);
	}
}