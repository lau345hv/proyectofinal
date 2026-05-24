package co.edu.unbosque.proyectofinal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;

/**
 * Pruebas unitarias para {@link UsuarioDTO}.
 * Incluye verificación de copia defensiva (corrección JAVA-E1086).
 */
@DisplayName("DTO UsuarioDTO")
class UsuarioDTOTest {

    private UsuarioDTO dto;

    @BeforeEach
    void setUp() {
        dto = new UsuarioDTO("Carlos", "Gómez", "carlos@correo.com",
                "carlos.g", "Clave123", RolUsuario.USUARIO);
    }

    
    @Test
    @DisplayName("Constructor vacío crea un DTO sin campos asignados")
    void constructorVacio() {
        UsuarioDTO vacio = new UsuarioDTO();
        assertNull(vacio.getNombre());
        assertNull(vacio.getCorreo());
        assertNotNull(vacio.getHistorialConversionesId(), "La lista no debe ser null");
        assertTrue(vacio.getHistorialConversionesId().isEmpty());
    }

    @Test
    @DisplayName("Constructor con nombreUsuario y contraseña asigna solo esos campos")
    void constructorCredenciales() {
        UsuarioDTO dto2 = new UsuarioDTO("usuario123", "MiClave1");
        assertEquals("usuario123", dto2.getNombreUsuario());
        assertEquals("MiClave1", dto2.getContrasena());
        assertNull(dto2.getNombre());
    }

    @Test
    @DisplayName("Constructor completo asigna todos los campos correctamente")
    void constructorCompleto() {
        assertEquals("Carlos", dto.getNombre());
        assertEquals("Gómez", dto.getApellido());
        assertEquals("carlos@correo.com", dto.getCorreo());
        assertEquals("carlos.g", dto.getNombreUsuario());
        assertEquals("Clave123", dto.getContrasena());
        assertEquals(RolUsuario.USUARIO, dto.getRol());
    }

    

    @Test
    @DisplayName("setId y getId funcionan correctamente")
    void setGetId() {
        dto.setId(10L);
        assertEquals(10L, dto.getId());
    }

    @Test
    @DisplayName("setTelefono y getTelefono funcionan correctamente")
    void setGetTelefono() {
        dto.setTelefono("3009876543");
        assertEquals("3009876543", dto.getTelefono());
    }

    @Test
    @DisplayName("setRol a ADMIN y getRol devuelve ADMIN")
    void setGetRolAdmin() {
        dto.setRol(RolUsuario.ADMIN);
        assertEquals(RolUsuario.ADMIN, dto.getRol());
    }

    

    @Test
    @DisplayName("Modificar la lista externa no altera la lista interna del DTO (setter defensivo)")
    void setHistorialCopiaDefensiva() {
        List<Long> ids = new ArrayList<>(List.of(1L, 2L, 3L));
        dto.setHistorialConversionesId(ids);
        ids.clear(); // modificación externa
        assertEquals(3, dto.getHistorialConversionesId().size(),
                "El DTO debe conservar su copia con los 3 ids originales");
    }

    @Test
    @DisplayName("Modificar la lista devuelta por el getter no altera la lista interna (getter defensivo)")
    void getHistorialCopiaDefensiva() {
        dto.setHistorialConversionesId(new ArrayList<>(List.of(10L, 20L)));
        List<Long> retornada = dto.getHistorialConversionesId();
        retornada.add(30L); 
        assertEquals(2, dto.getHistorialConversionesId().size(),
                "La lista interna no debe verse afectada por cambios en la lista devuelta");
    }

    @Test
    @DisplayName("setHistorialConversionesId con null asigna lista vacía")
    void setHistorialNull() {
        dto.setHistorialConversionesId(null);
        assertNotNull(dto.getHistorialConversionesId());
        assertTrue(dto.getHistorialConversionesId().isEmpty());
    }

    @Test
    @DisplayName("setHistorialConversionesId con lista vacía asigna lista vacía")
    void setHistorialVacia() {
        dto.setHistorialConversionesId(new ArrayList<>());
        assertTrue(dto.getHistorialConversionesId().isEmpty());
    }

   

    @Test
    @DisplayName("equals es reflexivo")
    void equalsReflexivo() {
        assertEquals(dto, dto);
    }

    @Test
    @DisplayName("Dos DTOs con el mismo id son iguales")
    void equalsIgualId() {
        dto.setId(1L);
        UsuarioDTO dto2 = new UsuarioDTO();
        dto2.setId(1L);
        assertEquals(dto, dto2);
    }

    @Test
    @DisplayName("Dos DTOs con distinto id no son iguales")
    void equalsDistintoId() {
        dto.setId(1L);
        UsuarioDTO dto2 = new UsuarioDTO();
        dto2.setId(2L);
        assertNotEquals(dto, dto2);
    }

    @Test
    @DisplayName("equals con objeto de otro tipo devuelve false")
    void equalsOtroTipo() {
        assertNotEquals("string", dto);
    }

    @Test
    @DisplayName("equals con null devuelve false")
    void equalsConNull() {
        assertNotEquals(null, dto);
    }

    @Test
    @DisplayName("hashCode igual para DTOs con el mismo id")
    void hashCodeIgual() {
        dto.setId(3L);
        UsuarioDTO dto2 = new UsuarioDTO();
        dto2.setId(3L);
        assertEquals(dto.hashCode(), dto2.hashCode());
    }

    @Test
    @DisplayName("toString contiene nombre y nombreUsuario")
    void toStringContiene() {
        String str = dto.toString();
        assertTrue(str.contains("Carlos") || str.contains("carlos.g"));
    }
}
