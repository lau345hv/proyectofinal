package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando se recibe un ID nulo, cero o negativo
 * para una entidad del sistema.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class IdInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IdInvalidoException(String entidad, Long id) {
		super("El ID proporcionado para '" + entidad + "' no es válido: " + id
				+ ". El ID debe ser un número entero positivo mayor que cero.");
	}
}
