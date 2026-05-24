package co.edu.unbosque.proyectofinal.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link JwtUtil}.
 * Se usa ReflectionTestUtils para inyectar el valor de jwt.secret
 * sin levantar el contexto de Spring.
 */
@DisplayName("JwtUtil - Generación y validación de tokens JWT")
class JwtUtilTest {

    /** Clave secreta de al menos 256 bits (32 caracteres) para HS256. */
    private static final String SECRET =
            "claveSecretaSuperSeguraParaTestsDeJWT1234567890AB";

    private JwtUtil jwtUtil;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
       
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);

        usuario = new Usuario("Laura", "Pérez", "laura@correo.com",
                "laura.p", "Pass5678", RolUsuario.USUARIO);
        usuario.setId(7L);
    }

    

    @Test
    @DisplayName("generateToken devuelve un token no nulo y no vacío")
    void generateTokenNoNulo() {
        String token = jwtUtil.generateToken(usuario);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("generateToken tiene formato JWT (tres segmentos separados por '.')")
    void generateTokenFormatoJWT() {
        String token = jwtUtil.generateToken(usuario);
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length, "Un JWT válido tiene 3 partes separadas por '.'");
    }

    @Test
    @DisplayName("generateToken con ADMIN incluye claims correctos")
    void generateTokenAdmin() {
        usuario.setRol(RolUsuario.ADMIN);
        String token = jwtUtil.generateToken(usuario);
        assertEquals("ADMIN", jwtUtil.extractRol(token));
    }

    

    @Test
    @DisplayName("extractUsername devuelve el nombreUsuario del token")
    void extractUsername() {
        String token = jwtUtil.generateToken(usuario);
        assertEquals("laura.p", jwtUtil.extractUsername(token));
    }

    

    @Test
    @DisplayName("extractRol devuelve el rol correcto para USUARIO")
    void extractRolUsuario() {
        String token = jwtUtil.generateToken(usuario);
        assertEquals("USUARIO", jwtUtil.extractRol(token));
    }

    

    @Test
    @DisplayName("extractExpiration devuelve una fecha futura")
    void extractExpiracionFutura() {
        String token = jwtUtil.generateToken(usuario);
        java.util.Date expiracion = jwtUtil.extractExpiration(token);
        assertNotNull(expiracion);
        assertTrue(expiracion.after(new java.util.Date()),
                "La fecha de expiración debe ser futura");
    }

    

    @Test
    @DisplayName("validateToken devuelve true para token válido y mismo usuario")
    void validateTokenValido() {
        String token = jwtUtil.generateToken(usuario);
        assertTrue(jwtUtil.validateToken(token, usuario));
    }

    @Test
    @DisplayName("validateToken devuelve false para token de otro usuario")
    void validateTokenOtroUsuario() {
        String token = jwtUtil.generateToken(usuario);

        Usuario otro = new Usuario("Pedro", "Ruiz", "pedro@correo.com",
                "pedro.r", "OtraClave1", RolUsuario.USUARIO);
        otro.setId(99L);

        assertFalse(jwtUtil.validateToken(token, otro));
    }

    
    

    @Test
    @DisplayName("extractClaim permite extraer el subject directamente")
    void extractClaimSubject() {
        String token = jwtUtil.generateToken(usuario);
        String subject = jwtUtil.extractClaim(token,
                claims -> claims.getSubject());
        assertEquals("laura.p", subject);
    }
}
