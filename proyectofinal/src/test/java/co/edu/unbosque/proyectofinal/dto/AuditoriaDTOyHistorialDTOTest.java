package co.edu.unbosque.proyectofinal.dto;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Pruebas unitarias para {@link AuditoriaDTO} y {@link HistorialConversionDTO}.
 */
@DisplayName("DTOs Auditoria e HistorialConversion")
class AuditoriaDTOyHistorialDTOTest {

    

    private AuditoriaDTO auditoriaDTO;
    private LocalDateTime fecha;

    @BeforeEach
    void setUp() {
        fecha = LocalDateTime.of(2026, 5, 23, 14, 0);
        auditoriaDTO = new AuditoriaDTO(1L, "admin.user", RolUsuario.ADMIN,
                TipoAccion.DELETE, "Eliminó usuario id=5",
                "/usuario/eliminar/5", fecha);
    }

    @Test
    @DisplayName("AuditoriaDTO constructor vacío no lanza excepciones")
    void auditoriaDTOConstructorVacio() {
       
        assertDoesNotThrow(() -> new AuditoriaDTO());
    }

    @Test
    @DisplayName("AuditoriaDTO constructor parametrizado asigna campos")
    void auditoriaDTOConstructorParametrizado() {
        assertEquals(1L, auditoriaDTO.getUsuarioId());
        assertEquals("admin.user", auditoriaDTO.getNombreUsuario());
        assertEquals(RolUsuario.ADMIN, auditoriaDTO.getRolUsuario());
        assertEquals(TipoAccion.DELETE, auditoriaDTO.getTipoAccion());
        assertEquals("Eliminó usuario id=5", auditoriaDTO.getDescripcion());
        assertEquals("/usuario/eliminar/5", auditoriaDTO.getEndpoint());
        assertEquals(fecha, auditoriaDTO.getFecha());
    }

    @Test
    @DisplayName("AuditoriaDTO setters funcionan correctamente")
    void auditoriaDTOSetters() {
        auditoriaDTO.setId(99L);
        auditoriaDTO.setTipoAccion(TipoAccion.CONVERSION);
        auditoriaDTO.setDescripcion("Nueva descripción");
        assertEquals(99L, auditoriaDTO.getId());
        assertEquals(TipoAccion.CONVERSION, auditoriaDTO.getTipoAccion());
        assertEquals("Nueva descripción", auditoriaDTO.getDescripcion());
    }

    @Test
    @DisplayName("AuditoriaDTO equals reflexivo")
    void auditoriaDTOEqualsReflexivo() {
        assertEquals(auditoriaDTO, auditoriaDTO);
    }

    @Test
    @DisplayName("AuditoriaDTO equals con mismo id")
    void auditoriaDTOEqualsIgualId() {
        auditoriaDTO.setId(1L);
        AuditoriaDTO otro = new AuditoriaDTO();
        otro.setId(1L);
        assertEquals(auditoriaDTO, otro);
    }

    @Test
    @DisplayName("AuditoriaDTO equals con distinto id")
    void auditoriaDTOEqualsDistintoId() {
        auditoriaDTO.setId(1L);
        AuditoriaDTO otro = new AuditoriaDTO();
        otro.setId(2L);
        assertNotEquals(auditoriaDTO, otro);
    }

    @Test
    @DisplayName("AuditoriaDTO hashCode consistente para mismo id")
    void auditoriaDTOHashCode() {
        auditoriaDTO.setId(4L);
        AuditoriaDTO otro = new AuditoriaDTO();
        otro.setId(4L);
        assertEquals(auditoriaDTO.hashCode(), otro.hashCode());
    }

    @Test
    @DisplayName("AuditoriaDTO toString no es null ni vacío")
    void auditoriaDTOToString() {
        assertNotNull(auditoriaDTO.toString());
        assertFalse(auditoriaDTO.toString().isBlank());
    }

    

    @Test
    @DisplayName("HistorialConversionDTO constructor vacío no lanza excepciones")
    void historialDTOConstructorVacio() {
        
        assertDoesNotThrow(() -> new HistorialConversionDTO());
    }

    @Test
    @DisplayName("HistorialConversionDTO constructor parametrizado asigna campos")
    void historialDTOConstructorParametrizado() {
        LocalDateTime fechaConv = LocalDateTime.of(2026, 4, 10, 9, 0);
        HistorialConversionDTO dto = new HistorialConversionDTO(
                fechaConv, TipoArchivo.IMAGEN, "png", "jpg",
                "foto.png", "foto.jpg",
                "/up/foto.png", "/conv/foto.jpg",
                EstadoConversion.COMPLETADO, 2L
        );
        assertEquals(fechaConv, dto.getFechaConversion());
        assertEquals(TipoArchivo.IMAGEN, dto.getTipoArchivo());
        assertEquals("png", dto.getFormatoOrigen());
        assertEquals("jpg", dto.getFormatoDestino());
        assertEquals("foto.png", dto.getNombreArchivoOriginal());
        assertEquals("foto.jpg", dto.getNombreArchivoConvertido());
        assertEquals("/up/foto.png", dto.getRutaArchivoOriginal());
        assertEquals("/conv/foto.jpg", dto.getRutaArchivoConvertido());
        assertEquals(EstadoConversion.COMPLETADO, dto.getEstado());
        assertEquals(2L, dto.getUsuarioId());
    }

    @Test
    @DisplayName("HistorialConversionDTO setters funcionan correctamente")
    void historialDTOSetters() {
        HistorialConversionDTO dto = new HistorialConversionDTO();
        dto.setId(5L);
        dto.setTipoArchivo(TipoArchivo.AUDIO);
        dto.setEstado(EstadoConversion.FALLIDO);
        dto.setFormatoOrigen("mp3");
        dto.setFormatoDestino("wav");
        dto.setUsuarioId(3L);

        assertEquals(5L, dto.getId());
        assertEquals(TipoArchivo.AUDIO, dto.getTipoArchivo());
        assertEquals(EstadoConversion.FALLIDO, dto.getEstado());
        assertEquals("mp3", dto.getFormatoOrigen());
        assertEquals("wav", dto.getFormatoDestino());
        assertEquals(3L, dto.getUsuarioId());
    }

    @Test
    @DisplayName("HistorialConversionDTO equals con mismo id")
    void historialDTOEqualsIgualId() {
        HistorialConversionDTO d1 = new HistorialConversionDTO();
        HistorialConversionDTO d2 = new HistorialConversionDTO();
        d1.setId(10L);
        d2.setId(10L);
        assertEquals(d1, d2);
    }

    @Test
    @DisplayName("HistorialConversionDTO equals con distinto id")
    void historialDTOEqualsDistintoId() {
        HistorialConversionDTO d1 = new HistorialConversionDTO();
        HistorialConversionDTO d2 = new HistorialConversionDTO();
        d1.setId(1L);
        d2.setId(2L);
        assertNotEquals(d1, d2);
    }

    @Test
    @DisplayName("HistorialConversionDTO hashCode consistente")
    void historialDTOHashCode() {
        HistorialConversionDTO d1 = new HistorialConversionDTO();
        HistorialConversionDTO d2 = new HistorialConversionDTO();
        d1.setId(8L);
        d2.setId(8L);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    @DisplayName("HistorialConversionDTO toString no es null ni vacío")
    void historialDTOToString() {
        HistorialConversionDTO dto = new HistorialConversionDTO();
        dto.setId(1L);
        assertNotNull(dto.toString());
        assertFalse(dto.toString().isBlank());
    }
}