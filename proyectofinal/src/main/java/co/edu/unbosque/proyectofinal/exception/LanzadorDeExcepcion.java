package co.edu.unbosque.proyectofinal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * Clase central que cumple dos roles a la vez en el sistema:
 * <ol>
 *   <li><b>Lanzador de excepciones de negocio:</b> expone métodos {@code lanzarXxx(...)}
 *       que los servicios pueden invocar para lanzar excepciones tipadas con
 *       mensajes descriptivos.</li>
 *   <li><b>Manejador global de excepciones:</b> intercepta las excepciones lanzadas
 *       en los controladores y retorna respuestas HTTP con el código y mensaje
 *       adecuados al tipo de error.</li>
 * </ol>
 * <p>
 * Está anotada con {@link RestControllerAdvice} para que los {@code @ExceptionHandler}
 * se apliquen globalmente a todos los controladores REST de la aplicación.
 * </p>
 *
 * @author Equipo de desarrollo
 * @version 2.0
 */
@RestControllerAdvice
public class LanzadorDeExcepcion {

	// =====================================================================
	// Métodos para LANZAR excepciones (uso interno desde los servicios)
	// =====================================================================

	public void lanzarRecursoNoEncontrado(String mensaje) throws RecursoNoEncontradoException {
		throw new RecursoNoEncontradoException(mensaje);
	}

	public void lanzarDatoInvalido(String mensaje) throws DatoInvalidoException {
		throw new DatoInvalidoException(mensaje);
	}

	public void lanzarCorreoDuplicado(String mensaje) throws CorreoDuplicadoException {
		throw new CorreoDuplicadoException(mensaje);
	}

	public void lanzarNombreUsuarioDuplicado(String mensaje) throws NombreUsuarioDuplicadoException {
		throw new NombreUsuarioDuplicadoException(mensaje);
	}

	public void lanzarFormatoCorreoInvalido(String correo) throws FormatoCorreoInvalidoException {
		throw new FormatoCorreoInvalidoException(correo);
	}

	public void lanzarIdInvalido(String entidad, Long id) throws IdInvalidoException {
		throw new IdInvalidoException(entidad, id);
	}

	public void lanzarTextoVacio(String campo) throws TextoVacioException {
		throw new TextoVacioException(campo);
	}

	public void lanzarTextoDemasiadoLargo(String campo, int longitudRecibida, int longitudMaxima)
			throws TextoDemasiadoLargoException {
		throw new TextoDemasiadoLargoException(campo, longitudRecibida, longitudMaxima);
	}

	public void lanzarOpcionNoValida(String campo, String valorRecibido, String opcionesValidas)
			throws OpcionNoValidaException {
		throw new OpcionNoValidaException(campo, valorRecibido, opcionesValidas);
	}

	public void lanzarNumeroNegativo(String campo, double valor) throws NumeroNegativoException {
		throw new NumeroNegativoException(campo, valor);
	}

	public void lanzarNumeroDemasiadoGrande(String campo, double valor, double maximo)
			throws NumeroDemasiadoGrandeException {
		throw new NumeroDemasiadoGrandeException(campo, valor, maximo);
	}

	public void lanzarArchivoVacio(String mensaje) throws ArchivoVacioException {
		throw new ArchivoVacioException(mensaje);
	}

	public void lanzarArchivoDemasiadoGrande(long tamanoBytes, long maximoBytes)
			throws ArchivoDemasiadoGrandeException {
		throw new ArchivoDemasiadoGrandeException(tamanoBytes, maximoBytes);
	}

	public void lanzarFormatoArchivoInvalido(String mensaje) throws FormatoArchivoInvalidoException {
		throw new FormatoArchivoInvalidoException(mensaje);
	}

	public void lanzarConversionFallida(String mensaje) throws ConversionFallidaException {
		throw new ConversionFallidaException(mensaje);
	}

	public void lanzarApiExterna(String mensaje) throws ApiExternaException {
		throw new ApiExternaException(mensaje);
	}

	public void lanzarContrasenaInvalida(String mensaje) throws ContrasenaInvalidaException {
		throw new ContrasenaInvalidaException(mensaje);
	}

	public void lanzarContrasenaInvalida() throws ContrasenaInvalidaException {
		throw new ContrasenaInvalidaException();
	}

	public void lanzarCredencialesInvalidas() throws CredencialesInvalidasException {
		throw new CredencialesInvalidasException();
	}

	public void lanzarUsuarioSinPermiso(String mensaje) throws UsuarioSinPermisoException {
		throw new UsuarioSinPermisoException(mensaje);
	}

	public void lanzarTelefonoInvalido(String mensaje) throws TelefonoInvalidoException {
		throw new TelefonoInvalidoException(mensaje);
	}

	public void lanzarTipoArchivoNoCoincide(String tipoEsperado, String tipoRecibido)
			throws TipoArchivoNoCoincideException {
		throw new TipoArchivoNoCoincideException(tipoEsperado, tipoRecibido);
	}

	public void lanzarNombreInvalido(String mensaje) throws NombreInvalidoException {
		throw new NombreInvalidoException(mensaje);
	}

	public void lanzarCorreoNoExiste(String mensaje) throws CorreoNoExisteException {
		throw new CorreoNoExisteException(mensaje);
	}

	public void lanzarCodigoVerificacionInvalido(String mensaje)
			throws CodigoVerificacionInvalidoException {
		throw new CodigoVerificacionInvalidoException(mensaje);
	}

	// =====================================================================
	// Manejadores de excepciones de NEGOCIO
	// =====================================================================

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<String> recursoNoEncontrado(RecursoNoEncontradoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(DatoInvalidoException.class)
	public ResponseEntity<String> datoInvalido(DatoInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CorreoDuplicadoException.class)
	public ResponseEntity<String> correoDuplicado(CorreoDuplicadoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(NombreUsuarioDuplicadoException.class)
	public ResponseEntity<String> nombreUsuarioDuplicado(NombreUsuarioDuplicadoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(FormatoCorreoInvalidoException.class)
	public ResponseEntity<String> formatoCorreoInvalido(FormatoCorreoInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IdInvalidoException.class)
	public ResponseEntity<String> idInvalido(IdInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(TextoVacioException.class)
	public ResponseEntity<String> textoVacio(TextoVacioException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(TextoDemasiadoLargoException.class)
	public ResponseEntity<String> textoDemasiadoLargo(TextoDemasiadoLargoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(OpcionNoValidaException.class)
	public ResponseEntity<String> opcionNoValida(OpcionNoValidaException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NumeroNegativoException.class)
	public ResponseEntity<String> numeroNegativo(NumeroNegativoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NumeroDemasiadoGrandeException.class)
	public ResponseEntity<String> numeroDemasiadoGrande(NumeroDemasiadoGrandeException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ArchivoVacioException.class)
	public ResponseEntity<String> archivoVacio(ArchivoVacioException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ArchivoDemasiadoGrandeException.class)
	public ResponseEntity<String> archivoDemasiadoGrande(ArchivoDemasiadoGrandeException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@ExceptionHandler(FormatoArchivoInvalidoException.class)
	public ResponseEntity<String> formatoArchivoInvalido(FormatoArchivoInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConversionFallidaException.class)
	public ResponseEntity<String> conversionFallida(ConversionFallidaException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(ApiExternaException.class)
	public ResponseEntity<String> apiExterna(ApiExternaException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(ContrasenaInvalidaException.class)
	public ResponseEntity<String> contrasenaInvalida(ContrasenaInvalidaException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<String> credencialesInvalidas(CredencialesInvalidasException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UsuarioSinPermisoException.class)
	public ResponseEntity<String> usuarioSinPermiso(UsuarioSinPermisoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(TelefonoInvalidoException.class)
	public ResponseEntity<String> telefonoInvalido(TelefonoInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(TipoArchivoNoCoincideException.class)
	public ResponseEntity<String> tipoArchivoNoCoincide(TipoArchivoNoCoincideException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NombreInvalidoException.class)
	public ResponseEntity<String> nombreInvalido(NombreInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CorreoNoExisteException.class)
	public ResponseEntity<String> correoNoExiste(CorreoNoExisteException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(CodigoVerificacionInvalidoException.class)
	public ResponseEntity<String> codigoVerificacionInvalido(CodigoVerificacionInvalidoException e) {
		return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
	}

	// =====================================================================
	// Manejadores de excepciones de SPRING (multipart, request body, params)
	// =====================================================================

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<String> archivoSuperaLimite(MaxUploadSizeExceededException e) {
		return new ResponseEntity<>(
				"El archivo supera el tamaño máximo permitido por el servidor. "
						+ "Por favor seleccione un archivo más pequeño.",
				HttpStatus.PAYLOAD_TOO_LARGE);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<String> archivoFaltante(MissingServletRequestPartException e) {
		return new ResponseEntity<>(
				"Falta enviar el archivo en la petición. Asegúrese de adjuntar el "
						+ "campo '" + e.getRequestPartName() + "' como multipart/form-data.",
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<String> parametroFaltante(MissingServletRequestParameterException e) {
		return new ResponseEntity<>(
				"Falta el parámetro obligatorio '" + e.getParameterName() + "' en la petición.",
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<String> tipoParametroInvalido(MethodArgumentTypeMismatchException e) {
		String tipoEsperado = e.getRequiredType() != null
				? e.getRequiredType().getSimpleName()
				: "tipo desconocido";
		return new ResponseEntity<>(
				"El parámetro '" + e.getName() + "' tiene un valor inválido. "
						+ "Se esperaba un valor de tipo " + tipoEsperado + ".",
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> cuerpoNoLegible(HttpMessageNotReadableException e) {
		return new ResponseEntity<>(
				"El cuerpo de la petición no se pudo leer. Verifique que el JSON "
						+ "tenga el formato correcto y que todos los campos sean del tipo esperado.",
				HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> validacionFallida(MethodArgumentNotValidException e) {
		StringBuilder mensaje = new StringBuilder("Errores de validación: ");
		e.getBindingResult().getFieldErrors().forEach(error ->
				mensaje.append("[")
						.append(error.getField())
						.append(": ")
						.append(error.getDefaultMessage())
						.append("] "));
		return new ResponseEntity<>(mensaje.toString().trim(), HttpStatus.BAD_REQUEST);
	}

	// =====================================================================
	// Manejadores de excepciones de SPRING SECURITY
	// =====================================================================

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<String> credencialesIncorrectas(BadCredentialsException e) {
		return new ResponseEntity<>(
				"Nombre de usuario o contraseña incorrectos.",
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<String> autenticacionFallida(AuthenticationException e) {
		return new ResponseEntity<>(
				"No autenticado. Por favor inicie sesión.",
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<String> accesoDenegado(AccessDeniedException e) {
		return new ResponseEntity<>(
				"No tiene permisos suficientes para realizar esta operación.",
				HttpStatus.FORBIDDEN);
	}

	// =====================================================================
	// Red de seguridad: cualquier otra excepción no contemplada
	// =====================================================================

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<String> errorRuntime(RuntimeException e) {
		return new ResponseEntity<>(
				"Error inesperado en el servidor: " + e.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> errorGenerico(Exception e) {
		return new ResponseEntity<>(
				"Error inesperado en el servidor: " + e.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}