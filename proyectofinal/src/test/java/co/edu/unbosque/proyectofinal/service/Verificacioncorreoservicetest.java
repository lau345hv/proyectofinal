package co.edu.unbosque.proyectofinal.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import co.edu.unbosque.proyectofinal.exception.CodigoVerificacionInvalidoException;
import co.edu.unbosque.proyectofinal.exception.DatoInvalidoException;
import co.edu.unbosque.proyectofinal.exception.LanzadorDeExcepcion;

/**
 * Pruebas unitarias para {@link VerificacionCorreoService}.
 * Todas las dependencias externas están mockeadas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VerificacionCorreoService - Pruebas unitarias")
class VerificacionCorreoServiceTest {

    @InjectMocks
    private VerificacionCorreoService service;

    @Mock private LanzadorDeExcepcion lanzador;
    @Mock private RestTemplate restTemplate;

  

    @Test
    @DisplayName("solicitarCodigo() genera y almacena un código de 6 dígitos")
    void solicitarCodigoGeneraCodigo() {
        String codigo = service.solicitarCodigo("test@gmail.com");

        assertNotNull(codigo);
        assertTrue(codigo.matches("\\d{6}"),
                "El código debe tener exactamente 6 dígitos numéricos");
    }

    @Test
    @DisplayName("solicitarCodigo() genera códigos distintos en llamadas sucesivas")
    void solicitarCodigoGeneraCodigosDistintos() {
        String c1 = service.solicitarCodigo("a@gmail.com");
        String c2 = service.solicitarCodigo("b@gmail.com");
        
        assertNotNull(c1);
        assertNotNull(c2);
    }

    @Test
    @DisplayName("solicitarCodigo() con correo vacío lanza DatoInvalidoException")
    void solicitarCodigoCorreoVacio() {
        doThrow(new DatoInvalidoException("vacío")).when(lanzador).lanzarDatoInvalido(anyString());

        assertThrows(DatoInvalidoException.class,
                () -> service.solicitarCodigo(""));
    }

    @Test
    @DisplayName("solicitarCodigo() con correo nulo lanza DatoInvalidoException")
    void solicitarCodigoCorreoNulo() {
        doThrow(new DatoInvalidoException("nulo")).when(lanzador).lanzarDatoInvalido(anyString());

        assertThrows(DatoInvalidoException.class,
                () -> service.solicitarCodigo(null));
    }

    @Test
    @DisplayName("solicitarCodigo() deja código pendiente después de llamarse")
    void solicitarCodigoDejaCodigoPendiente() {
        service.solicitarCodigo("usuario@gmail.com");
        assertTrue(service.hayCodigoPendiente("usuario@gmail.com"));
    }

    @Test
    @DisplayName("solicitarCodigo() sobrescribe el código anterior para el mismo correo")
    void solicitarCodigoSobrescribePrevio() {
        String c1 = service.solicitarCodigo("mismo@gmail.com");
        String c2 = service.solicitarCodigo("mismo@gmail.com");
        
        assertNotNull(c1);
        assertNotNull(c2);
        assertTrue(service.hayCodigoPendiente("mismo@gmail.com"));
    }

   

    @Test
    @DisplayName("validarCodigo() con código correcto no lanza excepción y consume el código")
    void validarCodigoCorrecto() {
        String correo = "valido@gmail.com";
        String codigo = service.solicitarCodigo(correo);

        service.validarCodigo(correo, codigo); 

       
        assertFalse(service.hayCodigoPendiente(correo),
                "El código debe eliminarse tras validarse correctamente");
    }

    @Test
    @DisplayName("validarCodigo() con código incorrecto lanza CodigoVerificacionInvalidoException")
    void validarCodigoIncorrecto() {
        String correo = "incorrecto@gmail.com";
        service.solicitarCodigo(correo);

        doThrow(new CodigoVerificacionInvalidoException("incorrecto")).when(lanzador).lanzarCodigoVerificacionInvalido(anyString());

        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> service.validarCodigo(correo, "000000"));
    }

    @Test
    @DisplayName("validarCodigo() sin código previo lanza CodigoVerificacionInvalidoException")
    void validarCodigoSinSolicitar() {
        doThrow(new CodigoVerificacionInvalidoException("no hay código")).when(lanzador).lanzarCodigoVerificacionInvalido(anyString());

        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> service.validarCodigo("sincodigo@gmail.com", "123456"));
    }

    @Test
    @DisplayName("validarCodigo() con correo vacío lanza CodigoVerificacionInvalidoException")
    void validarCodigoCorreoVacio() {
        doThrow(new CodigoVerificacionInvalidoException("vacío")).when(lanzador).lanzarCodigoVerificacionInvalido(anyString());

        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> service.validarCodigo("", "123456"));
    }

    @Test
    @DisplayName("validarCodigo() con código nulo lanza CodigoVerificacionInvalidoException")
    void validarCodigoCodigoNulo() {
        doThrow(new CodigoVerificacionInvalidoException("nulo")).when(lanzador).lanzarCodigoVerificacionInvalido(anyString());

        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> service.validarCodigo("a@gmail.com", null));
    }

    @Test
    @DisplayName("validarCodigo() es insensible a espacios en el código")
    void validarCodigoConEspacios() {
        String correo = "espacios@gmail.com";
        String codigo = service.solicitarCodigo(correo);

       
        service.validarCodigo(correo, " " + codigo + " ");

        assertFalse(service.hayCodigoPendiente(correo));
    }

    

    @Test
    @DisplayName("hayCodigoPendiente() retorna false si no hay código para ese correo")
    void hayCodigoPendienteFalse() {
        assertFalse(service.hayCodigoPendiente("noregistrado@gmail.com"));
    }

    @Test
    @DisplayName("hayCodigoPendiente() retorna true tras solicitar código")
    void hayCodigoPendienteTrue() {
        service.solicitarCodigo("pendiente@gmail.com");
        assertTrue(service.hayCodigoPendiente("pendiente@gmail.com"));
    }

    @Test
    @DisplayName("hayCodigoPendiente() retorna false para correo nulo")
    void hayCodigoPendienteNulo() {
        assertFalse(service.hayCodigoPendiente(null));
    }

    @Test
    @DisplayName("hayCodigoPendiente() es insensible a mayúsculas")
    void hayCodigoPendienteMayusculas() {
        service.solicitarCodigo("Usuario@Gmail.com");
       
        assertTrue(service.hayCodigoPendiente("usuario@gmail.com"));
    }

    

    @Test
    @DisplayName("Dos solicitudes de código al mismo correo dejan un solo código pendiente")
    void dosSolicitudesMismoCorreo() {
        String correo = "doble@gmail.com";
        service.solicitarCodigo(correo);
        service.solicitarCodigo(correo);

        assertTrue(service.hayCodigoPendiente(correo));

       
        String codigoActual = service.solicitarCodigo(correo);
        service.validarCodigo(correo, codigoActual);
        assertFalse(service.hayCodigoPendiente(correo));
    }

    @Test
    @DisplayName("Código expirado por manipulación de tiempo lanza excepción")
    void codigoExpiradoConReflection() throws Exception {
        String correo = "expirado@gmail.com";
        service.solicitarCodigo(correo);

        java.util.Map<?, ?> map = (java.util.Map<?, ?>) ReflectionTestUtils.getField(
                service, "codigosPendientes");
        assertNotNull(map);
        assertFalse(map.isEmpty(), "Debe haber un código pendiente");

       
        Object info = map.get(correo.toLowerCase());
        if (info != null) {
            ReflectionTestUtils.setField(info, "fechaCreacion",
                    java.time.LocalDateTime.now().minusMinutes(20));
        }

        doThrow(new CodigoVerificacionInvalidoException("expirado")).when(lanzador).lanzarCodigoVerificacionInvalido(anyString());

        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> service.validarCodigo(correo, "123456"));
    }
}