package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando se intenta registrar un correo electrónico
 * que ya existe en el sistema.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class CorreoDuplicadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CorreoDuplicadoException(String mensaje) {
		super(mensaje);
	}
}
