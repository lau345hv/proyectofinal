package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado en el sistema
 * (por ejemplo, un usuario o un registro del historial inexistente).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class RecursoNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RecursoNoEncontradoException(String mensaje) {
		super(mensaje);
	}
}
