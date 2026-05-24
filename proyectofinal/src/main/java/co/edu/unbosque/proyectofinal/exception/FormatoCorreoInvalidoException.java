package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando un correo electrónico no cumple con el
 * formato esperado (debe contener '@' y un dominio con punto).
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class FormatoCorreoInvalidoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FormatoCorreoInvalidoException(String correo) {
		super("El correo '" + correo + "' no tiene un formato válido. "
				+ "El correo debe contener '@' y un dominio con punto. "
				+ "Ejemplo de formato correcto: usuario@dominio.com");
	}
}
