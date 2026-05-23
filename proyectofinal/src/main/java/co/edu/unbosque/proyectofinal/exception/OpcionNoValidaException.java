package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando se recibe un valor que no corresponde a
 * ninguna de las opciones permitidas para un campo enumerado
 * (por ejemplo, un tipo de archivo o un rol inexistente).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class OpcionNoValidaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OpcionNoValidaException(String campo, String valorRecibido, String opcionesValidas) {
		super("La opción '" + valorRecibido + "' no es válida para el campo '" + campo + "'. "
				+ "Las opciones permitidas son: " + opcionesValidas + ".");
	}
}
