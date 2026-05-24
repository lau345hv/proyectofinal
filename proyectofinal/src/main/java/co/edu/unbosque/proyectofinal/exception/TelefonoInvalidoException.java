package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción que indica que el número de teléfono no cumple el formato
 * colombiano: exactamente 10 dígitos que comienzan con 3.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class TelefonoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TelefonoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
