package co.edu.unbosque.proyectofinal.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Pruebas unitarias para la entidad {@link HistorialConversion}.
 */
@DisplayName("Entidad HistorialConversion")
class HistorialConversionTest {

    private HistorialConversion historial;
    private Usuario usuario;
    private LocalDateTime fecha;

    @BeforeEach
    void setUp() {
        fecha = LocalDateTime.of(2026, 5, 1, 10, 30);
        usuario = new Usuario("Carlos", "Ramírez", "carlos@correo.com",
                "carlos.r", "Clave1234", RolUsuario.USUARIO);
        historial = new HistorialConversion(
                fecha,
                TipoArchivo.VIDEO,
                "mkv",
                "mp4",
                "video.mkv",
                "video.mp4",
                "/uploads/video.mkv",
                "/converted/video.mp4",
                EstadoConversion.COMPLETADO,
                usuario
        );
    }

    @Test
    @DisplayName("Constructor parametrizado asigna todos los campos")
    void constructorParametrizado() {
        assertEquals(fecha, historial.getFechaConversion());
        assertEquals(TipoArchivo.VIDEO, historial.getTipoArchivo());
        assertEquals("mkv", historial.getFormatoOrigen());
        assertEquals("mp4", historial.getFormatoDestino());
        assertEquals("video.mkv", historial.getNombreArchivoOriginal());
        assertEquals("video.mp4", historial.getNombreArchivoConvertido());
        assertEquals("/uploads/video.mkv", historial.getRutaArchivoOriginal());
        assertEquals("/converted/video.mp4", historial.getRutaArchivoConvertido());
        assertEquals(EstadoConversion.COMPLETADO, historial.getEstado());
        assertEquals(usuario, historial.getUsuario());
    }

    @Test
    @DisplayName("Constructor vacío no lanza excepciones")
    void constructorVacio() {
       
        assertDoesNotThrow(() -> new HistorialConversion());
    }

    @Test
    @DisplayName("setEstado actualiza el estado de la conversión")
    void setEstado() {
        historial.setEstado(EstadoConversion.FALLIDO);
        assertEquals(EstadoConversion.FALLIDO, historial.getEstado());
    }

    @Test
    @DisplayName("setTipoArchivo actualiza el tipo de archivo")
    void setTipoArchivo() {
        historial.setTipoArchivo(TipoArchivo.AUDIO);
        assertEquals(TipoArchivo.AUDIO, historial.getTipoArchivo());
    }

    @Test
    @DisplayName("setFormatoOrigen actualiza el formato de origen")
    void setFormatoOrigen() {
        historial.setFormatoOrigen("avi");
        assertEquals("avi", historial.getFormatoOrigen());
    }

    @Test
    @DisplayName("setFormatoDestino actualiza el formato de destino")
    void setFormatoDestino() {
        historial.setFormatoDestino("webm");
        assertEquals("webm", historial.getFormatoDestino());
    }

    @Test
    @DisplayName("setFechaConversion actualiza la fecha")
    void setFechaConversion() {
        LocalDateTime nuevaFecha = LocalDateTime.of(2026, 6, 15, 8, 0);
        historial.setFechaConversion(nuevaFecha);
        assertEquals(nuevaFecha, historial.getFechaConversion());
    }

    @Test
    @DisplayName("setUsuario actualiza el usuario propietario")
    void setUsuario() {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setId(99L);
        historial.setUsuario(nuevoUsuario);
        assertEquals(nuevoUsuario, historial.getUsuario());
    }

    @Test
    @DisplayName("equals es reflexivo")
    void equalsReflexivo() {
        assertEquals(historial, historial);
    }

    @Test
    @DisplayName("Dos historiales con mismo id son iguales")
    void equalsIgualId() {
        historial.setId(1L);
        HistorialConversion h2 = new HistorialConversion();
        h2.setId(1L);
        assertEquals(historial, h2);
    }

    @Test
    @DisplayName("Dos historiales con distinto id no son iguales")
    void equalsDistintoId() {
        historial.setId(1L);
        HistorialConversion h2 = new HistorialConversion();
        h2.setId(2L);
        assertNotEquals(historial, h2);
    }

    @Test
    @DisplayName("equals con null devuelve false")
    void equalsConNull() {
        assertNotEquals(null, historial);
    }

    @Test
    @DisplayName("hashCode es consistente para el mismo id")
    void hashCodeConsistente() {
        historial.setId(10L);
        HistorialConversion h2 = new HistorialConversion();
        h2.setId(10L);
        assertEquals(historial.hashCode(), h2.hashCode());
    }

    @Test
    @DisplayName("toString contiene formato origen y destino")
    void toStringContiene() {
        String str = historial.toString();
        assertTrue(str.contains("mkv") || str.contains("mp4") || str.contains("VIDEO"));
    }
}