package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando ocurre un error al comunicarse con la API
 * externa de conversión (CloudConvert): timeouts, credenciales inválidas,
 * cuotas excedidas o respuestas con código HTTP de error.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class ApiExternaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ApiExternaException(String mensaje) {
		super("Error al comunicarse con la API externa de conversión: " + mensaje);
	}
}
