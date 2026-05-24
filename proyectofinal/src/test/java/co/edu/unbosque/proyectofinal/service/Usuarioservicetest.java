package co.edu.unbosque.proyectofinal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectofinal.dto.AuditoriaDTO;
import co.edu.unbosque.proyectofinal.dto.UsuarioDTO;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.exception.ContrasenaInvalidaException;
import co.edu.unbosque.proyectofinal.exception.CorreoDuplicadoException;
import co.edu.unbosque.proyectofinal.exception.FormatoCorreoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.IdInvalidoException;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.exception.NombreUsuarioDuplicadoException;
import co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectofinal.exception.TextoVacioException;
import co.edu.unbosque.proyectofinal.exception.UsuarioSinPermisoException;
import co.edu.unbosque.proyectofinal.repository.UsuarioRepository;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;

/**
 * Pruebas unitarias para {@link UsuarioService}.
 * Todos los repositorios y dependencias están mockeados con Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Pruebas unitarias")
class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService service;

    @Mock private UsuarioRepository repo;
    @Mock private LanzadorDeExcepcion lanzador;
    @Mock private EmailValidacionService emailValidacionService;
    @Mock private VerificacionCorreoService verificacionCorreoService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditoriaService auditoriaService;

    private UsuarioDTO dtoValido;
    private Usuario usuarioEntidad;

    @BeforeEach
    void setUp() {
        dtoValido = new UsuarioDTO();
        dtoValido.setNombre("Ana");
        dtoValido.setApellido("García");
        dtoValido.setCorreo("ana@gmail.com");
        dtoValido.setNombreUsuario("ana.garcia");
        dtoValido.setContrasena("Clave1234");
        dtoValido.setTelefono("3001234567");
        dtoValido.setRol(RolUsuario.USUARIO);

        usuarioEntidad = new Usuario("Ana", "García", "ana@gmail.com",
                "ana.garcia", "hash", RolUsuario.USUARIO);
        usuarioEntidad.setId(1L);
        usuarioEntidad.setTelefono("3001234567");
    }

   

    @Test
    @DisplayName("create() con datos válidos llama a repo.save()")
    void createValido() {
        when(repo.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(repo.findByNombreUsuario(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashBCrypt");

        int result = service.create(dtoValido);

        assertEquals(0, result);
        verify(repo, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("create() con nombre de usuario 'admin' lanza UsuarioSinPermisoException")
    void createConNombreAdmin() {
        dtoValido.setNombreUsuario("Admin");
        doThrow(new UsuarioSinPermisoException("Reservado")).when(lanzador).lanzarUsuarioSinPermiso(anyString());

        assertThrows(UsuarioSinPermisoException.class, () -> service.create(dtoValido));
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("create() con correo duplicado lanza CorreoDuplicadoException")
    void createCorreoDuplicado() {
        when(repo.findByCorreo("ana@gmail.com")).thenReturn(Optional.of(usuarioEntidad));
        doThrow(new CorreoDuplicadoException("Duplicado")).when(lanzador).lanzarCorreoDuplicado(anyString());

        assertThrows(CorreoDuplicadoException.class, () -> service.create(dtoValido));
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("create() con nombre vacío lanza TextoVacioException")
    void createNombreVacio() {
        dtoValido.setNombre("");
        doThrow(new TextoVacioException("nombre vacío")).when(lanzador).lanzarTextoVacio("nombre");

        assertThrows(TextoVacioException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con correo sin formato válido lanza FormatoCorreoInvalidoException")
    void createCorreoInvalido() {
        dtoValido.setCorreo("noesuncorreo");
        doThrow(new FormatoCorreoInvalidoException("Formato inválido")).when(lanzador).lanzarFormatoCorreoInvalido(anyString());

        assertThrows(FormatoCorreoInvalidoException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con contraseña corta lanza ContrasenaInvalidaException")
    void createContrasenaCorta() {
        dtoValido.setContrasena("abc");
        doThrow(new ContrasenaInvalidaException()).when(lanzador).lanzarContrasenaInvalida();

        assertThrows(ContrasenaInvalidaException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con contraseña solo letras lanza ContrasenaInvalidaException")
    void createContrasenaSoloLetras() {
        dtoValido.setContrasena("SoloLetras");
        doThrow(new ContrasenaInvalidaException()).when(lanzador).lanzarContrasenaInvalida();

        assertThrows(ContrasenaInvalidaException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con nombre usuario duplicado lanza NombreUsuarioDuplicadoException")
    void createNombreUsuarioDuplicado() {
        when(repo.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(repo.findByNombreUsuario("ana.garcia")).thenReturn(Optional.of(usuarioEntidad));
        doThrow(new NombreUsuarioDuplicadoException("Duplicado")).when(lanzador).lanzarNombreUsuarioDuplicado(anyString());

        assertThrows(NombreUsuarioDuplicadoException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() sin rol asigna USUARIO por defecto")
    void createSinRolAsignaUsuarioPorDefecto() {
        dtoValido.setRol(null);
        when(repo.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(repo.findByNombreUsuario(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        service.create(dtoValido);

        verify(repo, times(1)).save(any(Usuario.class));
    }

    

    @Test
    @DisplayName("getAll() retorna lista con todos los usuarios")
    void getAllRetornaLista() {
        when(repo.findAll()).thenReturn(List.of(usuarioEntidad));
        AuditoriaDTO auditoria = new AuditoriaDTO();
        when(auditoriaService.preAccion(TipoAccion.READ, "Consulta de todos los usuarios del sistema"))
                .thenReturn(auditoria);

        List<UsuarioDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getNombre());
        verify(auditoriaService, times(1)).create(any());
    }

    @Test
    @DisplayName("getAll() retorna lista vacía si no hay usuarios")
    void getAllListaVacia() {
        when(repo.findAll()).thenReturn(List.of());
        when(auditoriaService.preAccion(any(), anyString())).thenReturn(new AuditoriaDTO());

        List<UsuarioDTO> result = service.getAll();

        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("deleteById() con ID válido elimina el usuario")
    void deleteByIdValido() {
        when(repo.findById(1L)).thenReturn(Optional.of(usuarioEntidad));
        when(auditoriaService.preAccion(any(), anyString())).thenReturn(new AuditoriaDTO());

        int result = service.deleteById(1L);

        assertEquals(0, result);
        verify(repo, times(1)).delete(usuarioEntidad);
    }

    @Test
    @DisplayName("deleteById() con ID nulo lanza IdInvalidoException")
    void deleteByIdNulo() {
        doThrow(new IdInvalidoException("usuario", null)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.deleteById(null));
        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("deleteById() con ID <= 0 lanza IdInvalidoException")
    void deleteByIdCero() {
        doThrow(new IdInvalidoException("usuario", 0L)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.deleteById(0L));
        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("deleteById() de usuario inexistente lanza RecursoNoEncontradoException")
    void deleteByIdNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        doThrow(new RecursoNoEncontradoException("No existe")).when(lanzador).lanzarRecursoNoEncontrado(anyString());

        assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(99L));
    }

    @Test
    @DisplayName("deleteById() de la cuenta admin lanza UsuarioSinPermisoException")
    void deleteByIdAdmin() {
        Usuario admin = new Usuario("Admin", "System", "admin@correo.com",
                "admin", "hash", RolUsuario.ADMIN);
        admin.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(admin));
        doThrow(new UsuarioSinPermisoException("No se puede eliminar admin")).when(lanzador).lanzarUsuarioSinPermiso(anyString());

        assertThrows(UsuarioSinPermisoException.class, () -> service.deleteById(1L));
        verify(repo, never()).delete(any());
    }

   

    @Test
    @DisplayName("updateById() con datos válidos actualiza el usuario")
    void updateByIdValido() {
        when(repo.findById(1L)).thenReturn(Optional.of(usuarioEntidad));
        when(repo.findByCorreo(anyString())).thenReturn(Optional.empty());
        when(repo.findByNombreUsuario(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("nuevoHash");
        when(auditoriaService.preAccion(any(), anyString())).thenReturn(new AuditoriaDTO());

        int result = service.updateById(1L, dtoValido);

        assertEquals(0, result);
        verify(repo, times(1)).save(usuarioEntidad);
    }

    @Test
    @DisplayName("updateById() con ID nulo lanza IdInvalidoException")
    void updateByIdNulo() {
        doThrow(new IdInvalidoException("usuario", null)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.updateById(null, dtoValido));
    }

    @Test
    @DisplayName("updateById() de usuario inexistente lanza RecursoNoEncontradoException")
    void updateByIdNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        doThrow(new RecursoNoEncontradoException("No existe")).when(lanzador).lanzarRecursoNoEncontrado(anyString());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.updateById(99L, dtoValido));
    }

    @Test
    @DisplayName("updateById() de cuenta admin lanza UsuarioSinPermisoException")
    void updateByIdAdmin() {
        Usuario admin = new Usuario("Admin", "Sys", "admin@a.com",
                "admin", "hash", RolUsuario.ADMIN);
        admin.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(admin));
        doThrow(new UsuarioSinPermisoException("No modificable")).when(lanzador).lanzarUsuarioSinPermiso(anyString());

        assertThrows(UsuarioSinPermisoException.class,
                () -> service.updateById(1L, dtoValido));
    }

  

    @Test
    @DisplayName("count() retorna el total de usuarios")
    void count() {
        when(repo.count()).thenReturn(5L);
        assertEquals(5L, service.count());
    }

    @Test
    @DisplayName("exist() retorna true cuando el usuario existe")
    void existTrue() {
        when(repo.existsById(1L)).thenReturn(true);
        assertTrue(service.exist(1L));
    }

    @Test
    @DisplayName("exist() retorna false para ID null")
    void existIdNull() {
        assertFalse(service.exist(null));
    }

    @Test
    @DisplayName("exist() retorna false cuando el usuario no existe")
    void existFalse() {
        when(repo.existsById(99L)).thenReturn(false);
        assertFalse(service.exist(99L));
    }

   

    @Test
    @DisplayName("existsByNombreUsuario() retorna true cuando existe")
    void existsByNombreUsuarioTrue() {
        when(repo.findByNombreUsuario("ana.garcia"))
                .thenReturn(Optional.of(usuarioEntidad));
        assertTrue(service.existsByNombreUsuario("ana.garcia"));
    }

    @Test
    @DisplayName("existsByNombreUsuario() retorna false para nombre vacío")
    void existsByNombreUsuarioVacio() {
        assertFalse(service.existsByNombreUsuario(""));
    }

    @Test
    @DisplayName("existsByCorreo() retorna true cuando existe")
    void existsByCorreoTrue() {
        when(repo.findByCorreo("ana@gmail.com"))
                .thenReturn(Optional.of(usuarioEntidad));
        assertTrue(service.existsByCorreo("ana@gmail.com"));
    }

    @Test
    @DisplayName("existsByCorreo() retorna false para correo vacío")
    void existsByCorreoVacio() {
        assertFalse(service.existsByCorreo("  "));
    }

 

    @Test
    @DisplayName("findById() retorna DTO cuando el usuario existe")
    void findByIdExiste() {
        when(repo.findById(1L)).thenReturn(Optional.of(usuarioEntidad));

        UsuarioDTO result = service.findById(1L);

        assertNotNull(result);
        assertEquals("Ana", result.getNombre());
        assertEquals("ana@gmail.com", result.getCorreo());
    }

    @Test
    @DisplayName("findById() con ID inválido lanza IdInvalidoException")
    void findByIdInvalido() {
        doThrow(new IdInvalidoException("usuario", -1L)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.findById(-1L));
    }

    @Test
    @DisplayName("findById() con usuario inexistente lanza RecursoNoEncontradoException")
    void findByIdNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        doThrow(new RecursoNoEncontradoException("No existe")).when(lanzador).lanzarRecursoNoEncontrado(anyString());

        assertThrows(RecursoNoEncontradoException.class, () -> service.findById(99L));
    }

    

    @Test
    @DisplayName("findByCorreo() retorna DTO cuando el usuario existe")
    void findByCorreoExiste() {
        when(repo.findByCorreo("ana@gmail.com"))
                .thenReturn(Optional.of(usuarioEntidad));

        UsuarioDTO result = service.findByCorreo("ana@gmail.com");

        assertNotNull(result);
        assertEquals("ana.garcia", result.getNombreUsuario());
    }

    @Test
    @DisplayName("findByCorreo() con correo vacío lanza TextoVacioException")
    void findByCorreoVacio() {
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("correo");

        assertThrows(TextoVacioException.class, () -> service.findByCorreo(""));
    }

    @Test
    @DisplayName("findByCorreo() con formato inválido lanza FormatoCorreoInvalidoException")
    void findByCorreoFormatoInvalido() {
        doThrow(new FormatoCorreoInvalidoException("inválido")).when(lanzador).lanzarFormatoCorreoInvalido(anyString());

        assertThrows(FormatoCorreoInvalidoException.class,
                () -> service.findByCorreo("notAnEmail"));
    }

   

    @Test
    @DisplayName("findByNombreUsuario() retorna DTO cuando existe")
    void findByNombreUsuarioExiste() {
        when(repo.findByNombreUsuario("ana.garcia"))
                .thenReturn(Optional.of(usuarioEntidad));

        UsuarioDTO result = service.findByNombreUsuario("ana.garcia");

        assertNotNull(result);
        assertEquals("Ana", result.getNombre());
    }

    @Test
    @DisplayName("findByNombreUsuario() con nombre vacío lanza TextoVacioException")
    void findByNombreUsuarioVacio() {
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("nombreUsuario");

        assertThrows(TextoVacioException.class,
                () -> service.findByNombreUsuario(""));
    }

  

    @Test
    @DisplayName("findByNombre() retorna lista con los usuarios encontrados")
    void findByNombreEncuentra() {
        when(repo.findByNombre("Ana")).thenReturn(Optional.of(List.of(usuarioEntidad)));

        List<UsuarioDTO> result = service.findByNombre("Ana");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByNombre() con nombre vacío lanza TextoVacioException")
    void findByNombreVacio() {
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("nombre");

        assertThrows(TextoVacioException.class, () -> service.findByNombre(""));
    }

    @Test
    @DisplayName("findByApellido() retorna lista con los usuarios encontrados")
    void findByApellidoEncuentra() {
        when(repo.findByApellido("García"))
                .thenReturn(Optional.of(List.of(usuarioEntidad)));

        List<UsuarioDTO> result = service.findByApellido("García");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByApellido() con apellido vacío lanza TextoVacioException")
    void findByApellidoVacio() {
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("apellido");

        assertThrows(TextoVacioException.class, () -> service.findByApellido(""));
    }

    

    @Test
    @DisplayName("findByRol() retorna lista de usuarios con el rol indicado")
    void findByRolValido() {
        when(repo.findByRol(RolUsuario.USUARIO))
                .thenReturn(Optional.of(List.of(usuarioEntidad)));

        List<UsuarioDTO> result = service.findByRol(RolUsuario.USUARIO);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByRol() con rol null lanza OpcionNoValidaException")
    void findByRolNull() {
        doThrow(new co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException(
                        "rol", "null", "USUARIO, ADMIN")).when(lanzador).lanzarOpcionNoValida(anyString(), anyString(), anyString());

        assertThrows(co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException.class,
                () -> service.findByRol(null));
    }

   

    @Test
    @DisplayName("getAll() no expone la contraseña en el DTO")
    void getAllNoExponeContrasena() {
        when(repo.findAll()).thenReturn(List.of(usuarioEntidad));
        when(auditoriaService.preAccion(any(), anyString())).thenReturn(new AuditoriaDTO());

        List<UsuarioDTO> result = service.getAll();

        assertNotNull(result);
        result.forEach(dto ->
                assertEquals(null, dto.getContrasena(),
                        "La contraseña no debe exponerse en el DTO"));
    }
}