package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando se intenta registrar un nombre de usuario
 * que ya está en uso por otra cuenta.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class NombreUsuarioDuplicadoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NombreUsuarioDuplicadoException(String mensaje) {
		super(mensaje);
	}
}
