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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.proyectofinal.dto.HistorialConversionDTO;
import co.edu.unbosque.proyectofinal.entity.HistorialConversion;
import co.edu.unbosque.proyectofinal.entity.Usuario;
import co.edu.unbosque.proyectofinal.exception.DatoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.IdInvalidoException;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;
import co.edu.unbosque.proyectofinal.exception.OpcionNoValidaException;
import co.edu.unbosque.proyectofinal.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectofinal.exception.TextoVacioException;
import co.edu.unbosque.proyectofinal.repository.HistorialConversionRepository;
import co.edu.unbosque.proyectofinal.repository.UsuarioRepository;
import co.edu.unbosque.proyectofinal.util.AESUtil;
import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Pruebas unitarias para {@link HistorialConversionService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HistorialConversionService - Pruebas unitarias")
class HistorialConversionServiceTest {

    @InjectMocks private HistorialConversionService service;
    @Mock private HistorialConversionRepository repo;
    @Mock private UsuarioRepository usuarioRepo;
    @Mock private LanzadorDeExcepcion lanzador;

    private HistorialConversionDTO dtoValido;
    private Usuario usuario;
    private HistorialConversion entidad;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Ana", "García", "ana@gmail.com",
                "ana.garcia", "hash", RolUsuario.USUARIO);
        usuario.setId(1L);

        dtoValido = new HistorialConversionDTO();
        dtoValido.setUsuarioId(1L);
        dtoValido.setTipoArchivo(TipoArchivo.VIDEO);
        dtoValido.setFormatoOrigen("mkv");
        dtoValido.setFormatoDestino("mp4");
        dtoValido.setNombreArchivoOriginal("video.mkv");
        dtoValido.setNombreArchivoConvertido("video.mp4");
        dtoValido.setRutaArchivoOriginal("/uploads/video.mkv");
        dtoValido.setRutaArchivoConvertido("/converted/video.mp4");
        dtoValido.setEstado(EstadoConversion.COMPLETADO);

        entidad = new HistorialConversion(
                LocalDateTime.now(), TipoArchivo.VIDEO, "mkv", "mp4",
                AESUtil.encrypt("video.mkv"),   
                "video.mp4",                    
                AESUtil.encrypt("/uploads/video.mkv"),   
                AESUtil.encrypt("/converted/video.mp4"), 
                EstadoConversion.COMPLETADO, usuario);
        entidad.setId(1L);
    }

   

    @Test
    @DisplayName("create() con datos válidos llama a repo.save()")
    void createValido() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));

        int result = service.create(dtoValido);

        assertEquals(0, result);
        verify(repo, times(1)).save(any(HistorialConversion.class));
    }

    @Test
    @DisplayName("create() con DTO nulo lanza DatoInvalidoException")
    void createDtoNulo() {
        doThrow(new DatoInvalidoException("Nulo")).when(lanzador).lanzarDatoInvalido(anyString());

        assertThrows(DatoInvalidoException.class, () -> service.create(null));
        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("create() con usuarioId nulo lanza IdInvalidoException")
    void createUsuarioIdNulo() {
        dtoValido.setUsuarioId(null);
        doThrow(new IdInvalidoException("usuario", null)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con usuario inexistente lanza RecursoNoEncontradoException")
    void createUsuarioNoExiste() {
        when(usuarioRepo.findById(1L)).thenReturn(Optional.empty());
        doThrow(new RecursoNoEncontradoException("No existe")).when(lanzador).lanzarRecursoNoEncontrado(anyString());

        assertThrows(RecursoNoEncontradoException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con tipoArchivo nulo lanza OpcionNoValidaException")
    void createTipoArchivoNulo() {
        dtoValido.setTipoArchivo(null);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
        doThrow(new OpcionNoValidaException("tipoArchivo", "null", "AUDIO, VIDEO, IMAGEN")).when(lanzador).lanzarOpcionNoValida(anyString(), anyString(), anyString());

        assertThrows(OpcionNoValidaException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() con formatoOrigen vacío lanza TextoVacioException")
    void createFormatoOrigenVacio() {
        dtoValido.setFormatoOrigen("");
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("formatoOrigen");

        assertThrows(TextoVacioException.class, () -> service.create(dtoValido));
    }

    @Test
    @DisplayName("create() sin estado asigna PENDIENTE por defecto")
    void createSinEstadoAsignaPendiente() {
        dtoValido.setEstado(null);
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));

        service.create(dtoValido);

        verify(repo, times(1)).save(any(HistorialConversion.class));
    }

    

    @Test
    @DisplayName("getAll() retorna lista de DTOs")
    void getAllRetornaLista() {
        when(repo.findAll()).thenReturn(List.of(entidad));

        List<HistorialConversionDTO> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals(TipoArchivo.VIDEO, result.get(0).getTipoArchivo());
    }

    @Test
    @DisplayName("getAll() retorna lista vacía si no hay historial")
    void getAllListaVacia() {
        when(repo.findAll()).thenReturn(List.of());

        assertTrue(service.getAll().isEmpty());
    }

    

    @Test
    @DisplayName("deleteById() con ID válido elimina el registro")
    void deleteByIdValido() {
        when(repo.findById(1L)).thenReturn(Optional.of(entidad));

        int result = service.deleteById(1L);

        assertEquals(0, result);
        verify(repo, times(1)).delete(entidad);
    }

    @Test
    @DisplayName("deleteById() con ID nulo lanza IdInvalidoException")
    void deleteByIdNulo() {
        doThrow(new IdInvalidoException("historialConversion", null)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.deleteById(null));
    }

    @Test
    @DisplayName("deleteById() con ID <= 0 lanza IdInvalidoException")
    void deleteByIdCero() {
        doThrow(new IdInvalidoException("historialConversion", 0L)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.deleteById(0L));
    }

    @Test
    @DisplayName("deleteById() de registro inexistente lanza RecursoNoEncontradoException")
    void deleteByIdNoExiste() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        doThrow(new RecursoNoEncontradoException("No existe")).when(lanzador).lanzarRecursoNoEncontrado(anyString());

        assertThrows(RecursoNoEncontradoException.class, () -> service.deleteById(99L));
    }

  

    @Test
    @DisplayName("count() retorna el total de registros")
    void count() {
        when(repo.count()).thenReturn(3L);
        assertEquals(3L, service.count());
    }

    @Test
    @DisplayName("exist() retorna true cuando el registro existe")
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
    @DisplayName("findById() retorna DTO cuando el registro existe")
    void findByIdExiste() {
        when(repo.findById(1L)).thenReturn(Optional.of(entidad));

        HistorialConversionDTO result = service.findById(1L);

        assertNotNull(result);
        assertEquals(TipoArchivo.VIDEO, result.getTipoArchivo());
    }

    @Test
    @DisplayName("findById() con ID inválido lanza IdInvalidoException")
    void findByIdInvalido() {
        doThrow(new IdInvalidoException("historialConversion", -1L)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.findById(-1L));
    }

    

    @Test
    @DisplayName("findByUsuarioId() retorna lista de conversiones del usuario")
    void findByUsuarioIdValido() {
        when(usuarioRepo.existsById(1L)).thenReturn(true);
        when(repo.findByUsuarioId(1L)).thenReturn(Optional.of(List.of(entidad)));

        List<HistorialConversionDTO> result = service.findByUsuarioId(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByUsuarioId() con ID inválido lanza IdInvalidoException")
    void findByUsuarioIdInvalido() {
        doThrow(new IdInvalidoException("usuario", 0L)).when(lanzador).lanzarIdInvalido(anyString(), any());

        assertThrows(IdInvalidoException.class, () -> service.findByUsuarioId(0L));
    }

    @Test
    @DisplayName("findByUsuarioId() con usuario inexistente lanza RecursoNoEncontradoException")
    void findByUsuarioIdNoExiste() {
        when(usuarioRepo.existsById(99L)).thenReturn(false);
        doThrow(new RecursoNoEncontradoException("No existe")).when(lanzador).lanzarRecursoNoEncontrado(anyString());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.findByUsuarioId(99L));
    }

    

    @Test
    @DisplayName("findByTipoArchivo() retorna conversiones del tipo indicado")
    void findByTipoArchivoValido() {
        when(repo.findByTipoArchivo(TipoArchivo.VIDEO))
                .thenReturn(Optional.of(List.of(entidad)));

        List<HistorialConversionDTO> result = service.findByTipoArchivo(TipoArchivo.VIDEO);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByTipoArchivo() con null lanza OpcionNoValidaException")
    void findByTipoArchivoNulo() {
        doThrow(new OpcionNoValidaException("tipoArchivo", "null", "AUDIO, VIDEO, IMAGEN")).when(lanzador).lanzarOpcionNoValida(anyString(), anyString(), anyString());

        assertThrows(OpcionNoValidaException.class,
                () -> service.findByTipoArchivo(null));
    }

    
    @Test
    @DisplayName("findByEstado() retorna conversiones con el estado indicado")
    void findByEstadoValido() {
        when(repo.findByEstado(EstadoConversion.COMPLETADO))
                .thenReturn(Optional.of(List.of(entidad)));

        List<HistorialConversionDTO> result =
                service.findByEstado(EstadoConversion.COMPLETADO);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findByEstado() con null lanza OpcionNoValidaException")
    void findByEstadoNulo() {
        doThrow(new OpcionNoValidaException("estado", "null", "PENDIENTE, EN_PROCESO, COMPLETADO, FALLIDO")).when(lanzador).lanzarOpcionNoValida(anyString(), anyString(), anyString());

        assertThrows(OpcionNoValidaException.class,
                () -> service.findByEstado(null));
    }



    @Test
    @DisplayName("findByFormatoOrigen() retorna lista de conversiones")
    void findByFormatoOrigenValido() {
        when(repo.findByFormatoOrigen("mkv"))
                .thenReturn(Optional.of(List.of(entidad)));

        assertEquals(1, service.findByFormatoOrigen("mkv").size());
    }

    @Test
    @DisplayName("findByFormatoOrigen() con formato vacío lanza TextoVacioException")
    void findByFormatoOrigenVacio() {
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("formatoOrigen");

        assertThrows(TextoVacioException.class,
                () -> service.findByFormatoOrigen(""));
    }

    @Test
    @DisplayName("findByFormatoDestino() retorna lista de conversiones")
    void findByFormatoDestinoValido() {
        when(repo.findByFormatoDestino("mp4"))
                .thenReturn(Optional.of(List.of(entidad)));

        assertEquals(1, service.findByFormatoDestino("mp4").size());
    }

    @Test
    @DisplayName("findByFormatoDestino() con formato vacío lanza TextoVacioException")
    void findByFormatoDestinoVacio() {
        doThrow(new TextoVacioException("vacío")).when(lanzador).lanzarTextoVacio("formatoDestino");

        assertThrows(TextoVacioException.class,
                () -> service.findByFormatoDestino(""));
    }

   
    @Test
    @DisplayName("findByFechaConversionBetween() retorna conversiones en el rango")
    void findByFechaValido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fin    = LocalDateTime.of(2026, 12, 31, 23, 59);
        when(repo.findByFechaConversionBetween(inicio, fin))
                .thenReturn(Optional.of(List.of(entidad)));

        assertEquals(1, service.findByFechaConversionBetween(inicio, fin).size());
    }

    @Test
    @DisplayName("findByFechaConversionBetween() con inicio nulo lanza DatoInvalidoException")
    void findByFechaInicioNulo() {
        doThrow(new DatoInvalidoException("inicio nulo")).when(lanzador).lanzarDatoInvalido(anyString());

        assertThrows(DatoInvalidoException.class,
                () -> service.findByFechaConversionBetween(
                        null, LocalDateTime.now()));
    }

    @Test
    @DisplayName("findByFechaConversionBetween() con fin nulo lanza DatoInvalidoException")
    void findByFechaFinNulo() {
        doThrow(new DatoInvalidoException("fin nulo")).when(lanzador).lanzarDatoInvalido(anyString());

        assertThrows(DatoInvalidoException.class,
                () -> service.findByFechaConversionBetween(
                        LocalDateTime.now(), null));
    }

    @Test
    @DisplayName("findByFechaConversionBetween() con inicio posterior a fin lanza DatoInvalidoException")
    void findByFechaInicioMayorQueFin() {
        LocalDateTime inicio = LocalDateTime.of(2026, 12, 31, 0, 0);
        LocalDateTime fin    = LocalDateTime.of(2026, 1, 1, 0, 0);
        doThrow(new DatoInvalidoException("rango inválido")).when(lanzador).lanzarDatoInvalido(anyString());

        assertThrows(DatoInvalidoException.class,
                () -> service.findByFechaConversionBetween(inicio, fin));
    }
}