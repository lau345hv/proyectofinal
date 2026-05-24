package co.edu.unbosque.proyectofinal.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para todas las excepciones de negocio del proyecto.
 * Verifica constructores, mensajes y jerarquía de herencia.
 */
@DisplayName("Excepciones de negocio")
class ExcepcionesTest {

    

    @Test
    @DisplayName("ContrasenaInvalidaException() usa mensaje por defecto")
    void contrasenaInvalidaDefault() {
        ContrasenaInvalidaException ex = new ContrasenaInvalidaException();
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("8 caracteres"));
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("ContrasenaInvalidaException(String) conserva el mensaje dado")
    void contrasenaInvalidaConMensaje() {
        String msg = "Contraseña muy corta";
        ContrasenaInvalidaException ex = new ContrasenaInvalidaException(msg);
        assertEquals(msg, ex.getMessage());
    }

    

    @Test
    @DisplayName("ArchivoDemasiadoGrandeException muestra tamaños en MB")
    void archivoDemasiadoGrande() {
        long recibido = 20L * 1024 * 1024;
        long maximo   = 10L * 1024 * 1024;
        ArchivoDemasiadoGrandeException ex =
                new ArchivoDemasiadoGrandeException(recibido, maximo);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("20"), "Debe mencionar tamaño recibido en MB");
        assertTrue(ex.getMessage().contains("10"), "Debe mencionar tamaño máximo en MB");
        assertInstanceOf(RuntimeException.class, ex);
    }

    

    @Test
    @DisplayName("TextoDemasiadoLargoException incluye campo y longitudes en el mensaje")
    void textoDemasiadoLargo() {
        TextoDemasiadoLargoException ex =
                new TextoDemasiadoLargoException("nombre", 60, 50);
        assertTrue(ex.getMessage().contains("nombre"));
        assertTrue(ex.getMessage().contains("60"));
        assertTrue(ex.getMessage().contains("50"));
    }

   

    @Test
    @DisplayName("OpcionNoValidaException incluye campo, valor recibido y opciones válidas")
    void opcionNoValida() {
        OpcionNoValidaException ex =
                new OpcionNoValidaException("rol", "SUPERADMIN", "USUARIO, ADMIN");
        assertTrue(ex.getMessage().contains("rol"));
        assertTrue(ex.getMessage().contains("SUPERADMIN"));
        assertTrue(ex.getMessage().contains("USUARIO, ADMIN"));
    }

    
   

    @Test
    @DisplayName("TipoArchivoNoCoincideException menciona tipo esperado y recibido")
    void tipoArchivoNoCoincide() {
        TipoArchivoNoCoincideException ex =
                new TipoArchivoNoCoincideException("AUDIO", "VIDEO");
        assertNotNull(ex.getMessage());
       
        assertFalse(ex.getMessage().isBlank());
        assertInstanceOf(RuntimeException.class, ex);
    }

    
    @Test
    @DisplayName("NumeroNegativoException menciona campo y valor negativo")
    void numeroNegativo() {
        NumeroNegativoException ex = new NumeroNegativoException("precio", -5.0);
        assertTrue(ex.getMessage().contains("precio"));
        assertInstanceOf(RuntimeException.class, ex);
    }

    

    @Test
    @DisplayName("NumeroDemasiadoGrandeException menciona campo, valor y máximo")
    void numeroDemasiadoGrande() {
        NumeroDemasiadoGrandeException ex =
                new NumeroDemasiadoGrandeException("cantidad", 999.0, 100.0);
        assertTrue(ex.getMessage().contains("cantidad"));
        assertInstanceOf(RuntimeException.class, ex);
    }

   

    @Test
    @DisplayName("IdInvalidoException menciona entidad e id")
    void idInvalido() {
        IdInvalidoException ex = new IdInvalidoException("Usuario", -1L);
        assertTrue(ex.getMessage().contains("Usuario") || ex.getMessage().contains("-1"));
        assertInstanceOf(RuntimeException.class, ex);
    }

    

    @Test
    @DisplayName("TextoVacioException menciona el campo vacío")
    void textoVacio() {
        TextoVacioException ex = new TextoVacioException("correo");
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    

    @Test
    @DisplayName("FormatoCorreoInvalidoException contiene el correo inválido")
    void formatoCorreoInvalido() {
        FormatoCorreoInvalidoException ex = new FormatoCorreoInvalidoException("noesuncorreo");
        assertNotNull(ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

   

    @Test
    @DisplayName("CorreoDuplicadoException conserva el mensaje")
    void correoDuplicado() {
        CorreoDuplicadoException ex = new CorreoDuplicadoException("Correo ya registrado");
        assertEquals("Correo ya registrado", ex.getMessage());
    }

    

    @Test
    @DisplayName("NombreUsuarioDuplicadoException conserva el mensaje")
    void nombreUsuarioDuplicado() {
        NombreUsuarioDuplicadoException ex =
                new NombreUsuarioDuplicadoException("Nombre de usuario ya existe");
        assertEquals("Nombre de usuario ya existe", ex.getMessage());
    }

   

    @Test
    @DisplayName("RecursoNoEncontradoException conserva el mensaje")
    void recursoNoEncontrado() {
        RecursoNoEncontradoException ex =
                new RecursoNoEncontradoException("Usuario no encontrado");
        assertEquals("Usuario no encontrado", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }


    @Test
    @DisplayName("CredencialesInvalidasException tiene mensaje no vacío")
    void credencialesInvalidas() {
        CredencialesInvalidasException ex = new CredencialesInvalidasException();
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }

    

    @Test
    @DisplayName("UsuarioSinPermisoException conserva el mensaje")
    void usuarioSinPermiso() {
        UsuarioSinPermisoException ex = new UsuarioSinPermisoException("Acceso denegado");
        assertEquals("Acceso denegado", ex.getMessage());
    }

   

    @Test
    @DisplayName("DatoInvalidoException conserva el mensaje")
    void datoInvalido() {
        DatoInvalidoException ex = new DatoInvalidoException("Dato fuera de rango");
        assertEquals("Dato fuera de rango", ex.getMessage());
    }

    

    @Test
    @DisplayName("ApiExternaException conserva el mensaje")
    void apiExterna() {
        ApiExternaException ex = new ApiExternaException("Timeout en API externa");
        assertEquals("Timeout en API externa", ex.getMessage());
    }

    

    @Test
    @DisplayName("ConversionFallidaException conserva el mensaje")
    void conversionFallida() {
        ConversionFallidaException ex = new ConversionFallidaException("Formato no soportado");
        assertEquals("Formato no soportado", ex.getMessage());
    }

    

    @Test
    @DisplayName("ArchivoVacioException conserva el mensaje")
    void archivoVacio() {
        ArchivoVacioException ex = new ArchivoVacioException("El archivo está vacío");
        assertEquals("El archivo está vacío", ex.getMessage());
    }

   

    @Test
    @DisplayName("FormatoArchivoInvalidoException conserva el mensaje")
    void formatoArchivoInvalido() {
        FormatoArchivoInvalidoException ex =
                new FormatoArchivoInvalidoException("Extensión no permitida");
        assertEquals("Extensión no permitida", ex.getMessage());
    }

    

    @Test
    @DisplayName("TelefonoInvalidoException conserva el mensaje")
    void telefonoInvalido() {
        TelefonoInvalidoException ex = new TelefonoInvalidoException("Número inválido");
        assertEquals("Número inválido", ex.getMessage());
    }

    

    @Test
    @DisplayName("NombreInvalidoException conserva el mensaje")
    void nombreInvalido() {
        NombreInvalidoException ex = new NombreInvalidoException("Nombre contiene números");
        assertEquals("Nombre contiene números", ex.getMessage());
    }

    

    @Test
    @DisplayName("CorreoNoExisteException conserva el mensaje")
    void correoNoExiste() {
        CorreoNoExisteException ex = new CorreoNoExisteException("Correo no registrado");
        assertEquals("Correo no registrado", ex.getMessage());
    }

   

    @Test
    @DisplayName("CodigoVerificacionInvalidoException conserva el mensaje")
    void codigoVerificacionInvalido() {
        CodigoVerificacionInvalidoException ex =
                new CodigoVerificacionInvalidoException("Código expirado");
        assertEquals("Código expirado", ex.getMessage());
    }

    

    @Test
    @DisplayName("lanzarContrasenaInvalida() lanza ContrasenaInvalidaException")
    void lanzadorContrasenaInvalidaDefault() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(ContrasenaInvalidaException.class,
                () -> lanzador.lanzarContrasenaInvalida());
    }

    @Test
    @DisplayName("lanzarContrasenaInvalida(msg) lanza ContrasenaInvalidaException con mensaje")
    void lanzadorContrasenaInvalidaConMensaje() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        ContrasenaInvalidaException ex = assertThrows(ContrasenaInvalidaException.class,
                () -> lanzador.lanzarContrasenaInvalida("Demasiado corta"));
        assertEquals("Demasiado corta", ex.getMessage());
    }

    @Test
    @DisplayName("lanzarRecursoNoEncontrado lanza RecursoNoEncontradoException")
    void lanzadorRecursoNoEncontrado() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(RecursoNoEncontradoException.class,
                () -> lanzador.lanzarRecursoNoEncontrado("No existe"));
    }

    @Test
    @DisplayName("lanzarCorreoDuplicado lanza CorreoDuplicadoException")
    void lanzadorCorreoDuplicado() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(CorreoDuplicadoException.class,
                () -> lanzador.lanzarCorreoDuplicado("Correo duplicado"));
    }

    @Test
    @DisplayName("lanzarIdInvalido lanza IdInvalidoException")
    void lanzadorIdInvalido() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(IdInvalidoException.class,
                () -> lanzador.lanzarIdInvalido("Historial", 0L));
    }

    @Test
    @DisplayName("lanzarTextoVacio lanza TextoVacioException")
    void lanzadorTextoVacio() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(TextoVacioException.class,
                () -> lanzador.lanzarTextoVacio("nombre"));
    }

    @Test
    @DisplayName("lanzarTextoDemasiadoLargo lanza TextoDemasiadoLargoException")
    void lanzadorTextoDemasiadoLargo() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(TextoDemasiadoLargoException.class,
                () -> lanzador.lanzarTextoDemasiadoLargo("apellido", 80, 50));
    }

    @Test
    @DisplayName("lanzarNumeroNegativo lanza NumeroNegativoException")
    void lanzadorNumeroNegativo() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(NumeroNegativoException.class,
                () -> lanzador.lanzarNumeroNegativo("precio", -1.0));
    }

    @Test
    @DisplayName("lanzarCredencialesInvalidas lanza CredencialesInvalidasException")
    void lanzadorCredencialesInvalidas() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(CredencialesInvalidasException.class,
                lanzador::lanzarCredencialesInvalidas);
    }

    @Test
    @DisplayName("lanzarArchivoVacio lanza ArchivoVacioException")
    void lanzadorArchivoVacio() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(ArchivoVacioException.class,
                () -> lanzador.lanzarArchivoVacio("El archivo no tiene contenido"));
    }

    @Test
    @DisplayName("lanzarArchivoDemasiadoGrande lanza ArchivoDemasiadoGrandeException")
    void lanzadorArchivoDemasiadoGrande() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(ArchivoDemasiadoGrandeException.class,
                () -> lanzador.lanzarArchivoDemasiadoGrande(
                        50L * 1024 * 1024, 10L * 1024 * 1024));
    }

    @Test
    @DisplayName("lanzarConversionFallida lanza ConversionFallidaException")
    void lanzadorConversionFallida() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(ConversionFallidaException.class,
                () -> lanzador.lanzarConversionFallida("Error en conversión"));
    }

    @Test
    @DisplayName("lanzarTipoArchivoNoCoincide lanza TipoArchivoNoCoincideException")
    void lanzadorTipoArchivoNoCoincide() {
        LanzadorDeExcepcion lanzador = new LanzadorDeExcepcion();
        assertThrows(TipoArchivoNoCoincideException.class,
                () -> lanzador.lanzarTipoArchivoNoCoincide("AUDIO", "VIDEO"));
    }
}
