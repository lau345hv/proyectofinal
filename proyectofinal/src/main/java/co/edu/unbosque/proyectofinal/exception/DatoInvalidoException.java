package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando se recibe un dato con un valor inválido
 * que no cumple las reglas de negocio del sistema.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class DatoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DatoInvalidoException(String mensaje) {
		super(mensaje);
	}
}
