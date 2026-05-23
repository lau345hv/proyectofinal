package co.edu.unbosque.proyectofinal.exception;

/**
 * Excepción lanzada cuando un campo de texto obligatorio
 * llega vacío, nulo o solo con espacios en blanco.
 *
 * @author Equipo de desarrollo
 * @version 1.0
 */
public class TextoVacioException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TextoVacioException(String campo) {
		super("El campo '" + campo + "' no puede estar vacío ni contener solo espacios. "
				+ "Por favor ingrese un valor válido.");
	}
}
