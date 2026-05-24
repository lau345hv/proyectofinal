package co.edu.unbosque.proyectofinal.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;

/**
 * Pruebas unitarias para la entidad {@link Usuario}.
 */
@DisplayName("Entidad Usuario")
class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Ana", "López", "ana@correo.com",
                "ana.lopez", "Pass1234", RolUsuario.USUARIO);
    }

   

    @Test
    @DisplayName("Constructor parametrizado asigna todos los campos correctamente")
    void constructorParametrizado() {
        assertEquals("Ana", usuario.getNombre());
        assertEquals("López", usuario.getApellido());
        assertEquals("ana@correo.com", usuario.getCorreo());
        assertEquals("ana.lopez", usuario.getNombreUsuario());
        assertEquals("Pass1234", usuario.getContrasena());
        assertEquals(RolUsuario.USUARIO, usuario.getRol());
    }

    @Test
    @DisplayName("Constructor vacío asigna ROL USUARIO por defecto")
    void constructorVacioRolPorDefecto() {
        Usuario u = new Usuario();
        assertEquals(RolUsuario.USUARIO, u.getRol());
    }

   
    @Test
    @DisplayName("setNombre actualiza el nombre")
    void setNombre() {
        usuario.setNombre("María");
        assertEquals("María", usuario.getNombre());
    }

    @Test
    @DisplayName("setApellido actualiza el apellido")
    void setApellido() {
        usuario.setApellido("García");
        assertEquals("García", usuario.getApellido());
    }

    @Test
    @DisplayName("setCorreo actualiza el correo")
    void setCorreo() {
        usuario.setCorreo("nuevo@correo.com");
        assertEquals("nuevo@correo.com", usuario.getCorreo());
    }

    @Test
    @DisplayName("setRol cambia el rol a ADMIN")
    void setRolAdmin() {
        usuario.setRol(RolUsuario.ADMIN);
        assertEquals(RolUsuario.ADMIN, usuario.getRol());
    }

    @Test
    @DisplayName("setTelefono asigna correctamente")
    void setTelefono() {
        usuario.setTelefono("3001234567");
        assertEquals("3001234567", usuario.getTelefono());
    }

    

    @Test
    @DisplayName("getUsername() devuelve el nombreUsuario")
    void getUsername() {
        assertEquals("ana.lopez", usuario.getUsername());
    }

    @Test
    @DisplayName("getPassword() devuelve la contraseña")
    void getPassword() {
        assertEquals("Pass1234", usuario.getPassword());
    }

    @Test
    @DisplayName("getAuthorities() contiene ROLE_USUARIO para rol USUARIO")
    void getAuthoritiesUsuario() {
        var authorities = usuario.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
    }

    @Test
    @DisplayName("getAuthorities() contiene ROLE_ADMIN para rol ADMIN")
    void getAuthoritiesAdmin() {
        usuario.setRol(RolUsuario.ADMIN);
        var authorities = usuario.getAuthorities();
        assertTrue(authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("isAccountNonExpired() devuelve true")
    void accountNonExpired() {
        assertTrue(usuario.isAccountNonExpired());
    }

    @Test
    @DisplayName("isAccountNonLocked() devuelve true")
    void accountNonLocked() {
        assertTrue(usuario.isAccountNonLocked());
    }

    @Test
    @DisplayName("isCredentialsNonExpired() devuelve true")
    void credentialsNonExpired() {
        assertTrue(usuario.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("isEnabled() devuelve true")
    void enabled() {
        assertTrue(usuario.isEnabled());
    }

    

    @Test
    @DisplayName("Modificar lista externa no altera el historial interno")
    void copiaDefensivaSet() {
        List<HistorialConversion> lista = new ArrayList<>();
        usuario.setHistorialConversiones(lista);
        lista.add(new HistorialConversion()); // modificación externa
        assertEquals(0, usuario.getHistorialConversiones().size(),
                "La lista interna no debe verse afectada por cambios en la externa");
    }

    @Test
    @DisplayName("setHistorialConversiones con null no lanza NPE")
    void setHistorialNull() {
        assertDoesNotThrow(() -> usuario.setHistorialConversiones(null));
        assertNotNull(usuario.getHistorialConversiones());
        assertTrue(usuario.getHistorialConversiones().isEmpty());
    }

    
  

    @Test
    @DisplayName("equals es reflexivo")
    void equalsReflexivo() {
        assertEquals(usuario, usuario);
    }

    @Test
    @DisplayName("Dos usuarios con el mismo id son iguales")
    void equalsIgualId() {
        Usuario u2 = new Usuario();
        usuario.setId(1L);
        u2.setId(1L);
        assertEquals(usuario, u2);
    }

    @Test
    @DisplayName("Dos usuarios con distinto id no son iguales")
    void equalsDistintoId() {
        usuario.setId(1L);
        Usuario u2 = new Usuario();
        u2.setId(2L);
        assertNotEquals(usuario, u2);
    }

    @Test
    @DisplayName("equals con null devuelve false")
    void equalsConNull() {
        assertNotEquals(null, usuario);
    }

    @Test
    @DisplayName("hashCode es igual para usuarios con el mismo id")
    void hashCodeIgual() {
        Usuario u2 = new Usuario();
        usuario.setId(5L);
        u2.setId(5L);
        assertEquals(usuario.hashCode(), u2.hashCode());
    }

    @Test
    @DisplayName("toString contiene nombre y nombreUsuario")
    void toStringContiene() {
        String str = usuario.toString();
        assertTrue(str.contains("Ana"));
        assertTrue(str.contains("ana.lopez"));
    }
}
