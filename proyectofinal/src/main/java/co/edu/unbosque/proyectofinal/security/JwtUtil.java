package co.edu.unbosque.proyectofinal.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import co.edu.unbosque.proyectofinal.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Componente utilitario para la generación, validación y extracción de
 * información de tokens JWT (JSON Web Token) en el sistema de
 * autenticación.
 * <p>
 * Los tokens generados tienen una validez de 24 horas y son firmados con
 * el algoritmo HMAC-SHA256 usando la clave secreta configurada en
 * {@code application.properties} bajo la propiedad {@code jwt.secret}.
 * </p>
 * <p>
 * Cada token incluye los siguientes claims personalizados además de los
 * estándar:
 * </p>
 * <ul>
 *   <li>{@code rol}: rol del usuario (ej. {@code "USUARIO"} o {@code "ADMIN"})</li>
 *   <li>{@code id}: identificador interno del usuario</li>
 * </ul>
 *
 * @author Equipo de desarrollo
 * @version 2.0
 */
@Component
public class JwtUtil {

	/** Validez del token JWT en milisegundos (24 horas). */
	private static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;

	@Value("${jwt.secret}")
	private String secret;

	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(secret.getBytes());
	}

	/**
	 * Extrae el nombre de usuario (subject) del token JWT.
	 *
	 * @param token token JWT del que se extrae el subject
	 * @return nombre de usuario contenido en el token
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Extrae la fecha de expiración del token JWT.
	 *
	 * @param token token JWT del que se extrae la expiración
	 * @return fecha de expiración del token
	 */
	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	/**
	 * Extrae el rol del usuario contenido en el token.
	 *
	 * @param token token JWT
	 * @return nombre del rol como string (ej. "USUARIO", "ADMIN")
	 */
	public String extractRol(String token) {
		return extractClaim(token, claims -> claims.get("rol", String.class));
	}

	/**
	 * Extrae un claim específico del token JWT usando una función de
	 * resolución.
	 *
	 * @param <T>            tipo del valor del claim a extraer
	 * @param token          token JWT del que se extrae el claim
	 * @param claimsResolver función que define qué claim extraer
	 * @return valor del claim extraído
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Genera un nuevo token JWT para el usuario autenticado.
	 *
	 * @param userDetails detalles del usuario autenticado
	 * @return token JWT firmado como cadena de texto
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		if (userDetails instanceof Usuario u) {
			claims.put("id", u.getId());
			claims.put("rol", u.getRol().name());
		}
		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.signWith(getSigningKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	/**
	 * Valida si un token JWT es válido para el usuario dado.
	 *
	 * @param token       token JWT a validar
	 * @param userDetails detalles del usuario contra el que se valida
	 * @return {@code true} si el token es válido, {@code false} en caso contrario
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String nombreUsuario = extractUsername(token);
		return (nombreUsuario.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}