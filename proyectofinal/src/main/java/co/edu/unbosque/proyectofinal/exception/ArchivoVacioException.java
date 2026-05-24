package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando se intenta convertir un archivo vacío,
 * nulo o de tamaño cero enviado por el usuario.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class ArchivoVacioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ArchivoVacioException(String mensaje) {
		super(mensaje);
	}
}
