package co.edu.unbosque.proyectofinal.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;

/**
 * Pruebas unitarias para la entidad {@link Auditoria}.
 */
@DisplayName("Entidad Auditoria")
class AuditoriaTest {

    private Auditoria auditoria;
    private LocalDateTime fecha;

    @BeforeEach
    void setUp() {
        fecha = LocalDateTime.of(2026, 5, 23, 12, 0);
        auditoria = new Auditoria(1L, "carlos.r", RolUsuario.USUARIO,
                TipoAccion.LOGIN, "Inicio de sesión exitoso",
                "/auth/login", fecha);
    }

    @Test
    @DisplayName("Constructor parametrizado asigna todos los campos")
    void constructorParametrizado() {
        assertEquals(1L, auditoria.getUsuarioId());
        assertEquals("carlos.r", auditoria.getNombreUsuario());
        assertEquals(RolUsuario.USUARIO, auditoria.getRolUsuario());
        assertEquals(TipoAccion.LOGIN, auditoria.getTipoAccion());
        assertEquals("Inicio de sesión exitoso", auditoria.getDescripcion());
        assertEquals("/auth/login", auditoria.getEndpoint());
        assertEquals(fecha, auditoria.getFecha());
    }

    @Test
    @DisplayName("Constructor vacío no lanza excepciones")
    void constructorVacio() {
        
        assertDoesNotThrow(() -> new Auditoria());
    }

    @Test
    @DisplayName("setTipoAccion actualiza el tipo de acción")
    void setTipoAccion() {
        auditoria.setTipoAccion(TipoAccion.CONVERSION);
        assertEquals(TipoAccion.CONVERSION, auditoria.getTipoAccion());
    }

    @Test
    @DisplayName("setRolUsuario actualiza el rol")
    void setRolUsuario() {
        auditoria.setRolUsuario(RolUsuario.ADMIN);
        assertEquals(RolUsuario.ADMIN, auditoria.getRolUsuario());
    }

    @Test
    @DisplayName("setDescripcion actualiza la descripción")
    void setDescripcion() {
        auditoria.setDescripcion("Conversión de audio mp3 a wav");
        assertEquals("Conversión de audio mp3 a wav", auditoria.getDescripcion());
    }

    @Test
    @DisplayName("setEndpoint actualiza el endpoint")
    void setEndpoint() {
        auditoria.setEndpoint("/conversion/convertir");
        assertEquals("/conversion/convertir", auditoria.getEndpoint());
    }

    @Test
    @DisplayName("setFecha actualiza la fecha")
    void setFecha() {
        LocalDateTime nueva = LocalDateTime.of(2026, 6, 1, 9, 0);
        auditoria.setFecha(nueva);
        assertEquals(nueva, auditoria.getFecha());
    }

    @Test
    @DisplayName("setUsuarioId actualiza el id del usuario")
    void setUsuarioId() {
        auditoria.setUsuarioId(42L);
        assertEquals(42L, auditoria.getUsuarioId());
    }

    @Test
    @DisplayName("setNombreUsuario actualiza el nombre de usuario")
    void setNombreUsuario() {
        auditoria.setNombreUsuario("admin.principal");
        assertEquals("admin.principal", auditoria.getNombreUsuario());
    }

    @Test
    @DisplayName("Auditoria anónima permite usuarioId y nombreUsuario nulos")
    void auditoriaAnonima() {
        Auditoria anonima = new Auditoria(null, null, null,
                TipoAccion.READ, "Intento de acceso anónimo",
                "/publico/info", LocalDateTime.now());
        assertNull(anonima.getUsuarioId());
        assertNull(anonima.getNombreUsuario());
        assertNotNull(anonima.getDescripcion());
    }

    @Test
    @DisplayName("equals es reflexivo")
    void equalsReflexivo() {
        assertEquals(auditoria, auditoria);
    }

    @Test
    @DisplayName("Dos auditorias con mismo id son iguales")
    void equalsIgualId() {
        auditoria.setId(5L);
        Auditoria a2 = new Auditoria();
        a2.setId(5L);
        assertEquals(auditoria, a2);
    }

    @Test
    @DisplayName("Dos auditorias con distinto id no son iguales")
    void equalsDistintoId() {
        auditoria.setId(1L);
        Auditoria a2 = new Auditoria();
        a2.setId(2L);
        assertNotEquals(auditoria, a2);
    }

    @Test
    @DisplayName("equals con null devuelve false")
    void equalsConNull() {
        assertNotEquals(null, auditoria);
    }

    @Test
    @DisplayName("hashCode es igual para auditorias con el mismo id")
    void hashCodeIgual() {
        auditoria.setId(7L);
        Auditoria a2 = new Auditoria();
        a2.setId(7L);
        assertEquals(auditoria.hashCode(), a2.hashCode());
    }

    @Test
    @DisplayName("toString contiene nombreUsuario y tipoAccion")
    void toStringContiene() {
        String str = auditoria.toString();
        assertTrue(str.contains("carlos.r") || str.contains("LOGIN"));
    }
}