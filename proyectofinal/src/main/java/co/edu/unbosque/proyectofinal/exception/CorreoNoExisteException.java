package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción que indica que el correo electrónico no existe o no es
 * válido según la verificación externa contra el servidor de correo.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class CorreoNoExisteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CorreoNoExisteException(String mensaje) {
		super(mensaje);
	}
}
