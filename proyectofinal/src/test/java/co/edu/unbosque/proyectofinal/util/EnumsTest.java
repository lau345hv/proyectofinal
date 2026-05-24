package co.edu.unbosque.proyectofinal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectofinal.util.enums.EstadoConversion;
import co.edu.unbosque.proyectofinal.util.enums.RolUsuario;
import co.edu.unbosque.proyectofinal.util.enums.TipoAccion;
import co.edu.unbosque.proyectofinal.util.enums.TipoArchivo;

/**
 * Pruebas unitarias para los enumerados del sistema.
 */
@DisplayName("Enumerados del sistema")
class EnumsTest {

    

    @Test
    @DisplayName("RolUsuario tiene exactamente 2 valores")
    void rolUsuarioTieneDoValores() {
        assertEquals(2, RolUsuario.values().length);
    }

    @Test
    @DisplayName("RolUsuario contiene USUARIO y ADMIN")
    void rolUsuarioContieneValoresEsperados() {
        assertNotNull(RolUsuario.valueOf("USUARIO"));
        assertNotNull(RolUsuario.valueOf("ADMIN"));
    }

    @Test
    @DisplayName("RolUsuario.valueOf con valor inválido lanza IllegalArgumentException")
    void rolUsuarioValorInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> RolUsuario.valueOf("SUPERADMIN"));
    }

    

    @Test
    @DisplayName("TipoArchivo tiene exactamente 3 valores")
    void tipoArchivoTieneTresValores() {
        assertEquals(3, TipoArchivo.values().length);
    }

    @Test
    @DisplayName("TipoArchivo contiene AUDIO, VIDEO, IMAGEN")
    void tipoArchivoContieneValoresEsperados() {
        assertNotNull(TipoArchivo.valueOf("AUDIO"));
        assertNotNull(TipoArchivo.valueOf("VIDEO"));
        assertNotNull(TipoArchivo.valueOf("IMAGEN"));
    }

    @Test
    @DisplayName("TipoArchivo.valueOf con valor inválido lanza IllegalArgumentException")
    void tipoArchivoValorInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> TipoArchivo.valueOf("DOCUMENTO"));
    }

    
    @Test
    @DisplayName("EstadoConversion tiene exactamente 4 valores")
    void estadoConversionTieneCuatroValores() {
        assertEquals(4, EstadoConversion.values().length);
    }

    @Test
    @DisplayName("EstadoConversion contiene PENDIENTE, EN_PROCESO, COMPLETADO, FALLIDO")
    void estadoConversionContieneValoresEsperados() {
        assertNotNull(EstadoConversion.valueOf("PENDIENTE"));
        assertNotNull(EstadoConversion.valueOf("EN_PROCESO"));
        assertNotNull(EstadoConversion.valueOf("COMPLETADO"));
        assertNotNull(EstadoConversion.valueOf("FALLIDO"));
    }

    @Test
    @DisplayName("EstadoConversion ordinal PENDIENTE es 0")
    void estadoConversionOrdinalPendiente() {
        assertEquals(0, EstadoConversion.PENDIENTE.ordinal());
    }

    @Test
    @DisplayName("EstadoConversion ordinal FALLIDO es 3")
    void estadoConversionOrdinalFallido() {
        assertEquals(3, EstadoConversion.FALLIDO.ordinal());
    }

    

    @Test
    @DisplayName("TipoAccion tiene exactamente 7 valores")
    void tipoAccionTieneSieteValores() {
        assertEquals(7, TipoAccion.values().length);
    }

    @Test
    @DisplayName("TipoAccion contiene CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT, CONVERSION")
    void tipoAccionContieneValoresEsperados() {
        assertNotNull(TipoAccion.valueOf("CREATE"));
        assertNotNull(TipoAccion.valueOf("READ"));
        assertNotNull(TipoAccion.valueOf("UPDATE"));
        assertNotNull(TipoAccion.valueOf("DELETE"));
        assertNotNull(TipoAccion.valueOf("LOGIN"));
        assertNotNull(TipoAccion.valueOf("LOGOUT"));
        assertNotNull(TipoAccion.valueOf("CONVERSION"));
    }

    @Test
    @DisplayName("TipoAccion.name() devuelve el nombre correcto")
    void tipoAccionName() {
        assertEquals("LOGIN", TipoAccion.LOGIN.name());
        assertEquals("CONVERSION", TipoAccion.CONVERSION.name());
    }

    @Test
    @DisplayName("Comparación entre valores de enum funciona correctamente")
    void tipoAccionComparacion() {
        assertEquals(TipoAccion.CREATE, TipoAccion.CREATE);
        assertNotEquals(TipoAccion.CREATE, TipoAccion.DELETE);
    }
}
